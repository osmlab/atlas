package ast_repo_subsystem;

use warnings;
use strict;

use Exporter qw(import);
use File::Basename qw(basename);
use File::Path qw(make_path rmtree);
use File::Temp qw(tempdir tempfile);
use POSIX qw(strftime);
use ast_tty;
use ast_module_subsystem;
use ast_utilities;

# Export symbols: variables and subroutines
our @EXPORT = qw(
    REPOS_FOLDER
    create_repo
    edit_repo
    list_repos
    remove_repo
    install_repo
    get_all_repos
    add_skip_variable
    add_exclude_variable
    get_repo_settings
    print_repo_settings
);

our $REPOS_FOLDER = 'repos';
our $REPO_CONFIG = 'repo_config';

my $no_colors_stdout = ast_tty::is_no_colors_stdout();
my $red_stdout = $no_colors_stdout ? "" : ast_tty::ansi_red();
my $green_stdout = $no_colors_stdout ? "" : ast_tty::ansi_green();
my $magenta_stdout = $no_colors_stdout ? "" : ast_tty::ansi_magenta();
my $bold_stdout = $no_colors_stdout ? "" : ast_tty::ansi_bold();
my $bunl_stdout = $no_colors_stdout ? "" : ast_tty::ansi_begin_underln();
my $eunl_stdout = $no_colors_stdout ? "" : ast_tty::ansi_end_underln();
my $reset_stdout = $no_colors_stdout ? "" : ast_tty::ansi_reset();

my $no_colors_stderr = ast_tty::is_no_colors_stderr();
my $red_stderr = $no_colors_stderr ? "" : ast_tty::ansi_red();
my $green_stderr = $no_colors_stderr ? "" : ast_tty::ansi_green();
my $magenta_stderr = $no_colors_stderr ? "" : ast_tty::ansi_magenta();
my $bold_stderr = $no_colors_stderr ? "" : ast_tty::ansi_bold();
my $reset_stderr = $no_colors_stderr ? "" : ast_tty::ansi_reset();

my $REPO_EDIT_HEADER = "# Lines beginning with \"#\" are ignored
#
# Add exclude packages for gradle using \"exclude\".
# To exclude multiple packages, simply repeat this config variable for each package. E.g.
# exclude = com.example.package
# exclude = com.example.anotherpackage
#
# Skip gradle tasks using \"skip\".
# To skip multiple tasks, simply repeat this config variable for each task. E.g.
# skip = javadoc
# skip = integrationTest
#
# If you're stuck, hit <ESC> then type :q!<Enter> to abort the edit.
# To save your changes, hit <ESC> then type :wq<Enter>";

#
# TODO fix all the hardcoded stuff.
# e.g. 'url', 'ref', 'exclude', etc.
#

# Create a new repo.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output output
#   $repo: the name of the repo
#   $url: the repo URL
#   $ref: the ref (a branch, tag, even a commit)
# Return: 1 on success, 0 on failure
sub create_repo {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $repo = shift;
    my $url = shift;
    my $ref = shift;

    my $repo_subfolder = File::Spec->catfile($ast_path, $REPOS_FOLDER, $repo);
    if (-d $repo_subfolder) {
        ast_utilities::error_output($program_name, "repo ${bold_stderr}${repo}${reset_stderr} already exists");
        return 0;
    }

    make_path("$repo_subfolder", {
        verbose => 0,
        mode    => 0755
    });

    my $repo_config_file = File::Spec->catfile($repo_subfolder, $REPO_CONFIG);
    open my $file_handle, '>', "$repo_config_file";
    print $file_handle "url = ${url}\n";
    print $file_handle "ref = ${ref}\n";
    close $file_handle;

    unless ($quiet) {
        print "New repo: ${bold_stdout}${repo}${reset_stdout}\nURL: ${bold_stdout}${url}${reset_stdout}\nRef: ${bold_stdout}${ref}${reset_stdout}\n";
    }

    return 1;
}

# Edit a repo.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output output
#   $repo: the name of the repo
# Return: 1 on success, 0 on failure
sub edit_repo {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $repo = shift;

    my $repo_subfolder = File::Spec->catfile($ast_path, $REPOS_FOLDER, $repo);
    unless (-d $repo_subfolder) {
        ast_utilities::error_output($program_name, "no such repo ${bold_stderr}${repo}${reset_stderr}");
        return 0;
    }

    my $repo_config_file = File::Spec->catfile($repo_subfolder, $REPO_CONFIG);
    unless (-f $repo_config_file) {
        ast_utilities::error_output($program_name, "could not find config file for repo ${bold_stderr}${repo}${reset_stderr}");
        print STDERR "To resolve, please remove the repo and re-add.\n";
        return 0;
    }

    # Create the staging file
    my $handle;
    my $staging_file;
    my $tmpdir = tempdir(CLEANUP => 1);
    ($handle, $staging_file) = tempfile(DIR => $tmpdir);
    close $handle;

    # copy the current config file into the staging file
    open my $stage_handle, '>', "$staging_file";
    print $stage_handle "# Config for repo ${repo}\n";
    print $stage_handle "${REPO_EDIT_HEADER}\n";
    my $url = read_single_config_variable_from_arbitrary_file($repo_config_file, 'url');
    my $ref = read_single_config_variable_from_arbitrary_file($repo_config_file, 'ref');
    my @skips = read_multiple_config_variables_from_arbitrary_file($repo_config_file, 'skip');
    my @excludes = read_multiple_config_variables_from_arbitrary_file($repo_config_file, 'exclude');
    print $stage_handle "url = ${url}\n";
    print $stage_handle "ref = ${ref}\n";
    foreach my $skip (@skips) {
        print $stage_handle "skip = ${skip}\n";
    }
    foreach my $exclude (@excludes) {
        print $stage_handle "exclude = ${exclude}\n";
    }
    close $stage_handle;

    # open the staging file in the user's editor
    my @editor = ast_utilities::get_editor();
    push @editor, "$staging_file";
    system {$editor[0]} @editor;

    # confirm that the staging file is not malformed, i.e. it must have a valid URL and ref
    $url = read_single_config_variable_from_arbitrary_file($staging_file, 'url');
    $ref = read_single_config_variable_from_arbitrary_file($staging_file, 'ref');
    if ($url eq '' || $ref eq '') {
        ast_utilities::error_output($program_name, "failed to parse \'url\' and \'ref\' config variables");
        print STDERR "Aborting edit without saving...\n";
        return 0;
    }

    # copy the staging file back into the actual config file
    open $handle, '>', "$repo_config_file";
    $url = read_single_config_variable_from_arbitrary_file($staging_file, 'url');
    $ref = read_single_config_variable_from_arbitrary_file($staging_file, 'ref');
    @skips = read_multiple_config_variables_from_arbitrary_file($staging_file, 'skip');
    @excludes = read_multiple_config_variables_from_arbitrary_file($staging_file, 'exclude');
    print $handle "url = ${url}\n";
    print $handle "ref = ${ref}\n";
    foreach my $skip (@skips) {
        print $handle "skip = ${skip}\n";
    }
    foreach my $exclude (@excludes) {
        print $handle "exclude = ${exclude}\n";
    }
    close $handle;

    return 1;
}

# List the repos.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output output
# Return: 1 on success, 0 on failure
sub list_repos {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;

    my @filtered_repos = get_all_repos($ast_path);

    if (scalar @filtered_repos == 0) {
        ast_utilities::error_output($program_name, "found no repos");
        return 0;
    }

    print "${bold_stdout}Registered repos:${reset_stdout}\n\n";
    for my $found_repo (sort {lc $a cmp lc $b} @filtered_repos) {
        my $url = read_single_config_variable($ast_path, $program_name, $quiet, $found_repo, 'url');
        my $ref = read_single_config_variable($ast_path, $program_name, $quiet, $found_repo, 'ref');
        unless (defined $url && defined $ref) {
            ast_utilities::error_output($program_name, "repo list operation failed");
            return 0;
        }
        print "    ${bold_stdout}${found_repo}${reset_stdout} : ${url} (${ref})\n";
    }
    print "\n";

    return 1;
}

# Remove an existing repo.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output output
#   $repo: the name of the repo
# Return: 1 on success, 0 on failure
sub remove_repo {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $repo = shift;

    my $repo_subfolder = File::Spec->catfile($ast_path, $REPOS_FOLDER, $repo);
    unless (-d $repo_subfolder) {
        ast_utilities::error_output($program_name, "no such repo ${bold_stderr}${repo}${reset_stderr}");
        return 0;
    }

    rmtree($repo_subfolder);

    unless ($quiet) {
        print "Removed repo ${bold_stdout}${repo}${reset_stdout}.\n";
    }

    return 1;
}

# Install module using an existing repo.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output output
#   $repo: the name of the repo
#   $ref_override: an optional override ref
# Return: 1 on success, 0 on failure
sub install_repo {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $repo = shift;
    my $ref_override = shift;

    my $repo_subfolder = File::Spec->catfile($ast_path, $REPOS_FOLDER, $repo);
    unless (-d $repo_subfolder) {
        ast_utilities::error_output($program_name, "no such repo ${bold_stderr}${repo}${reset_stderr}");
        return 0;
    }

    my %module_metadata = ast_module_subsystem::get_module_to_metadata_hash($ast_path);

    my $url = read_single_config_variable($ast_path, $program_name, $quiet, $repo, 'url');
    my $ref = read_single_config_variable($ast_path, $program_name, $quiet, $repo, 'ref');
    my @excludes = read_multiple_config_variables($ast_path, $program_name, $quiet, $repo, 'exclude');
    my @skips = read_multiple_config_variables($ast_path, $program_name, $quiet, $repo, 'skip');
    unless (defined $url && defined $ref) {
        ast_utilities::error_output($program_name, "repo install operation failed");
        return 0;
    }
    my $ref_to_use;
    if ($ref_override eq '') {
        $ref_to_use = $ref;
    }
    else {
        $ref_to_use = $ref_override;
    }

    my $tmpdir = tempdir(CLEANUP => 1);

    # First, we can check to see if the provided ref is already represented in one of the installed
    # modules. Since this uses git ls-remote, this only works when the ref is a tag or a branch. If
    # the ref was a commit hash, then we will need to clone the repo first to determine if an install
    # is necessary.
    my @command = ();
    push @command, "git";
    push @command, "ls-remote";
    push @command, "${url}";
    push @command, "${ref_to_use}";
    my $lsremote_result = ast_utilities::read_command_output(\@command);
    my @remote_ref = split /\s+/, $lsremote_result;
    my $remote_commit_hash;
    if (scalar @remote_ref > 0) {
        $remote_commit_hash = $remote_ref[0];
    }
    if (defined $remote_commit_hash) {
        foreach my $module_key (keys %module_metadata) {
            my %metadata = %{$module_metadata{$module_key}};
            my $module_commit_hash = $metadata{$ast_module_subsystem::REPO_COMMIT_KEY};
            if (defined $module_commit_hash && $module_commit_hash eq $remote_commit_hash) {
                ast_utilities::warn_output($program_name, "nothing to do");
                print STDERR "Ref ${bold_stderr}${ref_to_use}${reset_stderr} in repo ${bold_stderr}${repo}${reset_stderr} refers to commit ${bold_stderr}${remote_commit_hash}${reset_stderr}.\n";
                print STDERR "Installed module ${bold_stderr}${module_key}${reset_stderr} was built from this commit.\n";
                print STDERR "Try \'${bold_stderr}${ast_utilities::CONFIG_PROGRAM} activate ${module_key}${reset_stderr}\' to use this version.\n";
                return 1;
            }
        }
    }

    @command = ();
    push @command, "git";
    push @command, "clone";
    push @command, "${url}";
    push @command, "${tmpdir}";
    my $success = system {$command[0]} @command;
    unless ($success == 0) {
        ast_utilities::error_output($program_name, "repo install operation failed");
        return 0;
    }

    chdir $tmpdir or die "$!";

    @command = ();
    push @command, "git";
    push @command, "checkout";
    push @command, "${ref_to_use}";
    $success = system {$command[0]} @command;
    unless ($success == 0) {
        ast_utilities::error_output($program_name, "repo install operation failed");
        return 0;
    }
    my $installed_commit_hash = `git rev-parse HEAD`;
    my $installed_commit_hash_short = `git rev-parse --short HEAD`;
    chomp $installed_commit_hash;
    chomp $installed_commit_hash_short;

    my $tentative_module_name = "${repo}-${installed_commit_hash_short}";
    my %modules = ast_module_subsystem::get_module_to_status_hash($ast_path);
    my @module_names = keys %modules;
    foreach my $module_name (@module_names) {
        if ($tentative_module_name eq $module_name) {
            print STDERR "\n";
            ast_utilities::warn_output($program_name, "nothing to do");
            print STDERR "Ref ${bold_stderr}${ref_to_use}${reset_stderr} in repo ${bold_stderr}${repo}${reset_stderr} refers to commit ${bold_stderr}${installed_commit_hash}${reset_stderr}.\n";
            print STDERR "Installed module ${bold_stderr}${module_name}${reset_stderr} was built from this commit.\n";
            print STDERR "Try \'${bold_stderr}${ast_utilities::CONFIG_PROGRAM} activate ${module_name}${reset_stderr}\' to use this version.\n";
            return 1;
        }
    }

    my $gradle_injection = "
    task atlasshelltools(type: Jar) {
        baseName = project.name
        classifier = '-AST'
        from {
            configurations.atlasshelltools.collect
            {
                it.isDirectory() ? it : zipTree(it).matching {
                    exclude
                    {
                        it.path.contains('META-INF') && (it.path.endsWith('.SF') || it.path.endsWith('.DSA') || it.path.endsWith('.RSA'))
                    }
                }
            }
        }
        with jar
        zip64 = true
    }

    configurations
    {
        atlasshelltools
        {
            %s
        }
    }

    dependencies
    {
        atlasshelltools project.configurations.getByName('compile')
        if (packages.slf4j != null) {
            atlasshelltools packages.slf4j.log4j12
        }
        if (packages.log4j != null) {
            atlasshelltools packages.log4j
        }
    }
    ";
    my @excludes_mapped = ();
    foreach my $exclude_element (@excludes) {
        push @excludes_mapped, "exclude group: \'${exclude_element}\';";
    }
    $gradle_injection = sprintf($gradle_injection, join("\n", @excludes_mapped));
    open my $file_handle, '>>', "$tmpdir/build.gradle" or die "Could not open build.gradle $!";
    print $file_handle "${gradle_injection}\n";
    close $file_handle;

    @command = ();
    push @command, "./gradlew";
    push @command, "clean";
    push @command, "atlasshelltools";
    foreach my $skip_element (@skips) {
        push @command, "-x";
        push @command, "$skip_element";
    }
    $success = system {$command[0]} @command;
    unless ($success == 0) {
        ast_utilities::error_output($program_name, "repo install operation failed");
        return 0;
    }

    my @find_command = (
        "find", ".",
        "-type", "f",
        "-name", "*-AST.jar",
        "-print0"
    );
    open FIND, "-|", @find_command;
    # TODO 'local' modifier makes sense here? confirm this, 'my' may make more sense
    # see https://www.perlmonks.org/?node_id=94007
    local $/ = "\0";
    while (<FIND>) {
        # FIND command is printing full paths, we just want the basename.
        # Also, we must chomp to remove the terminating null byte left over from
        # the '-print0' flag given to 'find'.
        my $module = $_;
        chomp $module;
        my $module_basename = basename($module);

        my %local_metadata;
        $local_metadata{$ast_module_subsystem::SOURCE_KEY} = "repo";
        $local_metadata{$ast_module_subsystem::URI_KEY} = "${url}";
        $local_metadata{$ast_module_subsystem::REPO_NAME_KEY} = "${repo}";
        $local_metadata{$ast_module_subsystem::REPO_REF_KEY} = "${ref_to_use}";
        $local_metadata{$ast_module_subsystem::REPO_COMMIT_KEY} = "${installed_commit_hash}";
        $local_metadata{$ast_module_subsystem::DATE_TIME_KEY} = strftime("%Y-%m-%d %H:%M:%S UTC", gmtime(time));
        # install the module!
        my $success = ast_module_subsystem::perform_install($module, $ast_path, $program_name,
            "${repo}-${installed_commit_hash_short}", 0, 0, 1, 0, \%local_metadata, 0);

        unless ($success) {
            ast_utilities::error_output($program_name, "repo install operation failed");
            return 0;
        }
    }
    return 1;
}

# Get an array containing all repo names. Useful for autocomplete and listing code.
sub get_all_repos {
    my $ast_path = shift;

    my $repo_folder = File::Spec->catfile($ast_path, $REPOS_FOLDER);

    opendir my $repo_dir_handle, $repo_folder or die "Something went wrong opening dir: $!";
    my @repos = readdir $repo_dir_handle;
    closedir $repo_dir_handle;

    # we need to filter '.' and '..'
    my @filtered_repos = ();
    for my $found_repo (@repos) {
        unless ($found_repo eq '.' || $found_repo eq '..') {
            push @filtered_repos, $found_repo;
        }
    }

    return @filtered_repos;
}

# TODO refactor DRY
# Wrapper for append_config_variable_to_file. Used by UI code.
sub add_skip_variable {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $repo = shift;
    my $value = shift;

    my $repo_subfolder = File::Spec->catfile($ast_path, $REPOS_FOLDER, $repo);
    unless (-d $repo_subfolder) {
        ast_utilities::error_output($program_name, "repo ${bold_stderr}${repo}${reset_stderr} does not exist");
        return 0;
    }

    my $repo_config_file = File::Spec->catfile($repo_subfolder, $REPO_CONFIG);
    unless (-f $repo_config_file) {
        ast_utilities::error_output($program_name, "could not find config file for repo ${bold_stderr}${repo}${reset_stderr}");
        return 0;
    }

    append_config_variable_to_file($repo_config_file, 'skip', $value);

    return 1;
}

# TODO refactor DRY
# Wrapper for append_config_variable_to_file. Used by UI code.
sub add_exclude_variable {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $repo = shift;
    my $value = shift;

    my $repo_subfolder = File::Spec->catfile($ast_path, $REPOS_FOLDER, $repo);
    unless (-d $repo_subfolder) {
        ast_utilities::error_output($program_name, "repo ${bold_stderr}${repo}${reset_stderr} does not exist");
        return 0;
    }

    my $repo_config_file = File::Spec->catfile($repo_subfolder, $REPO_CONFIG);
    unless (-f $repo_config_file) {
        ast_utilities::error_output($program_name, "could not find config file for repo ${bold_stderr}${repo}${reset_stderr}");
        return 0;
    }

    append_config_variable_to_file($repo_config_file, 'exclude', $value);

    return 1;
}

# Get an array containing string-ified repo settings. The array is useful for
# output purposes.
sub get_repo_settings {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $repo = shift;

    my $repo_subfolder = File::Spec->catfile($ast_path, $REPOS_FOLDER, $repo);
    unless (-d $repo_subfolder) {
        ast_utilities::error_output($program_name, "repo ${bold_stderr}${repo}${reset_stderr} does not exist");
        return();
    }

    my $repo_config_file = File::Spec->catfile($repo_subfolder, $REPO_CONFIG);
    unless (-f $repo_config_file) {
        ast_utilities::error_output($program_name, "could not find config file for repo ${bold_stderr}${repo}${reset_stderr}");
        return();
    }

    my @settings = ();

    open my $file_handle, '<', $repo_config_file or die "Could not open file $repo_config_file $!";
    while (my $line = <$file_handle>) {
        chomp $line;
        if ($line eq '' || substr($line, 0, 1) eq '#') {
            next;
        }
        # trim excess whitespace from left and right
        $line =~ s/^\s+|\s+$//g;
        if ($line eq '' || substr($line, 0, 1) eq '#') {
            next;
        }
        my @line_split = split '=', $line, 2;
        unless (defined $line_split[0]) {
            next;
        }
        # trim excess whitespace from left and right
        $line_split[0] =~ s/^\s+|\s+$//g;
        if (defined $line_split[1] && $line_split[1] !~ /^\s*$/) {
            # trim excess whitespace from left and right
            $line_split[1] =~ s/^\s+|\s+$//g;
            push @settings, "$line_split[0] = $line_split[1]";
        }
    }
    close $file_handle;

    return @settings;
}

# Print repo settings using get_repo_settings.
sub print_repo_settings {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $repo = shift;

    if ($quiet) {
        return;
    }

    my @settings = get_repo_settings($ast_path, $program_name, $quiet, $repo);
    if (scalar @settings != 0) {
        print "${bold_stdout}${repo} settings:${reset_stdout}\n";
    }
    foreach my $setting (@settings) {
        print "${setting}\n";
    }
}

# TODO refactor DRY
# Given an arbitrary file path, opens it and attempts to read the first config
# variable that matches the given variable. If the value cannot be read, returns
# an empty string.
sub read_single_config_variable_from_arbitrary_file {
    my $file = shift;
    my $variable = shift;

    my $value = '';
    open my $file_handle, '<', $file or die "Could not open file $file $!";
    while (my $line = <$file_handle>) {
        chomp $line;
        # trim excess whitespace from left and right
        $line =~ s/^\s+|\s+$//g;
        if ($line eq '' || substr($line, 0, 1) eq '#') {
            next;
        }
        my @line_split = split '=', $line, 2;
        unless (defined $line_split[0]) {
            next;
        }
        # trim excess whitespace from left and right
        $line_split[0] =~ s/^\s+|\s+$//g;
        if ($line_split[0] eq $variable) {
            if (defined $line_split[1] && $line_split[1] !~ /^\s*$/) {
                # trim excess whitespace from left and right
                $line_split[1] =~ s/^\s+|\s+$//g;
                $value = $line_split[1];
            }
        }
    }
    close $file_handle;

    return $value;
}

# TODO refactor DRY
# Given an arbitrary file path, opens it and attempts to read the all config
# variables that match the given variable. The values will be returned in an array.
# If no values could be read, the array will be empty.
sub read_multiple_config_variables_from_arbitrary_file {
    my $file = shift;
    my $variable = shift;

    my @values = ();
    open my $file_handle, '<', $file or die "Could not open file $file $!";
    while (my $line = <$file_handle>) {
        chomp $line;
        # trim excess whitespace from left and right
        $line =~ s/^\s+|\s+$//g;
        if ($line eq '' || substr($line, 0, 1) eq '#') {
            next;
        }
        my @line_split = split '=', $line, 2;
        unless (defined $line_split[0]) {
            next;
        }
        # trim excess whitespace from left and right
        $line_split[0] =~ s/^\s+|\s+$//g;
        if ($line_split[0] eq $variable) {
            if (defined $line_split[1] && $line_split[1] !~ /^\s*$/) {
                # trim excess whitespace from left and right
                $line_split[1] =~ s/^\s+|\s+$//g;
                push @values, $line_split[1];
            }
        }
    }
    close $file_handle;

    return @values;
}

# Given a file, a variable, and a value, append the variable setting to the file.
sub append_config_variable_to_file {
    my $file = shift;
    my $variable = shift;
    my $value = shift;

    open my $file_handle, '>>', $file or die "Could not open file $file $!";
    print $file_handle "${variable} = ${value}\n";
    close $file_handle;
}

# Wrapper for read_single_config_variable_from_arbitrary_file that uses a given
# ast_path and repo name. This makes it easier for some of the repo subroutines to
# do proper error handling.
sub read_single_config_variable {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $repo = shift;
    my $variable = shift;

    my $repo_subfolder = File::Spec->catfile($ast_path, $REPOS_FOLDER, $repo);
    unless (-d $repo_subfolder) {
        ast_utilities::error_output($program_name, "repo ${bold_stderr}${repo}${reset_stderr} does not exist");
        return 0;
    }

    my $repo_config_file = File::Spec->catfile($repo_subfolder, $REPO_CONFIG);
    unless (-f $repo_config_file) {
        ast_utilities::error_output($program_name, "could not find config file for repo ${bold_stderr}${repo}${reset_stderr}");
        return 0;
    }

    my $value = read_single_config_variable_from_arbitrary_file($repo_config_file, $variable);

    if ($value eq '') {
        ast_utilities::error_output($program_name, "failed to parse config setting \'${variable}\' for repo ${bold_stderr}${repo}${reset_stderr}");
        return undef;
    }

    return $value;
}

# Wrapper for read_multiple_config_variables_from_arbitrary_file that uses a given
# ast_path and repo name. This makes it easier for some of the repo subroutines to
# do proper error handling.
sub read_multiple_config_variables {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $repo = shift;
    my $variable = shift;

    my $repo_subfolder = File::Spec->catfile($ast_path, $REPOS_FOLDER, $repo);
    unless (-d $repo_subfolder) {
        ast_utilities::error_output($program_name, "repo ${bold_stderr}${repo}${reset_stderr} does not exist");
        return 0;
    }

    my $repo_config_file = File::Spec->catfile($repo_subfolder, $REPO_CONFIG);
    unless (-f $repo_config_file) {
        ast_utilities::error_output($program_name, "could not find config file for repo ${bold_stderr}${repo}${reset_stderr}");
        return 0;
    }

    my @values = read_multiple_config_variables_from_arbitrary_file($repo_config_file, $variable);

    return @values;
}

# Check that a repo name matches the approved name regex.
# Params:
#   $repo: the repo to check
# Return: if the repo name matched the regex
sub repo_regex_ok {
    my $repo = shift;

    if ($repo =~ m/^[_a-zA-Z0-9][_a-zA-Z0-9-]*$/) {
        return 1;
    }
    return 0;
}

# Perl modules must return a value. Returning a value perl considers "truthy"
# signals that the module loaded successfully.
1;
