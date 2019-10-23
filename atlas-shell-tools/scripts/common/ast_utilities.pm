package ast_utilities;

use warnings;
use strict;

use Exporter qw(import);
use File::Path qw(make_path);
use List::Util qw(min);
use ast_tty;
use ast_log_subsystem;
use ast_preset_subsystem;
use ast_repo_subsystem;

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
    verify_environment_or_exit
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
    levenshtein
    read_command_output
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

my $integrity_file = ".atlas-shell-tools-integrity-file";

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

# Ensure that the necessary environment variables are configured. If not,
# exit with an error.
# Params: none
# Return: none
sub verify_environment_or_exit {
    unless (defined $ENV{HOME}) {
        print STDERR "Error: HOME environment variable is not set\n";
        exit 1;
    }

    unless (-d $ENV{HOME}) {
        print STDERR "Error: the directory referenced by HOME does not exist\n";
        exit 1;
    }

    unless (-w $ENV{HOME}) {
        print STDERR "Error: the directory referenced by HOME is not writable\n";
        exit 1;
    }

    unless (defined $ENV{ATLAS_SHELL_TOOLS_HOME}) {
        print STDERR "Error: ATLAS_SHELL_TOOLS_HOME environment variable is not set\n";
        exit 1;
    }

    unless (-f "$ENV{ATLAS_SHELL_TOOLS_HOME}/${integrity_file}") {
        print STDERR "Error: ATLAS_SHELL_TOOLS_HOME environment variable is not a valid installation\n";
        exit 1
    }
}

# Create the XDG data directory. Defaults to "$HOME/.local/share" but respects
# the XDG_DATA_HOME env variable if set.
# Params: none
# Return: the newly set data directory
sub create_data_directory {
    # The directory for data storage. Client code must access this variable thru
    # create_data_directory(), which optionally modifies this variable based on the
    # XDG_DATA_HOME environment variable.
    my $data_directory = "$ENV{HOME}/.local/share";

    # Respect XDG_DATA_HOME per the XDG Base Directory specification
    # https://standards.freedesktop.org/basedir-spec/basedir-spec-latest.html
    if (defined $ENV{XDG_DATA_HOME}) {
        $data_directory = $ENV{XDG_DATA_HOME};
    }

    # Create data subdirectories if necessary
    $data_directory = File::Spec->catfile($data_directory, 'atlas-shell-tools');
    my $full_log4j_path = File::Spec->catfile($data_directory, $ast_log_subsystem::LOG4J_FOLDER);
    my $full_module_path = File::Spec->catfile($data_directory, $ast_module_subsystem::MODULES_FOLDER);
    my $full_presets_path = File::Spec->catfile($data_directory, $ast_preset_subsystem::PRESETS_FOLDER);
    my $default_namespace_path = File::Spec->catfile($data_directory, $ast_preset_subsystem::PRESETS_FOLDER, $ast_preset_subsystem::DEFAULT_NAMESPACE);
    my $full_repos_path = File::Spec->catfile($data_directory, $ast_repo_subsystem::REPOS_FOLDER);
    make_path("$data_directory", "$full_module_path", "$full_log4j_path",
        "$full_presets_path", "$default_namespace_path", "$full_repos_path", {
            verbose => 0,
            mode    => 0755
        });

    # reset the log4j file if it is missing
    my $log4j_file = File::Spec->catfile($data_directory, $ast_log_subsystem::LOG4J_FOLDER, $ast_log_subsystem::LOG4J_FILE);
    unless (-f $log4j_file) {
        ast_log_subsystem::reset_log4j($data_directory);
    }

    # reset the current namespace file if it is missing
    my $current_namespace_file = File::Spec->catfile($data_directory, $ast_preset_subsystem::NAMESPACE_PATH);
    unless (-f $current_namespace_file) {
        ast_preset_subsystem::reset_namespace($data_directory);
    }

    # add a .global folder to all namespaces if it is missing
    my @namespaces = ast_preset_subsystem::get_namespaces_array($data_directory);
    foreach my $namespace (@namespaces) {
        my $global_subfolder = File::Spec->catfile($data_directory, $ast_preset_subsystem::PRESETS_FOLDER, $namespace, $ast_preset_subsystem::GLOBAL_FOLDER);
        make_path("$global_subfolder", {
            verbose => 0,
            mode    => 0755
        });
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

    my @pager_command = get_pager();

    if ($skip_paging) {
        print "$message";
    }
    else {
        # NOTE: there is no easy way to prevent shell interference should the pager
        # command array contain only one element.
        open PAGER, "|-", @pager_command or die $!;
        print PAGER "$message";
        close PAGER;
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
    local $| = 1;      # activate autoflush to immediately show the prompt
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
# of the ATLAS_SHELL_TOOLS_PAGER env variable, and uses that instead if it is set.
# If that is unset, fall back to PAGER. If that is unset, try a default.
# Params: none
# Return: the pager command array
sub get_pager {
    my @pager_command = ();

    if (defined $ENV{ATLAS_SHELL_TOOLS_PAGER} && $ENV{ATLAS_SHELL_TOOLS_PAGER} ne '') {
        @pager_command = split /\s+/, $ENV{ATLAS_SHELL_TOOLS_PAGER};
    }
    elsif (defined $ENV{PAGER} && $ENV{PAGER} ne '') {
        @pager_command = split /\s+/, $ENV{PAGER};
    }
    else {
        push @pager_command, 'less';

        # Options (see less(1) for more info):
        # -c -> clear the screen before displaying
        # -S -> chop long lines instead of wrapping them
        # -R -> actually display ANSI "color" control sequences as colors/formatting
        # -M -> use verbose prompt
        # -i -> searches ignore case
        # -s -> squeeze consecutive blank lines
        # TODO consider -F, -X options here?
        # https://unix.stackexchange.com/questions/107315/less-quit-if-one-screen-without-no-init
        push @pager_command, '-cSRMis';
    }

    return @pager_command;
}

# Get an editor command capable of displaying and editing a text file. Checks
# the value of the ATLAS_SHELL_TOOLS_EDITOR env variable, and uses that instead if it points to an
# editor. If that is unset, fall back to EDITOR. If that is unset, try a default.
# Params: none
# Return: the editor command
sub get_editor {
    my @editor_command = ();

    if (defined $ENV{ATLAS_SHELL_TOOLS_EDITOR} && $ENV{ATLAS_SHELL_TOOLS_EDITOR} ne '') {
        @editor_command = split /\s+/, $ENV{ATLAS_SHELL_TOOLS_EDITOR};
    }
    elsif (defined $ENV{EDITOR} && $ENV{EDITOR} ne '') {
        @editor_command = split /\s+/, $ENV{EDITOR};
    }
    else {
        push @editor_command, 'vim';

        # The '+' option tells vim to start with the cursor at the end of the file.
        # This is generally convenient for most atlas-shell-tools use-cases.
        push @editor_command, '+';
    }

    return @editor_command;
}

# Get a man command capable of displaying some desired manpage.
# Params:
#   $skip_paging: optionally disable paging for man
# Return: the man command array
sub get_man {
    my @man_command = ();
    my $skip_paging = shift;

    push @man_command, 'man';

    if ($skip_paging) {
        push @man_command, '-P';
        push @man_command, 'cat';
    }

    return @man_command;
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

    closedir $h;

    return 1;
}

# Compute the Levenshtein distance between two strings.
# Params:
#   $string1: the first string
#   $string2: the second string
# Return: the Levenshtein distnace
sub levenshtein {
    my $string1 = shift;
    my $string2 = shift;

    # split the strings at each character
    my @letters1 = split //, $string1;
    my @letters2 = split //, $string2;

    # memoization table
    my @distance;
    $distance[$_][0] = $_ foreach (0 .. @letters1);
    $distance[0][$_] = $_ foreach (0 .. @letters2);

    foreach my $i (1 .. @letters1) {
        foreach my $j (1 .. @letters2) {
            my $cost = $letters1[$i - 1] eq $letters2[$j - 1] ? 0 : 1;
            $distance[$i][$j] = min($distance[$i - 1][$j] + 1, $distance[$i][$j - 1] + 1, $distance[$i - 1][$j - 1] + $cost);
        }
    }

    return $distance[@letters1][@letters2];
}

# Read the output of a given command array.
# Params:
#   $command_ref: a reference to an array containing the command args
# Return: the output of a given command array
sub read_command_output {
    my $command_ref = shift;

    my @command = @{$command_ref};
    open COMMAND, "-|", @command or die $!;
    my $output = '';
    while (<COMMAND>) {
        # Not the most efficient way to do things.
        # Perhaps some kind of slurp is needed. File::Slurp could work but does
        # have an outstanding Unicode bug. Need to investigate more.
        $output = $output . $_;
    }
    close COMMAND;

    return $output;
}

# Perl modules must return a value. Returning a value perl considers "truthy"
# signals that the module loaded successfully.
1;
