package ast_preset_subsystem;

use warnings;
use strict;

use Exporter qw(import);
use File::Spec;
use File::Path qw(make_path rmtree);
use File::Temp qw(tempdir tempfile);
use ast_utilities;
use ast_tty;

# Export symbols: variables and subroutines
our @EXPORT = qw(
    PRESETS_FOLDER
    CURRENT_NAMESPACE_FILE
    NAMESPACE_PATH
    DEFAULT_NAMESPACE
    reset_namespace
    get_namespace
    save_preset
    remove_preset
    remove_all_presets_for_command
    all_presets_for_command
    all_presets
    show_preset
    edit_preset
    copy_preset
    apply_preset_or_exit
    read_preset
    get_namespaces_array
    all_namespaces
    create_namespace
    use_namespace
    remove_namespace
    get_all_presets_in_current_namespace
    get_all_presets_for_command
    get_all_global_presets
    preset_regex_ok
    preset_regex
);

our $PRESETS_FOLDER = 'presets';
our $GLOBAL_FOLDER = '.global';

our $CURRENT_NAMESPACE_FILE = '.current_namespace';
our $NAMESPACE_PATH = File::Spec->catfile($PRESETS_FOLDER, $CURRENT_NAMESPACE_FILE);
our $DEFAULT_NAMESPACE = 'default';

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

# A header for the preset edit screen.
my $PRESET_EDIT_HEADER = "# Each line in this file will become a discrete ARGV element. For this
# reason, please use '--option=optionArgument' syntax (note the '=')
# for option arguments. Otherwise, your preset will not function properly.
# Also note that all non-option arguments will be dropped, and any line
# beginning with a '#' will be ignored.
#
# If you're stuck, hit <ESC> then type :q!<Enter> to abort the edit.
# To save your changes, hit <ESC> then type :wq<Enter>";

# Reset the preset namespace to default. If an extra argument is supplied,
# then reset the namespace to the supplied argument.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $new_namespace: an optional namespace to swap to
# Return: none
sub reset_namespace {
    my $ast_path = shift;
    my $new_namespace = shift;
    my $namespace_file = File::Spec->catfile($ast_path, $ast_preset_subsystem::NAMESPACE_PATH);
    open my $file_handle, '>', "$namespace_file";
    if (defined $new_namespace) {
        print $file_handle "${new_namespace}\n";
    }
    else {
        print $file_handle "${DEFAULT_NAMESPACE}\n";
    }
    close $file_handle;
}

# Read and return the current namespace.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
# Return: the current namespace
sub get_namespace {
    my $ast_path = shift;
    my $namespace_file = File::Spec->catfile($ast_path, $NAMESPACE_PATH);
    open my $file_handle, '<', "$namespace_file";
    my $firstline = <$file_handle>;
    chomp $firstline;
    close $file_handle;
    return $firstline;
}

# Save a preset for a given command.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output output
#   $preset: the name of the preset
#   $command: the name of the command
#   $namespace: the namespace to which to save
#   $argv_ref: a reference to an array containing all the options and args
# Return: 1 on success, 0 on failure
sub save_preset {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $preset = shift;
    my $command = shift;
    my $namespace = shift;
    my $argv_ref = shift;

    my @argv = @{$argv_ref};

    my $preset_subfolder = File::Spec->catfile($ast_path, $PRESETS_FOLDER, $namespace, $command);
    make_path("$preset_subfolder", {
        verbose => 0,
        mode    => 0755
    });
    my $preset_file = File::Spec->catfile($preset_subfolder, $preset);

    if (-f $preset_file) {
        if ($command eq $GLOBAL_FOLDER) {
            ast_utilities::error_output($program_name, "global preset ${bold_stderr}${preset}${reset_stderr} already exists");
        } else {
            ast_utilities::error_output($program_name, "preset ${bold_stderr}${preset}${reset_stderr} already exists for ${bold_stderr}${command}${reset_stderr}");
        }
        return 0;
    }

    my @detected_options = ();
    foreach my $arg (@argv) {
        # treat '-' as a regular argument
        if ($arg eq '-') {
            ast_utilities::warn_output($program_name, "preset discarding non-option arg \'${bold_stderr}$arg${reset_stderr}\'");
            next;
        }

        # stop processing once we see '--'
        if ($arg eq '--') {
            last;
        }

        # detect an option or skip non-option arguments
        if (ast_utilities::string_starts_with($arg, '--') || ast_utilities::string_starts_with($arg, '-')) {
            push @detected_options, $arg;
        }
        else {
            ast_utilities::warn_output($program_name, "preset discarding non-option arg \'${bold_stderr}$arg${reset_stderr}\'");
        }
    }

    if (scalar @detected_options == 0) {
        if (ast_utilities::is_dir_empty($preset_subfolder)) {
            rmdir($preset_subfolder);
        }
        ast_utilities::error_output($program_name, "cannot save an empty preset");
        return 0;
    }

    if ($command eq $GLOBAL_FOLDER) {
        print "Global preset ${bold_stdout}${preset}${reset_stdout}:\n";
    } else {
        print "Preset ${bold_stdout}${preset}${reset_stdout} for command ${bold_stdout}${command}${reset_stdout}:\n";
    }
    print "\n${bunl_stdout}Preset ARGV${eunl_stdout}${reset_stdout}\n";
    open my $file_handle, '>', "$preset_file";
    foreach my $option (@detected_options) {
        print "${bold_stdout}${option}${reset_stdout}\n";
        print $file_handle "${option}\n";
    }
    close $file_handle;

    unless ($quiet) {
        if ($command eq $GLOBAL_FOLDER) {
            print "\nGlobal preset ${bold_stdout}${preset}${reset_stdout} saved.\n";
        } else {
            print "\nPreset ${bold_stdout}${preset}${reset_stdout} saved for command ${bold_stdout}${command}${reset_stdout}.\n";
        }
    }

    return 1;
}

# Remove a given preset for a given command.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $preset: the name of the preset
#   $command: the name of the command
#   $namespace: the namespace from which to remove
# Return: 1 on success, 0 on failure
sub remove_preset {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $preset = shift;
    my $command = shift;
    my $namespace = shift;

    my $preset_subfolder = File::Spec->catfile($ast_path, $PRESETS_FOLDER, $namespace, $command);
    my $preset_file = File::Spec->catfile($preset_subfolder, $preset);

    unless (-f $preset_file) {
        if ($command eq $GLOBAL_FOLDER) {
            ast_utilities::error_output($program_name, "no such global preset ${bold_stderr}${preset}${reset_stderr}");
        }
        else {
            ast_utilities::error_output($program_name, "no such preset ${bold_stderr}${preset}${reset_stderr} for command ${bold_stderr}${command}${reset_stderr}");
        }
        return 0;
    }

    unlink $preset_file;

    unless ($quiet) {
        if ($command eq $GLOBAL_FOLDER) {
            print "Removed global preset ${bold_stdout}${preset}${reset_stdout}.\n";
        }
        else {
            print "Removed preset ${bold_stdout}${preset}${reset_stdout} for ${bold_stdout}${command}${reset_stdout}.\n";
        }
    }

    if (ast_utilities::is_dir_empty($preset_subfolder)) {
        rmdir($preset_subfolder);
    }

    return 1;
}

# Remove all presets for a given command.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $command: the name of the command
#   $namespace: the namespace from which to remove
# Return: 1 on success, 0 on failure
sub remove_all_presets_for_command {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $command = shift;
    my $namespace = shift;

    my $preset_subfolder = File::Spec->catfile($ast_path, $PRESETS_FOLDER, $namespace, $command);

    unless (-d $preset_subfolder) {
        if ($command eq $GLOBAL_FOLDER) {
            ast_utilities::error_output($program_name, "no global presets found");
        } else {
            ast_utilities::error_output($program_name, "no presets found for command ${bold_stderr}${command}${reset_stderr}");
        }
        return 0;
    }

    rmtree($preset_subfolder);

    unless ($quiet) {
        if ($command eq $GLOBAL_FOLDER) {
            print "Removed all global presets.\n";
        }
        else {
            print "Removed all presets for ${bold_stdout}${command}${reset_stdout}.\n";
        }
    }

    return 1;
}

# List all presets for a given command.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $command: the name of the command
#   $namespace: the namespace to save to
# Return: 1 on success, 0 on failure
sub all_presets_for_command {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $command = shift;
    my $namespace = shift;

    my $preset_subfolder = File::Spec->catfile($ast_path, $PRESETS_FOLDER, $namespace, $command);

    unless (-d $preset_subfolder) {
        if ($command eq $GLOBAL_FOLDER) {
            die "no global presets folder found in namespace ${namespace}";
        }
        ast_utilities::error_output($program_name, "no presets found for ${bold_stderr}${command}${reset_stderr}");
        return 0;
    }

    opendir my $presets_dir_handle, $preset_subfolder or die "Something went wrong opening dir: $!";
    my @presets = readdir $presets_dir_handle;
    closedir $presets_dir_handle;

    # we need to filter '.' and '..'
    my @filtered_presets = ();
    for my $found_preset (@presets) {
        unless ($found_preset eq '.' || $found_preset eq '..') {
            push @filtered_presets, $found_preset;
        }
    }

    if (scalar @filtered_presets == 0) {
        if ($command eq $GLOBAL_FOLDER) {
            return 0;
        }
        return 0;
    }

    if ($command eq $GLOBAL_FOLDER) {
        print "Global presets:\n\n";
    } else {
        print "Command ${bold_stdout}${command}${reset_stdout} presets:\n\n";
    }
    foreach my $found_preset (sort {lc $a cmp lc $b} @filtered_presets) {
        print "    ${bold_stdout}${found_preset}${reset_stdout}\n";
    }
    print "\n";

    return 1;
}


# List all presets in a given namespace.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $namespace: the namespace to display
# Return: 1 on success, 0 on failure
sub all_presets {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $namespace = shift;

    my $namespace_folder = File::Spec->catfile($ast_path, $PRESETS_FOLDER, $namespace);

    unless (-d $namespace_folder) {
        ast_utilities::error_output($program_name, "no such namespace ${bold_stderr}${namespace}${reset_stderr}");
        return 0;
    }

    if (ast_utilities::is_dir_empty($namespace_folder)) {
        ast_utilities::error_output($program_name, "no presets found");
        return 0;
    }

    opendir my $namespace_dir_handle, $namespace_folder or die "Something went wrong opening dir: $!";
    my @command_folders = readdir $namespace_dir_handle;
    closedir $namespace_dir_handle;

    my $found_at_least_one = 0;
    foreach my $found_command (@command_folders) {
        my $command_folder = File::Spec->catfile($namespace_folder, $found_command);
        # we need to filter '.', '..', and the '.global' folder
        unless ($found_command eq '.' || $found_command eq '..' || $found_command eq $GLOBAL_FOLDER) {
            $found_at_least_one |= all_presets_for_command($ast_path, $program_name, $quiet, $found_command, $namespace);
        }
    }

    my $global_folder = File::Spec->catfile($namespace_folder, $GLOBAL_FOLDER);
    $found_at_least_one |= all_presets_for_command($ast_path, $program_name, $quiet, $GLOBAL_FOLDER, $namespace);

    unless ($found_at_least_one) {
        ast_utilities::error_output($program_name, "no presets found");
        return 0;
    }

    return 1;
}

# Show a given preset for a given command.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $preset: the name of the preset
#   $command: the name of the command
#   $namespace: the namespace to save to
# Return: 1 on success, 0 on failure
sub show_preset {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $preset = shift;
    my $command = shift;
    my $namespace = shift;

    my $preset_subfolder = File::Spec->catfile($ast_path, $PRESETS_FOLDER, $namespace, $command);
    my $preset_file = File::Spec->catfile($preset_subfolder, $preset);

    unless (-f $preset_file) {
        if ($command eq $GLOBAL_FOLDER) {
            ast_utilities::error_output($program_name, "no such global preset ${bold_stderr}${preset}${reset_stderr}");
        } else {
            ast_utilities::error_output($program_name, "no such preset ${bold_stderr}${preset}${reset_stderr} for command ${bold_stderr}${command}${reset_stderr}");
        }
        return 0;
    }

    my @presets_from_file = read_preset($ast_path, $program_name, $quiet, $preset, $command, $namespace);
    if ($command eq $GLOBAL_FOLDER) {
        print "Global preset ${bold_stdout}${preset}${reset_stdout}:\n";
    } else {
        print "Preset ${bold_stdout}${preset}${reset_stdout} for command ${bold_stdout}${command}${reset_stdout}:\n";
    }
    print "\n${bunl_stdout}Preset ARGV${eunl_stdout}${reset_stdout}\n";
    for my $preset_from_file (@presets_from_file) {
        print "${bold_stdout}${preset_from_file}${reset_stderr}\n";
    }
    print "\n";

    return 1;
}

# Edit a given preset for a given command.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $preset: the name of the preset
#   $command: the name of the command
#   $namespace: the namespace
# Return: 1 on success, 0 on failure
sub edit_preset {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $preset = shift;
    my $command = shift;
    my $namespace = shift;

    my $preset_subfolder = File::Spec->catfile($ast_path, $PRESETS_FOLDER, $namespace, $command);
    make_path("$preset_subfolder", {
        verbose => 0,
        mode    => 0755
    });
    my $preset_file = File::Spec->catfile($preset_subfolder, $preset);

    my $creating_from_scratch = 0;
    unless (-f $preset_file) {
        $creating_from_scratch = 1;
    }

    my @presets_from_file = read_preset($ast_path, $program_name, $quiet, $preset, $command, $namespace);

    my $handle;
    my $stage_handle;
    my $tmpfile;
    my $preset_stage_file;
    my $tmpdir = tempdir(CLEANUP => 1);
    ($handle, $tmpfile) = tempfile(DIR => $tmpdir);
    ($stage_handle, $preset_stage_file) = tempfile(DIR => $tmpdir);
    close $handle;
    close $stage_handle;

    open $handle, '>', "$tmpfile";
    if ($creating_from_scratch) {
        print $handle "# CREATING PRESET\n";
    }
    else {
        print $handle "# EDITING PRESET\n";
    }
    print $handle "# Preset: ${preset}\n";
    print $handle "# Command: ${command}\n#\n";
    print $handle "${PRESET_EDIT_HEADER}\n";
    foreach my $cur_preset (@presets_from_file) {
        print $handle "${cur_preset}\n";
    }
    print $handle "\n";
    close $handle;

    my @editor = ast_utilities::get_editor();

    push @editor, "$tmpfile";
    system {$editor[0]} @editor;

    open $handle, '<', "$tmpfile";
    open $stage_handle, '>', "$preset_stage_file";
    while (my $line = <$handle>) {
        chomp $line;

        # skip empty lines
        if ($line eq '') {
            next;
        }

        # trim excess whitespace from left and right
        $line =~ s/^\s+|\s+$//g;

        # skip line if it starts with '#'
        if ($line =~ /^#.*/) {
            next;
        }

        # treat '-' as a regular argument
        if ($line eq '-') {
            ast_utilities::warn_output($program_name, "preset discarding non-option arg \'${bold_stderr}${line}${reset_stderr}\'");
            next;
        }

        # stop processing once we see '--'
        if ($line eq '--') {
            last;
        }

        # detect an option or skip non-option arguments
        if (ast_utilities::string_starts_with($line, '--') || ast_utilities::string_starts_with($line, '-')) {
            print $stage_handle "$line\n";
        }
        else {
            ast_utilities::warn_output($program_name, "preset discarding non-option arg \'${bold_stderr}${line}${reset_stderr}\'");
        }
    }
    close $handle;
    close $stage_handle;

    # Verify that the staged preset file is not empty
    if (-z $preset_stage_file) {
        ast_utilities::error_output($program_name, "preset cannot be empty");
        if (ast_utilities::is_dir_empty($preset_subfolder)) {
            rmdir($preset_subfolder);
        }
        return 0;
    }

    my @command = ();
    push @command, "cp";
    push @command, "$preset_stage_file";
    push @command, "$preset_file";
    system {$command[0]} @command;
    close $tmpdir;
    show_preset($ast_path, $program_name, $quiet, $preset, $command, $namespace);

    return 1;
}

# For a given command, copy a source preset to a destination preset.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $src_preset: the source preset
#   $dest_preset: the destination preset
#   $command: the name of the command
#   $namespace: the namespace
# Return: 1 on success, 0 on failure
sub copy_preset {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $src_preset = shift;
    my $dest_preset = shift;
    my $command = shift;
    my $namespace = shift;

    my $preset_subfolder = File::Spec->catfile($ast_path, $PRESETS_FOLDER, $namespace, $command);
    my $source_file = File::Spec->catfile($preset_subfolder, $src_preset);
    my $dest_file = File::Spec->catfile($preset_subfolder, $dest_preset);

    unless (-e $source_file) {
        if ($command eq $GLOBAL_FOLDER) {
            ast_utilities::error_output($program_name, "no such global preset ${bold_stderr}${src_preset}${reset_stderr}");
        } else {
            ast_utilities::error_output($program_name, "no such preset ${bold_stderr}${src_preset}${reset_stderr} for command ${bold_stderr}${command}${reset_stderr}");
        }
        return 0;
    }

    if (-e $dest_file) {
        if ($command eq $GLOBAL_FOLDER) {
            ast_utilities::error_output($program_name, "global preset ${bold_stderr}${dest_preset}${reset_stderr} already exists");
        }
        else {
            ast_utilities::error_output($program_name, "preset ${bold_stderr}${dest_preset}${reset_stderr} already exists for ${bold_stderr}${command}${reset_stderr}");
        }
        return 0;
    }

    my @command = ();
    push @command, "cp";
    push @command, "$source_file";
    push @command, "$dest_file";
    system {$command[0]} @command;

    unless ($quiet) {
        print "Copied contents of preset ${bold_stdout}${src_preset}${reset_stdout} into new preset ${bold_stdout}${dest_preset}${reset_stdout}.\n";
    }

    return 1;
}

# Apply preset(s) for a given command. Returns an updated argv array with the
# preset(s) applied. If the preset(s) do not exist, it will error and exit.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppres non-essential output
#   $presets: the name of the preset(s)
#   $command: the name of the command
#   $namespace: the namespace
#   $argv_ref: a reference to an array containing all the options and args
# Return: the updated argv array
sub apply_preset_or_exit {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $presets = shift;
    my $command = shift;
    my $namespace = shift;
    my $argv_ref = shift;

    my @argv = @{$argv_ref};

    my @presets_comma = split ',', $presets;
    my @presets_colon = split ':', $presets;
    my @presets_array;

    # User cannot use both ',' and ':', pick one
    if (scalar @presets_colon > 1 && scalar @presets_comma > 1) {
        ast_utilities::error_output($program_name, "cannot use both \',\' and \':\' to split presets, choose one");
        exit 1;
    }

    # In this case, either user split on ':' to separate or there was only 1 preset
    if (scalar @presets_colon >= scalar @presets_comma) {
        @presets_array = @presets_colon;
    }
    # Otherwise, the user split on ',' so use that
    else {
        @presets_array = @presets_comma;
    }

    my @final_argv = ();
    my $use_global = 0;
    foreach my $preset (@presets_array) {
        my $preset_subfolder = File::Spec->catfile($ast_path, $PRESETS_FOLDER, $namespace, $command);
        my $preset_file = File::Spec->catfile($preset_subfolder, $preset);
        unless (-f $preset_file) {
            # Fall back and try global presets
            my $global_subfolder = File::Spec->catfile($ast_path, $PRESETS_FOLDER, $namespace, $GLOBAL_FOLDER);
            my $global_file = File::Spec->catfile($global_subfolder, $preset);
            unless (-f $global_file) {
                ast_utilities::error_output($program_name, "preset ${bold_stderr}${preset}${reset_stderr} not found for command ${bold_stderr}${command}${reset_stderr}");
                ast_utilities::error_output($program_name, "preset ${bold_stderr}${preset}${reset_stderr} not found in globals");
                all_presets_for_command($ast_path, $program_name, $quiet, $command, $namespace);
                all_presets_for_command($ast_path, $program_name, $quiet, $GLOBAL_FOLDER, $namespace);
                exit 1;
            }
            $use_global = 1;
        }

        my @argv_from_presets = ();
        if ($use_global) {
            @argv_from_presets = read_preset($ast_path, $program_name, $quiet, $preset, $GLOBAL_FOLDER, $namespace);
        } else {
            @argv_from_presets = read_preset($ast_path, $program_name, $quiet, $preset, $command, $namespace);
        }
        foreach my $preset_argv_elem (@argv_from_presets) {
            push @final_argv, $preset_argv_elem;
        }
    }
    foreach my $argv_elem (@argv) {
        push @final_argv, $argv_elem;
    }

    return @final_argv;
}

# Read a preset for a given command. Returns the preset in an array
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $preset: the name of the preset
#   $command: the name of the command
#   $namespace: the current namespace
# Return: the preset array, or an empty array on error
sub read_preset {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $preset = shift;
    my $command = shift;
    my $namespace = shift;

    my $preset_subfolder = File::Spec->catfile($ast_path, $PRESETS_FOLDER, $namespace, $command);
    my $preset_file = File::Spec->catfile($preset_subfolder, $preset);

    unless (-f $preset_file) {
        return();
    }

    my @options = ();

    open my $file_handle, '<', $preset_file;

    # NOTE this fails if a preset option argument contained a newline
    while (my $line = <$file_handle>) {
        chomp $line;
        push @options, $line;
    }

    close($file_handle);
    return @options;
}

# Get an array containing each namespace as an element.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
# Return: the namespace array
sub get_namespaces_array {
    my $ast_path = shift;

    my $preset_folder = File::Spec->catfile($ast_path, $PRESETS_FOLDER);

    unless (-d $preset_folder) {
        die "The folder $PRESETS_FOLDER did not exist at $ast_path";
    }

    opendir my $presets_dir_handle, $preset_folder or die "Something went wrong opening dir: $!";
    my @namespaces = readdir $presets_dir_handle;
    closedir $presets_dir_handle;

    # we need to filter '.', '..', and '.current_namespace'
    my @filtered_namespaces = ();
    for my $found_namespace (@namespaces) {
        unless ($found_namespace eq '.' || $found_namespace eq '..'
            || $found_namespace eq $CURRENT_NAMESPACE_FILE) {
            push @filtered_namespaces, $found_namespace;
        }
    }

    return @filtered_namespaces;
}

# Print all namespaces, and denote the current checked out namespace.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
# Return: 1 on success, 0 on failure
sub all_namespaces {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;

    my $current_namespace = get_namespace($ast_path);
    my @namespaces = get_namespaces_array($ast_path);

    if (scalar @namespaces == 0) {
        error_output($program_name, "no namespaces found");
        return 0;
    }

    print "${bold_stdout}Preset namespaces:${reset_stdout}\n\n";
    for my $found_namespace (sort {lc $a cmp lc $b} @namespaces) {
        if ($found_namespace eq $current_namespace) {
            print "    * ${bold_stdout}${green_stdout}${found_namespace}${reset_stdout}\n";
        }
        else {
            print "      ${found_namespace}\n";
        }
    }
    print "\n";

    return 1;
}

# Create a new namespace.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $new_namespace: the namespace to create
# Return: 1 on success, 0 on failure
sub create_namespace {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $new_namespace = shift;

    my $current_namespace = get_namespace($ast_path);
    my $preset_folder = File::Spec->catfile($ast_path, $PRESETS_FOLDER);
    my $new_namespace_folder = File::Spec->catfile($preset_folder, $new_namespace);
    my $global_folder = File::Spec->catfile($preset_folder, $new_namespace, $GLOBAL_FOLDER);

    unless (-d $preset_folder) {
        die "The folder $PRESETS_FOLDER did not exist at $ast_path";
    }

    if (-d $new_namespace_folder) {
        ast_utilities::error_output($program_name, "namespace ${bold_stderr}${new_namespace}${reset_stderr} already exists");
        return 0;
    }

    make_path("$new_namespace_folder", {
        verbose => 0,
        mode    => 0755
    });

    make_path("$global_folder", {
        verbose => 0,
        mode    => 0755
    });

    unless ($quiet) {
        print "Created namespace ${bold_stdout}${new_namespace}${reset_stdout}.\n";
    }

    return 1;
}

# Check out a given namespace.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $namespace: the namespace to use
# Return: 1 on success, 0 on failure
sub use_namespace {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $namespace = shift;

    my $preset_folder = File::Spec->catfile($ast_path, $PRESETS_FOLDER);
    my $namespace_folder = File::Spec->catfile($preset_folder, $namespace);

    unless (-d $preset_folder) {
        die "The folder $PRESETS_FOLDER did not exist at $ast_path";
    }

    unless (-d $namespace_folder) {
        ast_utilities::error_output($program_name, "no such namespace ${bold_stderr}${namespace}${reset_stderr}");
        return 0;
    }

    reset_namespace($ast_path, $namespace);

    unless ($quiet) {
        print "Now using namespace ${bold_stdout}${namespace}${reset_stdout}.\n";
    }

    return 1;
}

# Remove a namespace.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $namespace: the namespace to remove
# Return: 1 on success, 0 on failure
sub remove_namespace {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $namespace = shift;

    my $current_namespace = get_namespace($ast_path);
    my $preset_folder = File::Spec->catfile($ast_path, $PRESETS_FOLDER);
    my $namespace_folder = File::Spec->catfile($preset_folder, $namespace);

    unless (-d $preset_folder) {
        die "The folder $PRESETS_FOLDER did not exist at $ast_path";
    }

    unless (-d $namespace_folder) {
        ast_utilities::error_output($program_name, "no such namespace ${bold_stderr}${namespace}${reset_stderr}");
        return 0;
    }

    if ($namespace eq $DEFAULT_NAMESPACE) {
        ast_utilities::error_output($program_name, "cannot remove the default namespace");
        return 0;
    }

    if ($namespace eq $current_namespace) {
        ast_utilities::error_output($program_name, "cannot remove in-use namespace ${bold_stderr}${namespace}${reset_stderr}");
        return 0;
    }

    rmtree($namespace_folder);

    unless ($quiet) {
        print "Removed namespace ${bold_stdout}${namespace}${reset_stdout}.\n";
    }

    return 1;
}

# Get an array of all presets in the current namespace, including global presets.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
# Return: the preset array
sub get_all_presets_in_current_namespace {
    my $ast_path = shift;

    my $namespace = get_namespace($ast_path);
    my @all_presets = ();

    my $preset_folder = File::Spec->catfile($ast_path, $PRESETS_FOLDER);
    my $namespace_folder = File::Spec->catfile($preset_folder, $namespace);
    my $global_folder = File::Spec->catfile($namespace_folder, $GLOBAL_FOLDER);

    opendir my $namespace_dir_handle, $namespace_folder or die "Something went wrong opening dir: $!";
    my @command_folders = readdir $namespace_dir_handle;
    closedir $namespace_dir_handle;

    foreach my $found_command (@command_folders) {
        my $command_folder = File::Spec->catfile($namespace_folder, $found_command);
        # we need to filter '.', '..'
        unless ($found_command eq '.' || $found_command eq '..') {
            opendir my $command_dir_handle, $command_folder or die "Something went wrong opening dir: $!";
            my @preset_files = readdir $command_dir_handle;
            closedir $command_dir_handle;

            foreach my $found_preset (@preset_files) {
                # we need to filter '.', '..'
                unless ($found_preset eq '.' || $found_preset eq '..') {
                    push @all_presets, $found_preset;
                }
            }
        }
    }

    opendir my $global_dir_handle, $global_folder or die "Something went wrong opening dir: $!";
    my @preset_files = readdir $global_dir_handle;
    closedir $global_dir_handle;

    foreach my $found_preset (@preset_files) {
        # we need to filter '.', '..'
        unless ($found_preset eq '.' || $found_preset eq '..') {
            push @all_presets, $found_preset;
        }
    }

    return @all_presets;
}

# Get an array of all presets in the current namespace for a given command.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $command: the command from which to check presets
# Return: the preset array
sub get_all_presets_for_command {
    my $ast_path = shift;
    my $command = shift;

    my $namespace = get_namespace($ast_path);
    my @all_presets = ();

    my $preset_folder = File::Spec->catfile($ast_path, $PRESETS_FOLDER);
    my $namespace_folder = File::Spec->catfile($preset_folder, $namespace);
    my $command_folder = File::Spec->catfile($namespace_folder, $command);

    unless (-d $command_folder) {
        return @all_presets;
    }

    opendir my $command_dir_handle, $command_folder or die "Something went wrong opening dir: $!";
    my @preset_files = readdir $command_dir_handle;
    closedir $command_dir_handle;

    foreach my $found_preset (@preset_files) {
        # we need to filter '.', '..'
        unless ($found_preset eq '.' || $found_preset eq '..') {
            push @all_presets, $found_preset;
        }
    }

    return @all_presets;
}

# Get an array of all global presets in the current namespace
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
# Return: the preset array
sub get_all_global_presets {
    my $ast_path = shift;

    my $namespace = get_namespace($ast_path);
    my @all_presets = ();

    my $preset_folder = File::Spec->catfile($ast_path, $PRESETS_FOLDER);
    my $namespace_folder = File::Spec->catfile($preset_folder, $namespace);
    my $global_folder = File::Spec->catfile($namespace_folder, $GLOBAL_FOLDER);

    unless (-d $global_folder) {
        return @all_presets;
    }

    opendir my $global_dir_handle, $global_folder or die "Something went wrong opening dir: $!";
    my @preset_files = readdir $global_dir_handle;
    closedir $global_dir_handle;

    foreach my $found_preset (@preset_files) {
        # we need to filter '.', '..'
        unless ($found_preset eq '.' || $found_preset eq '..') {
            push @all_presets, $found_preset;
        }
    }

    return @all_presets;
}

# Check that a preset name matches the approved name regex.
# Params:
#   $preset: the preset to check
# Return: if the preset name matched the regex
sub preset_regex_ok {
    my $preset = shift;

    my $regex = preset_regex();

    if ($preset =~ m/$regex/) {
        return 1;
    }
    return 0;
}

# Get the valid preset name regex.
# Params: none
# Return: the valid preset name regex
sub preset_regex {
    return "^[_a-zA-Z0-9][_a-zA-Z0-9-]*\$";
}

# Perl modules must return a value. Returning a value perl considers "truthy"
# signals that the module loaded successfully.
1;
