package ast_tty;

use warnings;
use strict;

use Exporter qw(import);

# Export symbols: variables and subroutines
our @EXPORT = qw(
    is_no_colors
    is_no_colors_stdout
    is_no_colors_stderr
    ansi_red
    ansi_green
    ansi_magenta
    ansi_bold
    ansi_reset
    ansi_begin_underln
    ansi_end_underln
    terminal_width
);

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

# Determine if we should disable text/color formatting for output. Various
# conditions are checked, and if none of them trigger then we can use colors!
# We also make one check for explicit use of colors, to allow a case where a
# user has set NO_COLOR, but would like to make an exception for atlas-shell-tools.
# Params: none
# Return: 1 if no colors, 0 otherwise
sub is_no_colors {
    # check for dumb
    # TODO need to check for xterm too?
    if ($ENV{'TERM'} eq "dumb") {
        return 1;
    }

    # explicitly use colors for atlas-shell-tools
    if (exists $ENV{'ATLAS_SHELL_TOOLS_USE_COLOR'}) {
        return 0;
    }

    # respect the NO_COLOR env var
    if (exists $ENV{'NO_COLOR'}) {
        return 1;
    }

    if (exists $ENV{'ATLAS_SHELL_TOOLS_NO_COLOR'}) {
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
