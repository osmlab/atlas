package ash_common;

use warnings;
use strict;

use Exporter qw(import);
use File::Basename;
use File::Spec;
use File::Path qw(make_path rmtree);
use File::Temp qw(tempdir tempfile);
use Cwd qw(abs_path);

# Export symbols: variables and subroutines
our @EXPORT = qw(
    MODULE_SUFFIX
    DEACTIVATED_SUFFIX
    DEACTIVATED_MODULE_SUFFIX
    MODULES_FOLDER
    PRESETS_FOLDER
    ACTIVE_INDEX_FILE
    ACTIVE_INDEX_PATH
    LOG4J_FOLDER
    LOG4J_FILE
    LOG4J_FILE_PATH
    ACTIVATED
    DEACTIVATED
    GOOD_SYMLINK
    BROKEN_SYMLINK
    REAL_FILE
    JAVA_NO_COLOR_SENTINEL
    JAVA_COLOR_STDOUT
    JAVA_NO_COLOR_STDOUT
    JAVA_COLOR_STDERR
    JAVA_NO_COLOR_STDERR
    JAVA_MARKER_SENTINEL
    create_data_directory
    reset_log4j
    display_and_exit
    getopt_failure_and_exit
    is_no_colors
    is_no_colors_stdout
    is_no_colors_stderr
    error_output
    warn_output
    prompt
    prompt_yn
    get_pager
    get_editor
    get_subcommand_to_class_hash
    get_subcommand_to_description_hash
    get_module_to_status_hash
    get_module_to_symlink_hash
    get_module_to_target_hash
    get_activated_modules
    read_loglevel_from_file
    read_logstream_from_file
    replace_loglevel_in_file
    replace_logstream_in_file
    perform_uninstall
    perform_activate
    perform_deactivate
    generate_active_module_index
    remove_active_module_index
    save_preset
    remove_preset
    remove_all_presets_for_command
    all_presets
    show_preset
    edit_preset
    copy_preset
    apply_preset_or_exit
    read_preset
    string_starts_with
    is_dir_empty
    ansi_red
    ansi_green
    ansi_magenta
    ansi_bold
    ansi_reset
    ansi_begin_underln
    ansi_end_underln
    terminal_width
);

our $MODULE_SUFFIX = '.jar';
our $DEACTIVATED_SUFFIX = '.deactivated';
our $DEACTIVATED_MODULE_SUFFIX = $MODULE_SUFFIX . $DEACTIVATED_SUFFIX;
our $MODULES_FOLDER = 'modules';
our $PRESETS_FOLDER = 'presets';
our $ACTIVE_INDEX_FILE = 'active_module_index';
our $ACTIVE_INDEX_PATH = File::Spec->catfile($MODULES_FOLDER, $ACTIVE_INDEX_FILE);
our $LOG4J_FOLDER = 'log4j';
our $LOG4J_FILE = 'log4j.properties';
our $LOG4J_FILE_PATH = File::Spec->catfile($LOG4J_FOLDER, $LOG4J_FILE);

our $ACTIVATED = 1;
our $DEACTIVATED = 0;

our $GOOD_SYMLINK = 1;
our $BROKEN_SYMLINK = -1;
our $REAL_FILE = 0;

our $JAVA_COLOR_STDOUT = "___atlas-shell-tools_color_stdout_SPECIALARGUMENT___";
our $JAVA_NO_COLOR_STDOUT = "___atlas-shell-tools_nocolor_stdout_SPECIALARGUMENT___";
our $JAVA_COLOR_STDERR = "___atlas-shell-tools_color_stderr_SPECIALARGUMENT___";
our $JAVA_NO_COLOR_STDERR = "___atlas-shell-tools_nocolor_stderr_SPECIALARGUMENT___";
our $JAVA_MARKER_SENTINEL = "___atlas-shell-tools_LAST_ARG_MARKER_SENTINEL___";

# Use ASCII record separator as delimiter.
# This is also defined in Atlas class OSMSubcommandTablePrinter.
my $INDEX_DELIMITER = "\x1E";

# The default setting for the log4j file.
my $DEFAULT_LOG4J_CONTENTS = "log4j.rootLogger=ERROR, stderr
# DO NOT REMOVE/MODIFY THE ABOVE LINE OR ANY OF THIS FILE
# use 'ash-config log' subcommand to manage the log configuration

# Direct log messages to stderr
log4j.appender.stderr=org.apache.log4j.ConsoleAppender
log4j.appender.stderr.Target=System.err
log4j.appender.stderr.layout=org.apache.log4j.PatternLayout
log4j.appender.stderr.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n
";

# A header for the preset edit screen.
my $PRESET_EDIT_HEADER = "# Each line in this file will become a discrete ARGV element. For this
# reason, please use '--option=optionArgument' syntax (note the '=')
# for option arguments. Otherwise, your preset will not function properly.
# Also note that all non-option arguments will be dropped, and any line
# beginning with a '#' will be ignored.
#
# If you're stuck, hit <ESC> then type :q!<Enter> to abort the edit.
# To save your changes, hit <ESC> then type :wq<Enter>";

# The Java class that creates the active_module_index
my $TABLE_PRINTER_CLASS = "org.openstreetmap.atlas.utilities.command.OSMSubcommandTablePrinter";

my $no_colors_stdout = is_no_colors_stdout();
my $red_stdout = $no_colors_stdout ? "" : ansi_red();
my $green_stdout = $no_colors_stdout ? "" : ansi_green();
my $magenta_stdout = $no_colors_stdout ? "" : ansi_magenta();
my $bold_stdout = $no_colors_stdout ? "" : ansi_bold();
my $bunl_stdout = $no_colors_stdout ? "" : ansi_begin_underln();
my $eunl_stdout = $no_colors_stdout ? "" : ansi_end_underln();
my $reset_stdout = $no_colors_stdout ? "" : ansi_reset();

my $no_colors_stderr = is_no_colors_stderr();
my $red_stderr = $no_colors_stderr ? "" : ansi_red();
my $green_stderr = $no_colors_stderr ? "" : ansi_green();
my $magenta_stderr = $no_colors_stderr ? "" : ansi_magenta();
my $bold_stderr = $no_colors_stderr ? "" : ansi_bold();
my $reset_stderr = $no_colors_stderr ? "" : ansi_reset();

# Create the XDG data directory. Defaults to "$HOME/.local/share" but respects
# the XDG_DATA_HOME env variable if set.
# Params: none
# Return: the newly set data directory
sub create_data_directory {
    # The directory for data storage. Client code must access this variable thru
    # create_data_directory(), which optionally modifies this variable based on the
    # XDG_DATA_HOME environment variable.
    my $data_directory = "$ENV{HOME}/.local/share";

    if (defined $ENV{XDG_DATA_HOME}) {
        $data_directory = $ENV{XDG_DATA_HOME};
    }
    $data_directory = File::Spec->catfile($data_directory, 'ash');
    my $full_log4j_path = File::Spec->catfile($data_directory, $LOG4J_FOLDER);
    my $full_module_path = File::Spec->catfile($data_directory, $MODULES_FOLDER);
    my $full_presets_path = File::Spec->catfile($data_directory, $PRESETS_FOLDER);
    make_path("$data_directory", "$full_module_path", "$full_log4j_path", "$full_presets_path", {
        verbose => 0,
        mode => 0755
    });
    my $log4j_file = File::Spec->catfile($data_directory, $LOG4J_FOLDER, $LOG4J_FILE);
    unless (-f $log4j_file) {
        reset_log4j($data_directory);
    }
    return $data_directory;
}

# Reset the log4j file to default.
# Params:
#   $ash_path: the path to the ash data folder
# Return: none
sub reset_log4j {
    my $ash_path = shift;
    my $log4j_file = File::Spec->catfile($ash_path, $LOG4J_FOLDER, $LOG4J_FILE);
    open my $file_handle, '>', "$log4j_file";
    print $file_handle $DEFAULT_LOG4J_CONTENTS;
    close $file_handle;
}

# Display the given message and exit. Default behaviour is to use pagination,
# but this can be disabled with the "skip_paging" parameter.
# Params:
#   $message: the message text
#   $skip_paging: a boolean value that determines if the pager should be skipped
# Return: none
sub display_and_exit {
    my $message = shift;
    my $skip_paging = shift;

    unless (defined $skip_paging) {
        $skip_paging = 0;
    }

    my $pager_command = get_pager();
    unless (defined $pager_command) {
        $skip_paging = 1;
    }

    if ($skip_paging) {
        print "$message";
    } else {
        open PAGER, "|${pager_command}" or die $!;
        print PAGER "$message";
    }

    exit 0;
}

# Print a failure message for getopt failures.
# Params:
#   $program_name: the name of the failing program
#   $subcommand_name: the optional name of the subcommand
# Returns: none
sub getopt_failure_and_exit {
    my $program_name = shift;
    my $subcommand_name = shift;
    if (defined $subcommand_name) {
        print STDERR "Try '${bold_stderr}${program_name} ${subcommand_name} --help${reset_stderr}' for more information.\n";
    } else {
        print STDERR "Try '${bold_stderr}${program_name} --help${reset_stderr}' for more information.\n";
    }
    exit 1;
}

# Determine if we should disable text/color formatting for output. Various
# conditions are checked, and if none of them trigger then we can use colors!
# We also make one check for explicit use of colors, to allow a case where a
# user has set NO_COLOR, but would like to make an exception for ash.
# Params: none
# Return: 1 if no colors, 0 otherwise
sub is_no_colors {
    # check for dumb
    # TODO need to check for xterm too?
    if ($ENV{'TERM'} eq "dumb") {
        return 1;
    }

    # explicitly use colors for ash
    if (exists $ENV{'ASH_USE_COLOR'}) {
        return 0;
    }

    # respect the NO_COLOR env var
    if (exists $ENV{'NO_COLOR'}) {
        return 1;
    }

    # respect the ASH_NO_COLOR env var
    if (exists $ENV{'ASH_NO_COLOR'}) {
        return 1;
    }

    return 0;
}

# Same as the is_no_colors check, but also looks to see if stdout is a TTY.
# Params: none
# Return: 1 if no colors, 0 otherwise
sub is_no_colors_stdout {
    my $no_colors = is_no_colors();
    my $is_stdout_tty = -t STDOUT ? 1 : 0;
    return 1 if $no_colors || !$is_stdout_tty;
    return 0;
}

# Same as the is_no_colors check, but also looks to see if stderr is a TTY.
# Params: none
# Return: 1 if no colors, 0 otherwise
sub is_no_colors_stderr {
    my $no_colors = is_no_colors();
    my $is_stderr_tty = -t STDERR ? 1 : 0;
    return 1 if $no_colors || !$is_stderr_tty;
    return 0;
}

# Print a command error message. The format is:
# "$command: error: $message"
# This routine will use colors/formatting if allowed by environment settings.
# This routine will place output on stderr.
# Params:
#   $command: the name of the command
#   $message: the message
# Return: none
sub error_output {
    my $command = shift;
    my $message = shift;
    my $no_colors = is_no_colors_stderr();
    my $red = $no_colors ? "" : ansi_red();
    my $bold = $no_colors ? "" : ansi_bold();
    my $reset = $no_colors ? "" : ansi_reset();

    print STDERR "$command: ${red}${bold}error:${reset} $message\n"
}

# Print a command warn message. The format is:
# "$command: warn: $message"
# This routine will use colors/formatting if allowed by environment settings.
# This routine will place output on stderr.
# Params:
#   $command: the name of the command
#   $message: the message
# Return: none
sub warn_output {
    my $command = shift;
    my $message = shift;
    my $no_colors = is_no_colors_stderr();
    my $magenta = $no_colors ? "" : ansi_magenta();
    my $bold = $no_colors ? "" : ansi_bold();
    my $reset = $no_colors ? "" : ansi_reset();

    print STDERR "$command: ${magenta}${bold}warn:${reset} $message\n"
}

# Prompt the user for input.
# Params:
#   $query: the prompt string
# Return: the input
sub prompt {
    my $query = shift; # take a prompt string as argument
    local $| = 1; # activate autoflush to immediately show the prompt
    print $query;
    my $answer = <STDIN>;
    if (defined $answer) {
        chomp($answer);
    }
    return $answer;
}

# Prompt the user for y/n confirmation.
# Params:
#   $query: the prompt string
# Return: 1 if user gave y, 0 if user gave n
sub prompt_yn {
    my $query = shift;
    my $answer = prompt("$query\n[y/N]: ");
    unless (defined $answer) {
        return 0;
    }
    return 1 if lc($answer) eq 'y';
    return 0;
}

# Get a pager command capable of displaying formatting codes. Checks the value
# of the PAGER env variable, and uses that instead if it points to a pager.
# If no valid pager can be found, returns 'undef'.
# Params: none
# Return: the pager command, or undef if no valid command is found
sub get_pager {
    my $pager_command;
    my $exitcode;

    if (defined $ENV{PAGER}) {
        $pager_command = `which $ENV{PAGER}`;
        chomp $pager_command;
        $exitcode = $? >> 8;
    } else {
        $pager_command = `which less`;
        chomp $pager_command;
        $exitcode = $? >> 8;

        # Options (see less(1) for more info):
        # -c -> clear the screen before displaying
        # -S -> chop long lines instead of wrapping them
        # -R -> actually display ANSI "color" control sequences as colors/formatting
        # -M -> use verbose prompt
        # -i -> searches ignore case
        # -s -> squeeze consecutive blank lines
        # TODO consider -FX options here?
        # https://unix.stackexchange.com/questions/107315/less-quit-if-one-screen-without-no-init
        $pager_command = $pager_command . ' -cSRMis';
    }
    
    if ($exitcode == 0) {
        return $pager_command;
    }

    # This has pitfalls, but it should be OK in this case
    # https://perlmaven.com/how-to-return-undef-from-a-function
    return undef;
}

# Get an editor command capable of displaying and editing a text file. Checks
# the value of the EDITOR env variable, and uses that instead if it points to an
# editor. If no valid pager can be found, returns 'undef'.
# Params: none
# Return: the editor command, or undef if no valid command is found
sub get_editor {
    my $editor_command;
    my $exitcode;

    if (defined $ENV{EDITOR}) {
        $editor_command = `which $ENV{EDITOR}`;
        chomp $editor_command;
        $exitcode = $? >> 8;
    } else {
        $editor_command = `which vim`;
        chomp $editor_command;
        $exitcode = $? >> 8;
        $editor_command = $editor_command . ' +';
    }

    if ($exitcode == 0) {
        return $editor_command;
    }

    # this has pitfalls, but it should be OK in this case
    return undef;
}

# Get a hash that maps subcommand names to their respective classes. The hash is
# computed from the current active module index.
# Params:
#   $ash_path: the path to the ash data folder
# Return: a hash of all subcommands to their classes.
sub get_subcommand_to_class_hash {
    my $ash_path = shift;
    my $index_path = File::Spec->catfile($ash_path, $ACTIVE_INDEX_PATH);
    open my $index_fileIN, '<', $index_path or die "could not read index from path $index_path";
    my %subcommand_to_class = ();

    while (<$index_fileIN>) {
        my $line = $_;
        chomp $line;
        my @line_elements = split($INDEX_DELIMITER, $line);
        $subcommand_to_class{$line_elements[0]} = $line_elements[1];
    }

    return %subcommand_to_class;
}

# Get a hash that maps subcommand names to their respective descriptions. The
# hash is computed from the current active module index.
# Params:
#   $ash_path: the path to the ash data folder
# Return: a hash of all subcommands to their descriptions.
sub get_subcommand_to_description_hash {
    my $ash_path = shift;
    my $index_path = File::Spec->catfile($ash_path, $ACTIVE_INDEX_PATH);
    open my $index_fileIN, '<', $index_path or die "could not read index from path $index_path";
    my %subcommand_to_description = ();

    while (<$index_fileIN>) {
        my $line = $_;
        chomp $line;
        my @line_elements = split($INDEX_DELIMITER, $line);
        $subcommand_to_description{$line_elements[0]} = $line_elements[2];
    }

    return %subcommand_to_description;
}

# Get a hash that maps all present module names to their activation status.
# Activated modules are mapped to ACTIVATED, while deactivated modules are mapped
# to DEACTIVATED.
# Params:
#   $ash_path: the path to the ash data folder
# Return: a hash of all modules to their activation status.
sub get_module_to_status_hash {
    my $ash_path = shift;
    my @find_command=(
        "find", "${ash_path}/${MODULES_FOLDER}",
        "-maxdepth", "1",
        "(", "-name", "*$MODULE_SUFFIX", "-o", "-name", "*$DEACTIVATED_SUFFIX", ")",
        "-print0"
    );
    my %modules = ();
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
        $module = basename($module);

        my $module_activated;

        # TODO: figure out how to interpolate $DEACTIVATED_SUFFIX into this regex
        if ($module =~ /.*\.deactivated$/) {
            $module_activated = $DEACTIVATED;
        } else {
            $module_activated = $ACTIVATED;
        }
        $module =~ s{$MODULE_SUFFIX}{};
        $module =~ s{$DEACTIVATED_SUFFIX}{};
        $modules{$module} = $module_activated;
    }

    return %modules;
}

# Get a hash that maps all present module names to their symlink state.
# Symlinked modules are mapped to GOOD_SYMLINK, regular modules are mapped to
# REAL_FILE. Broken symlink modules are mapped to BROKEN_SYMLINK.
# Params:
#   $ash_path: the path to the ash data folder
# Return: a hash of all modules to their symlink state.
sub get_module_to_symlink_hash {
    my $ash_path = shift;
    my @find_command=(
        "find", "${ash_path}/${MODULES_FOLDER}",
        "-maxdepth", "1",
        "(", "-name", "*$MODULE_SUFFIX", "-o", "-name", "*$DEACTIVATED_SUFFIX", ")",
        "-print0"
    );
    my %modules = ();
    open FIND, "-|", @find_command;
    local $/ = "\0";
    while (<FIND>) {
        # FIND command is printing full paths, we just want the basename.
        # Also, we must chomp to remove the terminating null byte left over from
        # the '-print0' flag given to 'find'.
        my $module = $_;
        chomp $module;
        $module = basename($module);

        my $module_islink;

        if (-l $_) {
            if (lstat $_ and not stat $_) {
                $module_islink = $BROKEN_SYMLINK;
            } else {
                $module_islink = $GOOD_SYMLINK;
            }
        } else {
            $module_islink = $REAL_FILE;
        }
        $module =~ s{$MODULE_SUFFIX}{};
        $module =~ s{$DEACTIVATED_SUFFIX}{};
        $modules{$module} = $module_islink;
    }

    return %modules;
}

# Get a hash that maps all present module names to their symlink target.
# If a module is not a symlink, it maps to an empty string.
# Params:
#   $ash_path: the path to the ash data folder
# Return: a hash of all modules to their symlink target.
sub get_module_to_target_hash {
    my $ash_path = shift;
    my @find_command=(
        "find", "${ash_path}/${MODULES_FOLDER}",
        "-maxdepth", "1",
        "(", "-name", "*$MODULE_SUFFIX", "-o", "-name", "*$DEACTIVATED_SUFFIX", ")",
        "-print0"
    );
    my %modules = ();
    open FIND, "-|", @find_command;
    local $/ = "\0";
    while (<FIND>) {
        # FIND command is printing full paths, we just want the basename.
        # Also, we must chomp to remove the terminating null byte left over from
        # the '-print0' flag given to 'find'.
        my $module = $_;
        chomp $module;
        $module = basename($module);

        my $module_target;

        if (-l $_) {
            $module_target = readlink $_;
        } else {
            $module_target = '';
        }
        $module =~ s{$MODULE_SUFFIX}{};
        $module =~ s{$DEACTIVATED_SUFFIX}{};
        $modules{$module} = $module_target;
    }

    return %modules;
}

# Get an array of all active modules, computed from the modules hash returned by
# the 'get_module_to_status_hash' subroutine.
# Params:
#   $modules: a reference to the module hash returned by 'get_module_to_status_hash'
# Return: an array of all active modules
sub get_activated_modules {
    my $modules_ref = shift;
    my %modules = %{$modules_ref};
    my @activated_modules = ();

    foreach my $module (keys %modules) {
        if ($modules{$module} == $ACTIVATED) {
            push @activated_modules, $module;
        }
    }

    return @activated_modules;
}

# Read the log level from a given logfile. The log file should be a log4j file
# where the first line looks like:
#    log4j.rootLogger=LEVEL, STREAM
#Params:
#   $logfile_path: the path to the log4j file
# Return: the string level, eg. DEBUG, WARN, ERROR, etc.
sub read_loglevel_from_file {
    my $logfile_path = shift;

    # Grab the first line and chomp the trailing newline.
    open my $logfile, '<', $logfile_path or die "could not read logfile from path $logfile_path";
    my $firstline = <$logfile>;
    chomp $firstline;
    close $logfile;

    # firstline should now look like
    # log4j.rootLogger=LEVEL, STREAM
    my @split1 = split('=', $firstline);
    my @split2 = split(',', $split1[1]);
    my $level = $split2[0];
    # remove leading and trailing whitespace
    $level =~ s/^\s+|\s+$//g;

    return $level;
}

# Read the log stream from a given logfile. The log file should be a log4j file
# where the first line looks like:
#    log4j.rootLogger=LEVEL, STREAM
#Params:
#   $logfile_path: the path to the log4j file
# Return: the string stream, eg. stdout or stderr
sub read_logstream_from_file {
    my $logfile_path = shift;

    # Grab the first line and chomp the trailing newline.
    open my $logfile, '<', $logfile_path or die "could not read logfile from path $logfile_path";
    my $firstline = <$logfile>;
    chomp $firstline;
    close $logfile;

    # firstline should now look like
    # log4j.rootLogger=LEVEL, STREAM
    my @split1 = split('=', $firstline);
    my @split2 = split(',', $split1[1]);
    my $stream = $split2[1];
    # remove leading and trailing whitespace
    $stream =~ s/^\s+|\s+$//g;

    return $stream;
}

# Replace the log level in a given logfile. The log file should be a log4j file
# where the first line looks like:
#    log4j.rootLogger=LEVEL, stdout
# Note that this function will not validate that the new level is actually a
# valid log4j level. This is left up to the caller.
#Params:
#   $logfile_path: the path to the log4j file
#   $new_level: the new level
# Return: none
sub replace_loglevel_in_file {
    my $logfile_path = shift;
    my $new_level = shift;

    # read the old level that we are replacing
    my $old_level = read_loglevel_from_file($logfile_path);

    # create a backup of the old log4j file
    rename $logfile_path, $logfile_path . '.bak';
    open my $logfileIN, '<', $logfile_path . '.bak';
    open my $logfileOUT, '>', $logfile_path;

    # replace the level
    my $firstline = <$logfileIN>;
    $firstline =~ s/${old_level}/${new_level}/;
    print $logfileOUT $firstline;

    # write the rest of the log4j file
    while (<$logfileIN>) {
        print $logfileOUT $_;
    }

    close $logfileIN;
    close $logfileOUT;
}

# Replace the log stream in a given logfile. The log file should be a log4j file
# where the first line looks like:
#    log4j.rootLogger=LEVEL, STREAM
# This function will die if the provided stream is not 'stdout' or 'stderr'.
# This is necessary because we must compute stderr -> System.err and stdout ->
# System.out to properly update the log4j file.
#Params:
#   $logfile_path: the path to the log4j file
#   $new_stream: the new stream
# Return: none
sub replace_logstream_in_file {
    my $logfile_path = shift;
    my $new_stream = shift;

    # read the old stream that we are replacing
    my $old_stream = read_logstream_from_file($logfile_path);

    my $old_system;
    my $new_system;
    if ($new_stream eq 'stdout') {
        $old_system = 'System\.err';
        $new_system = 'System.out';
    } elsif ($new_stream eq 'stderr') {
        $old_system = 'System\.out';
        $new_system = 'System.err';
    } else {
        die "Invalid stream setting $new_stream, must be stdout or stderr";
    }

    # create a backup of the old log4j file
    rename $logfile_path, $logfile_path . '.bak';
    open my $logfileIN, '<', $logfile_path . '.bak';
    open my $logfileOUT, '>', $logfile_path;

    while (<$logfileIN>) {
        $_ =~ s/${old_stream}/${new_stream}/g;
        $_ =~ s/${old_system}/${new_system}/g;
        print $logfileOUT $_;
    }

    close $logfileIN;
    close $logfileOUT;
}

# Uninstall a module with a given name.
# Params:
#   $module_to_uninstall: the name of the module to uninstall
#   $ash_path: the path to the ash data folder
#   $program_name: the name of the running program
#   $quiet: suppress non-essential output
# Return: 1 on success, 0 on failure
sub perform_uninstall {
    my $module_to_uninstall = shift;
    my $ash_path = shift;
    my $program_name = shift;
    my $quiet = shift;

    my %modules = get_module_to_status_hash($ash_path);

    unless (exists $modules{$module_to_uninstall}) {
        error_output($program_name, "no such module ${bold_stderr}${module_to_uninstall}${reset_stderr}");
        return 0;
    }

    # try to remove the module
    my $modules_folder = File::Spec->catfile($ash_path, $MODULES_FOLDER);
    my $module_remove_path =
        File::Spec->catfile($modules_folder, $module_to_uninstall . $MODULE_SUFFIX);
    my $module_remove_path_deactivated =
        File::Spec->catfile($modules_folder, $module_to_uninstall . $DEACTIVATED_MODULE_SUFFIX);

    unlink $module_remove_path;
    unlink $module_remove_path_deactivated;
    unless ($quiet) {
        print "Module ${bold_stdout}${module_to_uninstall}${reset_stdout} uninstalled.\n";
    }

    return 1;
}

# Activate a module with a given name.
# Params:
#   $module_to_activate: the name of the module to activate
#   $ash_path: the path to the ash data folder
#   $program_name: the name of the running program
#   $quiet: suppress non-essential messages
# Return: 1 on success, 0 on failure
sub perform_activate {
    my $module_to_activate = shift;
    my $ash_path = shift;
    my $program_name = shift;
    my $quiet = shift;

    my %modules = get_module_to_status_hash($ash_path);

    unless (exists $modules{$module_to_activate}) {
        error_output($program_name, "no such module ${bold_stderr}${module_to_activate}${reset_stderr}");
        return 0;
    }

    my $status = $modules{$module_to_activate};
    if ($status == $ACTIVATED) {
        warn_output($program_name, "module ${bold_stderr}${module_to_activate}${reset_stderr} already activated");
        return 1;
    }

    # We made it here, so we are good to activate the module!
    # Effectively, this just means removing the DEACTIVATED_MODULE_SUFFIX
    my $module_path_nosuffix = File::Spec->catfile($ash_path, $MODULES_FOLDER, $module_to_activate);
    my $old_module_path = $module_path_nosuffix . $DEACTIVATED_MODULE_SUFFIX;
    my $new_module_path = $module_path_nosuffix . $MODULE_SUFFIX;

    rename $old_module_path, $new_module_path;
    my $exitcode = $? >> 8;
    if ($exitcode) {
        print STDERR "${red_stderr}${bold_stderr}Activation failed.${reset_stderr} Rename exited with $exitcode.\n";
        return 0;
    } else {
        ash_common::remove_active_module_index($ash_path, $program_name, $quiet);
        ash_common::generate_active_module_index($ash_path, $program_name, $quiet);
        unless ($quiet) {
            print "Module ${green_stdout}${bold_stdout}${module_to_activate}${reset_stdout} activated.\n";
        }
    }

    return 1;
}

# Deactivate a module with a given name.
# Params:
#   $module_to_deactivate: the name of the module to deactivate
#   $ash_path: the path to the ash data folder
#   $program_name: the name of the running program
#   $quiet: suppress non-essential messages
# Return: 1 on success, 0 on failure
sub perform_deactivate {
    my $module_to_deactivate = shift;
    my $ash_path = shift;
    my $program_name = shift;
    my $quiet = shift;

    my %modules = get_module_to_status_hash($ash_path);

    unless (exists $modules{$module_to_deactivate}) {
        error_output($program_name, "no such module ${bold_stderr}${module_to_deactivate}${reset_stderr}");
        return 0;
    }

    my $status = $modules{$module_to_deactivate};
    if ($status == $DEACTIVATED) {
        warn_output($program_name, "module ${bold_stderr}${module_to_deactivate}${reset_stderr} already deactivated");
        return 0;
    }

    # We made it here, so we are good to deactivate the module!
    # Effectively, this just means adding the DEACTIVATED_MODULE_SUFFIX
    my $module_path_nosuffix = File::Spec->catfile($ash_path, $MODULES_FOLDER, $module_to_deactivate);
    my $old_module_path = $module_path_nosuffix . $MODULE_SUFFIX;
    my $new_module_path = $module_path_nosuffix . $DEACTIVATED_MODULE_SUFFIX;

    rename $old_module_path, $new_module_path;
    my $exitcode = $? >> 8;
    if ($exitcode) {
        print STDERR "${red_stderr}${bold_stderr}Deactivation failed.${reset_stderr} Rename exited with $exitcode.\n";
            return 0;
    } else {
        unless ($quiet) {
            print "Module ${bold_stdout}${module_to_deactivate}${reset_stdout} deactivated.\n";
        }
    }

    return 1;
}

# Create the active module index for the currently activated module.
# Params:
#   $ash_path: the path to the ash data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
# Return: 1 on success, 0 on failure
sub generate_active_module_index {
    my $ash_path = shift;
    my $program_name = shift;
    my $quiet = shift;

    my $full_index_path = File::Spec->catfile($ash_path, $ACTIVE_INDEX_PATH);
    my $full_path_to_modules_folder = File::Spec->catfile($ash_path, $ash_common::MODULES_FOLDER, '*');
    my $java_command = "java -Xms2G -Xmx2G ".
                   "-cp \"${full_path_to_modules_folder}\" ".
                   "-Dlog4j.rootLogger=ERROR ".
                   "${TABLE_PRINTER_CLASS}";

    my %modules = ash_common::get_module_to_status_hash($ash_path);
    my @activated_modules = get_activated_modules(\%modules);

    if (scalar @activated_modules == 0) {
        ash_common::error_output($program_name, 'could not generate index');
        print STDERR "No active modules found\n";
        print STDERR "Try '${bold_stderr}ash-config list${reset_stderr}' to see all installed modules.\n";
        print STDERR "Then try '${bold_stderr}ash-config activate <module>${reset_stderr}' to activate.\n";
        return 0;
    }

    my $output = `$java_command`;
    my $exitcode = $? >> 8;
    unless ($exitcode == 0) {
        ash_common::error_output($program_name, 'could not generate index');
        return 0;
    }
    open my $file_handle, '>', $full_index_path;
    print $file_handle $output;
    close $file_handle;

    unless ($quiet) {
        print "New index successfully generated.\n";
    }
    return 1;
}

# Delete the active module index.
# Params:
#   $ash_path: the path to the ash data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
# Return: 1 on success, 0 on failure
sub remove_active_module_index {
    my $ash_path = shift;
    my $program_name = shift;
    my $quiet = shift;

    my $full_index_path = File::Spec->catfile($ash_path, $ACTIVE_INDEX_PATH);
    unlink $full_index_path;

    unless ($quiet) {
        print "Cleared index.\n";
    }
}

# Save a preset for a given command.
# Params:
#   $ash_path: the path to the ash data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output output
#   $preset: the name of the preset
#   $command: the name of the command
#   $argv_ref: a reference to an array containing all the options and args
# Return: 1 on success, 0 on failure
sub save_preset {
    my $ash_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $preset = shift;
    my $command = shift;
    my $argv_ref = shift;

    my @argv = @{$argv_ref};

    my $preset_subfolder = File::Spec->catfile($ash_path, $PRESETS_FOLDER, $command);
    make_path("$preset_subfolder", {
        verbose => 0,
        mode => 0755
    });
    my $preset_file = File::Spec->catfile($preset_subfolder, $preset);

    if (-f $preset_file) {
        error_output($program_name, "preset ${bold_stderr}${preset}${reset_stderr} already exists for ${bold_stderr}${command}${reset_stderr}");
        print STDERR "Try '${bold_stderr}${program_name} --cfgpreset remove:${preset} ${command}${reset_stderr}' to remove.\n";
        print STDERR "Try '${bold_stderr}${program_name} --cfgpreset edit:${preset} ${command}${reset_stderr}' to edit interactively.\n";
        return 0;
    }

    my @detected_options = ();
    foreach my $arg (@argv) {
        # treat '-' as a regular argument
        if ($arg eq '-') {
            warn_output($program_name, "discarding non-option arg \'${bold_stderr}$arg${reset_stderr}\'");
            next;
        }

        # stop processing once we see '--'
        if ($arg eq '--') {
            last;
        }

        # detected an option
        if (string_starts_with($arg, '--') || string_starts_with($arg, '-')) {
            push @detected_options, $arg;
        } else {
            warn_output($program_name, "discarding non-option arg \'${bold_stderr}$arg${reset_stderr}\'");
        }
    }

    if (scalar @detected_options == 0) {
        error_output($program_name, "cannot save an empty preset");
        return 0;
    }

    print "Command ${bold_stdout}${command}${reset_stdout} preset ${bold_stdout}${preset}${reset_stdout}:\n";
    print "\n${bunl_stdout}Preset ARGV${eunl_stdout}${reset_stdout}\n";
    open my $file_handle, '>', "$preset_file";
    foreach my $option (@detected_options) {
        print "${bold_stdout}${option}${reset_stdout}\n";
        print $file_handle "${option}\n";
    }
    close $file_handle;

    print "\nPreset ${bold_stdout}${preset}${reset_stdout} saved for ${bold_stdout}${command}${reset_stdout}.\n";

    return 1;
}

# Remove a given preset for a given command.
# Params:
#   $ash_path: the path to the ash data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $preset: the name of the preset
#   $command: the name of the command
# Return: 1 on success, 0 on failure
sub remove_preset {
    my $ash_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $preset = shift;
    my $command = shift;

    my $preset_subfolder = File::Spec->catfile($ash_path, $PRESETS_FOLDER, $command);
    my $preset_file = File::Spec->catfile($preset_subfolder, $preset);

    unless (-f $preset_file) {
        error_output($program_name, "no such preset ${bold_stderr}${preset}${reset_stderr} for command ${bold_stderr}${command}${reset_stderr}");
        print STDERR "Try \'${bold_stderr}${program_name} --cfgpreset show ${command}${reset_stderr}\' to see presets for ${bold_stderr}${command}${reset_stderr}.\n";
        return 0;
    }

    unlink $preset_file;
    print "Removed preset ${bold_stdout}${preset}${reset_stdout} for ${bold_stdout}${command}${reset_stdout}.\n";

    if (is_dir_empty($preset_subfolder)) {
        rmdir($preset_subfolder);
    }

    return 1;
}

# Remove all presets for a given command.
# Params:
#   $ash_path: the path to the ash data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $command: the name of the command
# Return: 1 on success, 0 on failure
sub remove_all_presets_for_command {
    my $ash_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $command = shift;

    my $preset_subfolder = File::Spec->catfile($ash_path, $PRESETS_FOLDER, $command);

    unless (-d $preset_subfolder) {
        error_output($program_name, "no presets found for command ${bold_stderr}${command}${reset_stderr}");
        return 0;
    }

    rmtree($preset_subfolder);
    print "Removed all presets for ${bold_stdout}${command}${reset_stdout}.\n";

    return 1;
}

# List all presets for a given command.
# Params:
#   $ash_path: the path to the ash data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $command: the name of the command
# Return: 1 on success, 0 on failure
sub all_presets {
    my $ash_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $command = shift;

    my $preset_subfolder = File::Spec->catfile($ash_path, $PRESETS_FOLDER, $command);

    unless (-d $preset_subfolder) {
        error_output($program_name, "no presets found for ${bold_stderr}${command}${reset_stderr}.");
        return 0;
    }

    opendir my $presets_dir_handle, $preset_subfolder or die "Something went wrong opening dir: $!";
    my @presets = readdir $presets_dir_handle;
    close $presets_dir_handle;

    # we need to filter '.' and '..'
    my @filtered_presets = ();
    for my $found_preset (@presets) {
        unless ($found_preset eq '.' || $found_preset eq '..') {
            push @filtered_presets, $found_preset;
        }
    }

    if (scalar @filtered_presets == 0) {
        error_output($program_name, "no presets found for ${bold_stderr}${command}${reset_stderr}.");
        return 0;
    }

    print "Command ${bold_stdout}${command}${reset_stdout} presets:\n\n";
    for my $found_preset (sort {lc $a cmp lc $b} @filtered_presets) {
        print "${bold_stdout}${found_preset}${reset_stdout}\n";
    }
    print "\n";

    return 1;
}

# Show a given preset for a given command.
# Params:
#   $ash_path: the path to the ash data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $preset: the name of the preset
#   $command: the name of the command
# Return: 1 on success, 0 on failure
sub show_preset {
    my $ash_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $preset = shift;
    my $command = shift;

    my $preset_subfolder = File::Spec->catfile($ash_path, $PRESETS_FOLDER, $command);
    my $preset_file = File::Spec->catfile($preset_subfolder, $preset);

    unless (-f $preset_file) {
        error_output($program_name, "no such preset ${bold_stderr}${preset}${reset_stderr} for command ${bold_stderr}${command}${reset_stderr}");
        print STDERR "Try \'${bold_stderr}${program_name} --cfgpreset show ${command}${reset_stderr}\' to see presets for ${bold_stderr}${command}${reset_stderr}.\n";
        return 0;
    }

    my @presets_from_file = read_preset($ash_path, $program_name, $quiet, $preset, $command);
    print "Command ${bold_stdout}${command}${reset_stdout} preset ${bold_stdout}${preset}${reset_stdout}:\n";
    print "\n${bunl_stdout}Preset ARGV${eunl_stdout}${reset_stdout}\n";
    for my $preset_from_file (@presets_from_file) {
        print "${bold_stdout}${preset_from_file}${reset_stderr}\n";
    }
    print "\n";

    return 1;
}

# Edit a given preset for a given command.
# Params:
#   $ash_path: the path to the ash data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $preset: the name of the preset
#   $command: the name of the command
# Return: 1 on success, 0 on failure
sub edit_preset {
    my $ash_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $preset = shift;
    my $command = shift;

    my $preset_subfolder = File::Spec->catfile($ash_path, $PRESETS_FOLDER, $command);
    my $preset_file = File::Spec->catfile($preset_subfolder, $preset);

    my $creating_from_scratch = 0;
    unless (-f $preset_file) {
        $creating_from_scratch = 1;
    }

    my @presets_from_file = read_preset($ash_path, $program_name, $quiet, $preset, $command);

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
    } else {
        print $handle "# EDITING PRESET\n";
    }
    print $handle "# Preset: ${preset}\n";
    print $handle "# Command: ${command}\n#\n";
    print $handle "${PRESET_EDIT_HEADER}\n";
    foreach my $cur_preset (@presets_from_file) {
        print $handle "${cur_preset}\n";
    }
    close $handle;

    my $editor = get_editor();
    unless (defined $editor) {
        error_output($program_name, "could not obtain a valid editor");
        print STDERR "Please point the ${bold_stdout}EDITOR${reset_stdout} environment variable at a valid editor.\n";
        return 0;
    }

    system($editor . " $tmpfile");

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
            warn_output($program_name, "discarding non-option arg \'${bold_stderr}${line}${reset_stderr}\'");
            next;
        }

        # stop processing once we see '--'
        if ($line eq '--') {
            last;
        }

        # skip non-option arguments
        if (string_starts_with($line, '--') || string_starts_with($line, '-')) {
            print $stage_handle "$line\n";
        } else {
            warn_output($program_name, "discarding non-option arg \'${bold_stderr}${line}${reset_stderr}\'");
        }
    }
    close $handle;
    close $stage_handle;

    # Verify that the staged preset file is not empty
    if (-z $preset_stage_file) {
        error_output($program_name, "preset cannot be empty");
        return 0;
    }

    # TODO File::Copy vs system 'cp', see pitfalls: https://www.perlmonks.org/?node_id=582433
    system("cp $preset_stage_file $preset_file");
    close $tmpdir;
    show_preset($ash_path, $program_name, $quiet, $preset, $command);

    return 1;
}

# For a given command, copy a source preset to a destination preset.
# Params:
#   $ash_path: the path to the ash data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $src_preset: the source preset
#   $dest_preset: the destination preset
#   $command: the name of the command
# Return: 1 on success, 0 on failure
sub copy_preset {
    my $ash_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $src_preset = shift;
    my $dest_preset = shift;
    my $command = shift;

    my $preset_subfolder = File::Spec->catfile($ash_path, $PRESETS_FOLDER, $command);
    my $source_file = File::Spec->catfile($preset_subfolder, $src_preset);
    my $dest_file = File::Spec->catfile($preset_subfolder, $dest_preset);

    unless (-e $source_file) {
        error_output($program_name, "no such preset ${bold_stderr}${src_preset}${reset_stderr} for command ${bold_stderr}${command}${reset_stderr}");
        print STDERR "Try \'${bold_stderr}${program_name} --cfgpreset show ${command}${reset_stderr}\' to see presets for ${bold_stderr}${command}${reset_stderr}.\n";
        return 0;
    }

    if (-e $dest_file) {
        error_output($program_name, "preset ${bold_stderr}${dest_preset}${reset_stderr} already exists for ${bold_stderr}${command}${reset_stderr}");
        print STDERR "Try '${bold_stderr}${program_name} --cfgpreset remove:${dest_preset} ${command}${reset_stderr}' to remove.\n";
        return 0;
    }

    system("cp $source_file $dest_file");

    unless ($quiet) {
        print "Copied contents of preset ${bold_stdout}${src_preset}${reset_stdout} into new preset ${bold_stdout}${dest_preset}${reset_stdout}.\n";
    }

    return 1;
}

# Apply a preset for a given command. Returns an updated argv array with the
# preset applied.
# Params:
#   $ash_path: the path to the ash data folder
#   $program_name: the name of the calling program
#   $quiet: suppres non-essential output
#   $preset: the name of the preset
#   $command: the name of the command
#   $argv_ref: a reference to an array containing all the options and args
# Return: the updated argv array
sub apply_preset_or_exit {
    my $ash_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $preset = shift;
    my $command = shift;
    my $argv_ref = shift;

    my @argv = @{$argv_ref};

    my $preset_subfolder = File::Spec->catfile($ash_path, $PRESETS_FOLDER, $command);
    my $preset_file = File::Spec->catfile($preset_subfolder, $preset);
    unless (-f $preset_file) {
        error_output($program_name, "no such preset ${bold_stderr}${preset}${reset_stderr} for command ${bold_stderr}${command}${reset_stderr}");
        print STDERR "Try \'${bold_stderr}${program_name} --cfgpreset show ${command}${reset_stderr}\' to see presets for ${bold_stderr}${command}${reset_stderr}.\n";
        exit 1;
    }

    my @argv_from_presets = read_preset($ash_path, $program_name, $quiet, $preset, $command);
    my @final_argv = ();

    foreach my $preset_argv_elem (@argv_from_presets) {
        push @final_argv, $preset_argv_elem;
    }

    foreach my $argv_elem (@argv) {
        push @final_argv, $argv_elem;
    }

    return @final_argv;
}

# Read a preset for a given command. Returns the preset in an array
# Params:
#   $ash_path: the path to the ash data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $preset: the name of the preset
#   $command: the name of the command
# Return: the preset array, or an empty array on error
sub read_preset {
    my $ash_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $preset = shift;
    my $command = shift;

    my $preset_subfolder = File::Spec->catfile($ash_path, $PRESETS_FOLDER, $command);
    my $preset_file = File::Spec->catfile($preset_subfolder, $preset);

    unless (-f $preset_file) {
        return ();
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

# Check if a given string starts with a given prefix.
# Params:
#   $string: the string to search
#   $prefix: the prefix to check
# Return: 1 if $string starts with $prefix, 0 otherwise
sub string_starts_with {
    my $string = shift;
    my $prefix = shift;

    return substr($string, 0, length($prefix)) eq $prefix;
}

# Check if a given directory is empty.
# Params:
#   $dir: the directory to check
# Return: 1 if the directory is empty, 0 otherwise
sub is_dir_empty {
    my ($dir) = @_;

    opendir my $h, $dir
        or die "Cannot open directory: '$dir': $!";

    while ( defined (my $entry = readdir $h) ) {
        return unless $entry =~ /^[.][.]?\z/;
    }

    return 1;
}

sub ansi_red {
    return `tput setaf 1`;
}

sub ansi_green {
    return `tput setaf 2`;
}

sub ansi_magenta {
    return `tput setaf 5`;
}

sub ansi_bold {
    return `tput bold`;
}

sub ansi_blink {
    return `tput blink`;
}

sub ansi_reset {
    return `tput sgr0`;
}

sub ansi_begin_underln {
    return `tput smul`;
}

sub ansi_end_underln {
    return `tput rmul`;
}

sub terminal_width {
    # 'tput' returns a string with a trailing newline
    my $cols = `tput cols`;

    # Explicitly convert to an integer here, removing the newline
    # This allows allows for calling code to do math with the value
    return int($cols);
}

# Perl modules must return a value. Returning a value perl considers "truthy"
# signals that the module loaded successfully.
1;
