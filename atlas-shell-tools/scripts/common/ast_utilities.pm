package ast_utilities;

use warnings;
use strict;

use Exporter qw(import);
use File::Path qw(make_path);
use ast_tty;
use ast_log_subsystem;
use ast_preset_subsystem;

# Export symbols: variables and subroutines
our @EXPORT = qw(
    ATLAS_SHELL_TOOLS_VERSION
    COMMAND_PROGRAM
    CONFIG_PROGRAM
    JAVA_NO_COLOR_SENTINEL
    JAVA_COLOR_STDOUT
    JAVA_NO_COLOR_STDOUT
    JAVA_COLOR_STDERR
    JAVA_NO_COLOR_STDERR
    JAVA_USE_PAGER
    JAVA_NO_USE_PAGER
    JAVA_MARKER_SENTINEL
    create_data_directory
    display_and_exit
    getopt_failure_and_exit
    error_output
    warn_output
    prompt
    prompt_yn
    get_pager
    get_editor
    get_man
    string_starts_with
    is_dir_empty
);

our $ATLAS_SHELL_TOOLS_VERSION = "atlas-shell-tools version 0.0.1";

our $COMMAND_PROGRAM = 'atlas';
our $CONFIG_PROGRAM = 'atlas-config';

our $JAVA_COLOR_STDOUT = "___atlas-shell-tools_color_stdout_SPECIALARGUMENT___";
our $JAVA_NO_COLOR_STDOUT = "___atlas-shell-tools_nocolor_stdout_SPECIALARGUMENT___";
our $JAVA_COLOR_STDERR = "___atlas-shell-tools_color_stderr_SPECIALARGUMENT___";
our $JAVA_NO_COLOR_STDERR = "___atlas-shell-tools_nocolor_stderr_SPECIALARGUMENT___";
our $JAVA_USE_PAGER = "___atlas-shell-tools_use_pager_SPECIALARGUMENT___";
our $JAVA_NO_USE_PAGER = "___atlas-shell-tools_no_use_pager_SPECIALARGUMENT___";
our $JAVA_MARKER_SENTINEL = "___atlas-shell-tools_LAST_ARG_MARKER_SENTINEL___";

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
    $data_directory = File::Spec->catfile($data_directory, 'atlas-shell-tools');
    my $full_log4j_path = File::Spec->catfile($data_directory, $ast_log_subsystem::LOG4J_FOLDER);
    my $full_module_path = File::Spec->catfile($data_directory, $ast_module_subsystem::MODULES_FOLDER);
    my $full_presets_path = File::Spec->catfile($data_directory, $ast_preset_subsystem::PRESETS_FOLDER);
    my $default_namespace_path = File::Spec->catfile($data_directory, $ast_preset_subsystem::PRESETS_FOLDER, $ast_preset_subsystem::DEFAULT_NAMESPACE);
    make_path("$data_directory", "$full_module_path", "$full_log4j_path",
              "$full_presets_path", "$default_namespace_path", {
        verbose => 0,
        mode => 0755
    });
    my $log4j_file = File::Spec->catfile($data_directory, $ast_log_subsystem::LOG4J_FOLDER, $ast_log_subsystem::LOG4J_FILE);
    unless (-f $log4j_file) {
        ast_log_subsystem::reset_log4j($data_directory);
    }
    my $namespace_file = File::Spec->catfile($data_directory, $ast_preset_subsystem::NAMESPACE_PATH);
    unless (-f $namespace_file) {
        ast_preset_subsystem::reset_namespace($data_directory);
    }
    return $data_directory;
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
    }
    else {
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
    }
    else {
        print STDERR "Try '${bold_stderr}${program_name} --help${reset_stderr}' for more information.\n";
    }
    exit 1;
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
    my $no_colors = ast_tty::is_no_colors_stderr();
    my $red = $no_colors ? "" : ast_tty::ansi_red();
    my $bold = $no_colors ? "" : ast_tty::ansi_bold();
    my $reset = $no_colors ? "" : ast_tty::ansi_reset();

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
    my $no_colors = ast_tty::is_no_colors_stderr();
    my $magenta = $no_colors ? "" : ast_tty::ansi_magenta();
    my $bold = $no_colors ? "" : ast_tty::ansi_bold();
    my $reset = $no_colors ? "" : ast_tty::ansi_reset();

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
        if ($ENV{PAGER} eq '') {
            return undef;
        }
        $pager_command = `which $ENV{PAGER}`;
        # Make double sure we found a valid command. One some OS's,
        # the 'which' binary does not return an exit status.
        if ($pager_command eq '') {
            return undef;
        }
        chomp $pager_command;
        if ($pager_command eq '') {
            return undef;
        }
        $exitcode = $? >> 8;
    }
    else {
        $pager_command = `which less`;
        # Make double sure we found a valid command. One some OS's,
        # the 'which' binary does not return an exit status.
        if ($pager_command eq '') {
            return undef;
        }
        chomp $pager_command;
        if ($pager_command eq '') {
            return undef;
        }
        $exitcode = $? >> 8;

        # Options (see less(1) for more info):
        # -c -> clear the screen before displaying
        # -S -> chop long lines instead of wrapping them
        # -R -> actually display ANSI "color" control sequences as colors/formatting
        # -M -> use verbose prompt
        # -i -> searches ignore case
        # -s -> squeeze consecutive blank lines
        # TODO consider -F, -X options here?
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
        if ($ENV{EDITOR} eq '') {
            return undef;
        }
        $editor_command = `which $ENV{EDITOR}`;
        # Make double sure we found a valid command. One some OS's,
        # the 'which' binary does not return an exit status.
        if ($editor_command eq '') {
            return undef;
        }
        chomp $editor_command;
        if ($editor_command eq '') {
            return undef;
        }
        $exitcode = $? >> 8;
    }
    else {
        $editor_command = `which vim`;
        # Make double sure we found a valid command. One some OS's,
        # the 'which' binary does not return an exit status.
        if ($editor_command eq '') {
            return undef;
        }
        chomp $editor_command;
        if ($editor_command eq '') {
            return undef;
        }
        $exitcode = $? >> 8;
        $editor_command = $editor_command . ' +';
    }

    if ($exitcode == 0) {
        return $editor_command;
    }

    # This has pitfalls, but it should be OK in this case
    # https://perlmaven.com/how-to-return-undef-from-a-function
    return undef;
}

# Get a man command capable of displaying some desired manpage. Checks
# Params:
#   $skip_paging: optionally disable paging for man
# Return: the man command array, or undef if no valid command is found
sub get_man {
    my $skip_paging = shift;

    my $man_command = `which man`;
    my $exitcode = $? >> 8;
    my @command = ();

    # Make double sure we found a valid command. One some OS's,
    # the 'which' binary does not return an exit status.
    if ($man_command eq '') {
        return ();
    }
    chomp $man_command;
    if ($man_command eq '') {
        return ();
    }

    push @command, $man_command;

    if ($skip_paging) {
        push @command, "-P";
        push @command, "cat";
    }

    if ($exitcode == 0) {
        return @command;
    }

    return ();
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

    while (defined(my $entry = readdir $h)) {
        return unless $entry =~ /^[.][.]?\z/;
    }

    return 1;
}

# Perl modules must return a value. Returning a value perl considers "truthy"
# signals that the module loaded successfully.
1;
