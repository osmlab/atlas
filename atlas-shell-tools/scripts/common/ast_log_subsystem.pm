package ast_log_subsystem;

use warnings;
use strict;

use Exporter qw(import);
use File::Spec;
use ast_tty;
use ast_utilities;

# Export symbols: variables and subroutines
our @EXPORT = qw(
    LOG4J_FOLDER
    LOG4J_FILE
    LOG4J_FILE_PATH
    DEFAULT_LOG4J_CONTENTS
    reset_log4j
    read_loglevel_from_file
    read_logstream_from_file
    replace_loglevel_in_file
    replace_logstream_in_file
);

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

our $LOG4J_FOLDER = 'log4j';
our $LOG4J_FILE = 'log4j.properties';
our $LOG4J_FILE_PATH = File::Spec->catfile($LOG4J_FOLDER, $LOG4J_FILE);

# The default setting for the log4j file.
my $DEFAULT_LOG4J_CONTENTS = "log4j.rootLogger=ERROR, stderr
# DO NOT REMOVE/MODIFY THE ABOVE LINE OR ANY OF THIS FILE
# Use 'atlas-config log' subcommand to manage the log configuration
# If this file is corrupted, use 'atlas-config log --reset' to fix.

# Direct log messages to stderr
log4j.appender.stderr=org.apache.log4j.ConsoleAppender
log4j.appender.stderr.Target=System.err
log4j.appender.stderr.layout=org.apache.log4j.PatternLayout
log4j.appender.stderr.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n
";

# Reset the log4j file to default.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
# Return: none
sub reset_log4j {
    my $ast_path = shift;
    my $log4j_file = File::Spec->catfile($ast_path, $LOG4J_FOLDER, $LOG4J_FILE);
    open my $file_handle, '>', "$log4j_file";
    print $file_handle $DEFAULT_LOG4J_CONTENTS;
    close $file_handle;
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
    }
    elsif ($new_stream eq 'stderr') {
        $old_system = 'System\.out';
        $new_system = 'System.err';
    }
    else {
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

# Perl modules must return a value. Returning a value perl considers "truthy"
# signals that the module loaded successfully.
1;
