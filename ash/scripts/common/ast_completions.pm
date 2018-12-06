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
    completion_ash
    completion_ashcfg
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

sub completion_ash {
    my $ash_path = shift;
    my $argv_ref = shift;

    my @argv = @{$argv_ref};
    # Shift "ash" off the front of ARGV
    shift @argv;

    my %subcommand_desc = ast_module_subsystem::get_subcommand_to_description_hash($ash_path);
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
    # use them.
    #
    my $argv_len = scalar @argv;
    my $rargv = $argv[$argv_len - 1];
    my $rargv_m1 = $argv[$argv_len - 2];
    my $rargv_m2 = $argv[$argv_len - 3];
    my $rargv_m3 = $argv[$argv_len - 4];

    # Handle special case where user is applying a preset with "--preset"
    if ($rargv_m1 eq '-p' || ast_utilities::string_starts_with($rargv_m1, '--p')) {
        my @presets = ast_preset_subsystem::get_all_presets_in_current_namespace($ash_path);
        my @completion_matches = completion_match_prefix($rargv, \@presets);

        foreach my $command (@completion_matches) {
            print "$command\n";
        }
        return 1;
    }

    # If we see a command anywhere in ARGV, stop special completions and signal
    # the completion wrapper script to use its filename defaults. We also handle
    # the special case where we saw the special 'cfg.preset' command. In that
    # case, we want to do custom completions.
    foreach my $arg (@argv) {
        foreach my $command (@commands) {
            if ($arg eq $command) {
                print "__ash_sentinel_complete_filenames__";
                return 1;
            }
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

            foreach my $command (@completion_matches) {
                print "$command\n";
            }
            return 1;
        }
    }

    # Default to completing available command names
    push @commands, $ast_preset_subsystem::CFGPRESET_START;
    my @completion_matches = completion_match_prefix($rargv, \@commands);
    foreach my $command (@completion_matches) {
        print "$command\n";
    }

    return 1;
}

sub completion_ashcfg {
    my $ash_path = shift;
    my $argv_ref = shift;

    my @argv = @{$argv_ref};
    # Shift "ash" off the front of ARGV
    shift @argv;

    my @commands = qw(activate deactivate install list log reset sync uninstall);

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
    # use them.
    #
    my $argv_len = scalar @argv;
    my $rargv = $argv[$argv_len - 1];
    my $rargv_m1 = $argv[$argv_len - 2];
    my $rargv_m2 = $argv[$argv_len - 3];
    my $rargv_m3 = $argv[$argv_len - 4];

    foreach my $arg (@argv) {
        if ($arg eq 'activate') {
            @commands = qw(module1 module2 anotherModule);
        }
        elsif ($arg eq 'deactivate') {
            @commands = qw(module3 module4 anotherModule2);
        }
        elsif ($arg eq 'install') {
            print "__ash_sentinel_complete_filenames__";
            return 1;
        }
    }

    my @completion_matches = completion_match_prefix($rargv, \@commands);
    foreach my $command (@completion_matches) {
        print "$command\n";
    }

    return 1;
}
