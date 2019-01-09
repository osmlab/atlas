package ast_repo_subsystem;

use warnings;
use strict;

use Exporter qw(import);
use File::Path qw(make_path rmtree);
use File::Temp qw(tempdir tempfile);
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
        mode => 0755
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
    system { $editor[0] } @editor;

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

    my $url = read_single_config_variable($ast_path, $program_name, $quiet, $repo, 'url');
    my $ref = read_single_config_variable($ast_path, $program_name, $quiet, $repo, 'ref');
    my @excludes = read_multiple_config_variables($ast_path, $program_name, $quiet, $repo, 'exclude');
    my @skips = read_multiple_config_variables($ast_path, $program_name, $quiet, $repo, 'skip');
    unless (defined $url && defined $ref) {
        ast_utilities::error_output($program_name, "repo install operation failed");
        return 0;
    }

    my $tmpdir = tempdir(CLEANUP => 1);

    my @command = ();
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

    my $ref_to_use;
    if ($ref_override eq '') {
        $ref_to_use = $ref;
    }
    else {
        $ref_to_use = $ref_override;
    }
    @command = ();
    push @command, "git";
    push @command, "checkout";
    push @command, "${ref_to_use}";
    $success = system {$command[0]} @command;
    unless ($success == 0) {
        ast_utilities::error_output($program_name, "repo install operation failed");
        return 0;
    }
    my $commit = `git rev-parse --short HEAD`;
    chomp $commit;

    my $tentative_module_name = "${repo}-${commit}";
    my %modules = ast_module_subsystem::get_module_to_status_hash($ast_path);
    my @module_names = keys %modules;
    foreach my $module_name (@module_names) {
        if ($tentative_module_name eq $module_name) {
            print STDERR "\n";
            ast_utilities::warn_output($ast_utilities::CONFIG_PROGRAM, "nothing to do");
            print STDERR "Repo ${bold_stderr}${repo}${reset_stderr} with ref ${bold_stderr}${ref_to_use}${reset_stderr} already up-to-date through installed module ${bold_stderr}${tentative_module_name}${reset_stderr}\n";
            return 1;
        }
    }

    my $gradle_injection = "
    task atlasshelltools(type: Jar) {
        baseName = project.name
        classifier = '-AST'
        from {
            configurations.atlasshelltools.collect { it.isDirectory() ? it : zipTree(it) }
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

    @command = ();
    push @command, "find";
    push @command, ".";
    push @command, "-type";
    push @command, "f";
    push @command, "-name";
    push @command, "*-AST.jar";
    push @command, "-exec";
    push @command, "atlas-config";
    push @command, "install";
    push @command, "{}";
    push @command, "--force";
    push @command, "--name";
    push @command, "${repo}-${commit}";
    push @command, ";";
    $success = system {$command[0]} @command;
    unless ($success == 0) {
        ast_utilities::error_output($program_name, "repo install operation failed");
        return 0;
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
        return ();
    }

    my $repo_config_file = File::Spec->catfile($repo_subfolder, $REPO_CONFIG);
    unless (-f $repo_config_file) {
        ast_utilities::error_output($program_name, "could not find config file for repo ${bold_stderr}${repo}${reset_stderr}");
        return ();
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
