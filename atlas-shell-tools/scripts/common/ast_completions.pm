package ast_completions;

use warnings;
use strict;

use Exporter qw(import);
use ast_module_subsystem;
use ast_preset_subsystem;
use ast_utilities;
use ast_tty;

# Export symbols: variables and subroutines
our @EXPORT = qw(
    completion_match_prefix
    completion_atlas
    completion_atlascfg
);

my $FILE_COMPLETE_SENTINEL = "__atlas-shell-tools_sentinel_complete_filenames__";

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

sub completion_match_prefix {
    my $prefix_to_complete = shift;
    my $possible_words_ref = shift;

    my @possible_words = @{$possible_words_ref};

    if ($prefix_to_complete eq "") {
        return @possible_words;
    }

    my @matched_words = ();

    foreach my $word (@possible_words) {
        if (ast_utilities::string_starts_with($word, $prefix_to_complete)) {
            push @matched_words, $word;
        }
    }

    return @matched_words;
}

sub completion_atlas {
    my $ast_path = shift;
    my $argv_ref = shift;

    my @argv = @{$argv_ref};
    # Shift "atlas" off the front of ARGV
    shift @argv;

    my %subcommand_desc = ast_module_subsystem::get_subcommand_to_description_hash($ast_path);
    my @commands = keys %subcommand_desc;

    # In the completion code, we use the following conventions to name variables
    # containing ARGV elements. Assume ARGV looks like the following:
    #
    # ARGV of length N:
    # ARGV[0] ... ARGV[N - K] ... ARGV[N - 3], ARGV[N - 2], ARGV[N - 1]
    #             ^ rargv_m(k-1)  ^ rargv_m2   ^ rargv_m1   ^ rargv
    #
    # Essentially, "rargv" means the "rightmost" ARGV element. Then "rargv_m1"
    # means the "rightmost" ARGV element minus 1, and so on. Since perl plays fast
    # and loose with array indexing, we can index into elements that may or may
    # not actually exist, and then check if they are defined before we actually
    # use them. The '-1' indexing syntax just indexes from the end of the array.
    #
    my $argv_len = scalar @argv;
    my $rargv = $argv[-1];
    my $rargv_m1 = $argv[-2];
    my $rargv_m2 = $argv[-3];
    my $rargv_m3 = $argv[-4];

    # TODO this will do strange things when completing subcommand options...
    # Autocomplete the '--preset' and '--save-preset' flags, since they are probably the most used flags
    if (ast_utilities::string_starts_with($rargv, '-')) {
        my @flags = qw(--preset --save-preset);
        my @completion_matches = completion_match_prefix($rargv, \@flags);
        print "@completion_matches\n";
        return 1;
    }

    # Handle special case where user is applying a preset with "--preset"
    if (defined $rargv_m1) {
        if ($rargv_m1 eq '-p' || ast_utilities::string_starts_with($rargv_m1, '--p')) {
            my @presets = ast_preset_subsystem::get_all_presets_in_current_namespace($ast_path);
            my @completion_matches = completion_match_prefix($rargv, \@presets);
            print "@completion_matches\n";
            return 1;
        }
    }

    # If we saw the cfgpreset command anywhere in ARGV, use special complete context.
    # This block of code is a bit of a mess :)
    foreach my $arg (@argv) {
        if ($arg eq $ast_preset_subsystem::CFGPRESET_START) {
            my @directives;

            if ($rargv_m1 eq 'namespace') {
                @directives = qw(create use list remove);
            }
            elsif ($rargv_m1 eq $ast_preset_subsystem::CFGPRESET_START) {
                @directives = qw(save show remove edit copy namespace);
            }

            my @completion_matches = completion_match_prefix($rargv, \@directives);
            print "@completion_matches\n";
            return 1;
        }
    }

    # If we see a command anywhere in ARGV, stop special completions and signal
    # the completion wrapper script to use its filename defaults.
    foreach my $arg (@argv) {
        foreach my $command (@commands) {
            if ($arg eq $command) {
                print $FILE_COMPLETE_SENTINEL;
                return 1;
            }
        }
    }

    # Default to completing available command names
    push @commands, $ast_preset_subsystem::CFGPRESET_START;
    my @completion_matches = completion_match_prefix($rargv, \@commands);
    print "@completion_matches\n";

    return 1;
}

sub completion_atlascfg {
    my $ast_path = shift;
    my $argv_ref = shift;

    my @argv = @{$argv_ref};

    # Shift "atlas-config" off the front of ARGV
    shift @argv;

    # Shift global options off the front of ARGV
    foreach my $element (@argv) {
        if (ast_utilities::string_starts_with($element, '-')) {
            shift @argv;
        }
    }

    # If no more ARGV is left, exit
    if (scalar @argv == 0) {
        return 1;
    }

    my @commands = ();
    my %modules = ast_module_subsystem::get_module_to_status_hash($ast_path);

    # In the completion code, we use the following conventions to name variables
    # containing ARGV elements. Assume ARGV looks like the following:
    #
    # ARGV of length N:
    # ARGV[0] ... ARGV[N - K] ... ARGV[N - 3], ARGV[N - 2], ARGV[N - 1]
    #             ^ rargv_m(k-1)  ^ rargv_m2   ^ rargv_m1   ^ rargv
    #
    # Essentially, "rargv" means the "rightmost" ARGV element. Then "rargv_m1"
    # means the "rightmost" ARGV element minus 1, and so on. Since perl plays fast
    # and loose with array indexing, we can index into elements that may or may
    # not actually exist, and then check if they are defined before we actually
    # use them. The '-1' indexing syntax just indexes from the end of the array.
    #
    my $rargv = $argv[-1];
    my $rargv_m1 = $argv[-2];
    my $rargv_m2 = $argv[-3];
    my $rargv_m3 = $argv[-4];

    unless (defined $argv[1]) {
        @commands = qw(activate deactivate install list log reset sync uninstall update);
    }

    # If subcommand is 'install', just complete file names
    if (defined $argv[0] && $argv[0] eq 'install' && $rargv_m1 eq 'install') {
        print $FILE_COMPLETE_SENTINEL;
        return 1;
    }
    elsif (defined $argv[0] && $argv[0] eq 'activate' && $rargv_m1 eq 'activate') {
        @commands = ast_module_subsystem::get_deactivated_modules(\%modules);
    }
    elsif (defined $argv[0] && $argv[0] eq 'deactivate' && $rargv_m1 eq 'deactivate') {
        @commands = ast_module_subsystem::get_activated_modules(\%modules);
    }
    elsif (defined $argv[0] && $argv[0] eq 'uninstall' && $rargv_m1 eq 'uninstall') {
        @commands = keys %modules;
    }

    my @completion_matches = completion_match_prefix($rargv, \@commands);
    print "@completion_matches\n";

    return 1;
}

# Perl modules must return a value. Returning a value perl considers "truthy"
# signals that the module loaded successfully.
1;
