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

# TODO implement a function that takes in the current argv and returns the name
# of the current subcommand (stripping away the global options)
# TODO implement a function that takes in the current argv and returns an array
# containing just the subcommand and its arguments
# The above ideas would simplify all the 'rargv_mX' variables

sub completion_atlas {
    my $ast_path = shift;
    my $zsh_mode = shift;
    my $argv_ref = shift;

    my @argv = @{$argv_ref};
    my %argv_map = map { $_ => 1 } @argv;

    # Shift COMP_CWORD off the front of ARGV
    my $comp_cword = shift @argv;

    # Shift "atlas" off the front of ARGV
    shift @argv;

    if ($zsh_mode && $comp_cword == (scalar @argv + 1)) {
        push @argv, "";
    }

    my %subcommand_classes = ast_module_subsystem::get_subcommand_to_class_hash($ast_path);
    my @commands = keys %subcommand_classes;

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

    # Autocomplete the '--preset' and '--save-preset' flags, since they are probably the most used flags.
    # Since they are global options, we only complete them if we have not yet seen a command in ARGV.
    my $saw_command = 0;
    if (ast_utilities::string_starts_with($rargv, '-')) {
        foreach my $command (@commands) {
            if (exists($argv_map{$command})) {
                $saw_command = 1;
            }
        }
        unless ($saw_command) {
            my @flags = qw(--preset --save-preset --save-global-preset);
            my @completion_matches = completion_match_prefix($rargv, \@flags);
            print join("\n", @completion_matches) . "\n";
            return 1;
        }
    }

    # Handle special case where user is applying a preset with "--preset"
    if (defined $rargv_m1) {
        if ($rargv_m1 eq '-p' || ast_utilities::string_starts_with($rargv_m1, '--p')) {
            my @presets = ast_preset_subsystem::get_all_presets_in_current_namespace($ast_path);
            my @completion_matches = completion_match_prefix($rargv, \@presets);
            print join("\n", @completion_matches) . "\n";
            return 1;
        }
    }

    # If we see a command anywhere in ARGV, stop special completions and signal
    # the completion wrapper script to use its filename defaults.
    foreach my $command (@commands) {
        if (exists($argv_map{$command})) {
            print $FILE_COMPLETE_SENTINEL;
            return 1;
        }
    }

    # Default to completing available command names
    my @completion_matches = completion_match_prefix($rargv, \@commands);
    print join("\n", @completion_matches) . "\n";

    return 1;
}

sub completion_atlascfg {
    my $ast_path = shift;
    my $zsh_mode = shift;
    my $argv_ref = shift;

    my @argv = @{$argv_ref};

    # Shift COMP_CWORD off the front of ARGV
    my $comp_cword = shift @argv;

    # Shift "atlas-config" off the front of ARGV
    shift @argv;

    my %subcommand_classes = ast_module_subsystem::get_subcommand_to_class_hash($ast_path);

    # Shift global options off the front of ARGV
    foreach my $element (@argv) {
        if (ast_utilities::string_starts_with($element, '-')) {
            shift @argv;
            # We need to decrement $comp_cword, since it will contain an extra
            # count for each of the global options.
            $comp_cword = $comp_cword - 1;
        }
    }

    if ($zsh_mode && $comp_cword == (scalar @argv + 1)) {
        push @argv, "";
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
        @commands = qw(activate deactivate install list log preset repo reset sync uninstall update);
    }

    #
    # This really long if-else handles all the possible command-lines
    #

    # 'atlas-config install' command will complete file names
    if ((defined $argv[0] && $argv[0] eq 'install') && (defined $rargv_m1 && $rargv_m1 eq 'install')) {
        print $FILE_COMPLETE_SENTINEL;
        return 1;
    }

    # 'atlas-config activate' command will complete deactivated modules
    elsif ((defined $argv[0] && $argv[0]) eq 'activate' && (defined $rargv_m1 && $rargv_m1 eq 'activate')) {
        @commands = ast_module_subsystem::get_deactivated_modules(\%modules);
    }

    # 'atlas-config deactivate' command will complete activated modules
    elsif ((defined $argv[0] && $argv[0] eq 'deactivate') && (defined $rargv_m1 && $rargv_m1 eq 'deactivate')) {
        @commands = ast_module_subsystem::get_activated_modules(\%modules);
    }

    # 'atlas-config uninstall' command will complete all modules as many times as desired
    elsif ((defined $argv[0] && $argv[0] eq 'uninstall')) {
        @commands = keys %modules;
    }

    # 'atlas-config repo' command will complete repo subcommands
    elsif ((defined $argv[0] && $argv[0] eq 'repo') && (defined $rargv_m1 && $rargv_m1 eq 'repo')) {
        @commands = qw(add list remove edit install add-gradle-skip add-gradle-exclude);
    }

    # 'atlas-config repo remove' command will complete repos
    elsif ((defined $argv[0] && $argv[0] eq 'repo') && (defined $rargv_m2 && $rargv_m2 eq 'repo') && (defined $rargv_m1 && $rargv_m1 eq 'remove')) {
        @commands = ast_repo_subsystem::get_all_repos($ast_path);
    }

    # 'atlas-config repo edit' command will complete repos
    elsif ((defined $argv[0] && $argv[0] eq 'repo') && (defined $rargv_m2 && $rargv_m2 eq 'repo') && (defined $rargv_m1 && $rargv_m1 eq 'edit')) {
        @commands = ast_repo_subsystem::get_all_repos($ast_path);
    }

    # 'atlas-config repo install' command will complete repos
    elsif ((defined $argv[0] && $argv[0] eq 'repo') && (defined $rargv_m2 && $rargv_m2 eq 'repo') && (defined $rargv_m1 && $rargv_m1 eq 'install')) {
        @commands = ast_repo_subsystem::get_all_repos($ast_path);
    }

    # 'atlas-config repo list' command will complete repos
    elsif ((defined $argv[0] && $argv[0] eq 'repo') && (defined $rargv_m2 && $rargv_m2 eq 'repo') && (defined $rargv_m1 && $rargv_m1 eq 'list')) {
        @commands = ast_repo_subsystem::get_all_repos($ast_path);
    }

    # 'atlas-config repo add-gradle-skip' command will complete repos
    elsif ((defined $argv[0] && $argv[0] eq 'repo') && (defined $rargv_m2 && $rargv_m2 eq 'repo') && (defined $rargv_m1 && $rargv_m1 eq 'add-gradle-skip')) {
        @commands = ast_repo_subsystem::get_all_repos($ast_path);
    }

    # 'atlas-config repo add-gradle-exclude' command will complete repos
    elsif ((defined $argv[0] && $argv[0] eq 'repo') && (defined $rargv_m2 && $rargv_m2 eq 'repo') && (defined $rargv_m1 && $rargv_m1 eq 'add-gradle-exclude')) {
        @commands = ast_repo_subsystem::get_all_repos($ast_path);
    }

    # 'atlas-config preset' command will complete 'preset' subcommands
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m1 && $rargv_m1 eq 'preset')) {
        @commands = qw(save save-global edit edit-global remove remove-global list list-global namespace copy copy-global);
    }

    # 'atlas-config preset save' command will complete atlas shell tools commands
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m2 && $rargv_m2 eq 'preset') && (defined $rargv_m1 && $rargv_m1 eq 'save')) {
        @commands = keys %subcommand_classes;
    }

    # 'atlas-config preset edit' command will complete atlas shell tools commands
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m2 && $rargv_m2 eq 'preset') && (defined $rargv_m1 && $rargv_m1 eq 'edit')) {
        @commands = keys %subcommand_classes;
    }

    # 'atlas-config preset edit <command>' command will complete any presets for <command>
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m3 && $rargv_m3 eq 'preset') && (defined $rargv_m2 && $rargv_m2 eq 'edit') && (defined $rargv_m1)) {
        @commands = ast_preset_subsystem::get_all_presets_for_command($ast_path, $rargv_m1);
    }

    # 'atlas-config preset edit-global' command will complete any global presets
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m2 && $rargv_m2 eq 'preset') && (defined $rargv_m1 && $rargv_m1 eq 'edit-global')) {
        @commands = ast_preset_subsystem::get_all_presets_for_command($ast_path, $ast_preset_subsystem::GLOBAL_FOLDER);
    }

    # 'atlas-config preset remove' command will complete atlas shell tools commands
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m2 && $rargv_m2 eq 'preset') && (defined $rargv_m1 && $rargv_m1 eq 'remove')) {
        @commands = keys %subcommand_classes;
    }

    # 'atlas-config preset remove <command>' command will complete any presets for <command>
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m3 && $rargv_m3 eq 'preset') && (defined $rargv_m2 && $rargv_m2 eq 'remove') && (defined $rargv_m1)) {
        @commands = ast_preset_subsystem::get_all_presets_for_command($ast_path, $rargv_m1);
    }

    # 'atlas-config preset remove-global' command will complete any global presets
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m2 && $rargv_m2 eq 'preset') && (defined $rargv_m1 && $rargv_m1 eq 'remove-global')) {
        @commands = ast_preset_subsystem::get_all_presets_for_command($ast_path, $ast_preset_subsystem::GLOBAL_FOLDER);
    }

    # 'atlas-config preset list' command will complete atlas shell tools commands
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m2 && $rargv_m2 eq 'preset') && (defined $rargv_m1 && $rargv_m1 eq 'list')) {
        @commands = keys %subcommand_classes;
    }

    # 'atlas-config preset list <command>' command will complete any presets for <command>
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m3 && $rargv_m3 eq 'preset') && (defined $rargv_m2 && $rargv_m2 eq 'list') && (defined $rargv_m1)) {
        @commands = ast_preset_subsystem::get_all_presets_for_command($ast_path, $rargv_m1);
    }

    # 'atlas-config preset list-global' will complete any global presets
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m2 && $rargv_m2 eq 'preset') && (defined $rargv_m1 && $rargv_m1 eq 'list-global')) {
        @commands = ast_preset_subsystem::get_all_presets_for_command($ast_path, $ast_preset_subsystem::GLOBAL_FOLDER);
    }

    # 'atlas-config preset namespace' command will complete 'namespace' subcommands
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m2 && $rargv_m2 eq 'preset') && (defined $rargv_m1 && $rargv_m1 eq 'namespace')) {
        @commands = qw(create remove list use);
    }

    # 'atlas-config preset namespace remove' command will complete namespaces
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m3 && $rargv_m3 eq 'preset') && (defined $rargv_m2 && $rargv_m2 eq 'namespace') && (defined $rargv_m1 && $rargv_m1 eq 'remove')) {
        @commands = ast_preset_subsystem::get_namespaces_array($ast_path);
    }

    # 'atlas-config preset namespace list' command will complete namespaces
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m3 && $rargv_m3 eq 'preset') && (defined $rargv_m2 && $rargv_m2 eq 'namespace') && (defined $rargv_m1 && $rargv_m1 eq 'list')) {
        @commands = ast_preset_subsystem::get_namespaces_array($ast_path);
    }

    # 'atlas-config preset namespace use' command will complete namespaces
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m3 && $rargv_m3 eq 'preset') && (defined $rargv_m2 && $rargv_m2 eq 'namespace') && (defined $rargv_m1 && $rargv_m1 eq 'use')) {
        @commands = ast_preset_subsystem::get_namespaces_array($ast_path);
    }

    # 'atlas-config preset copy' command will complete atlas shell tools commands
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m2 && $rargv_m2 eq 'preset') && (defined $rargv_m1 && $rargv_m1 eq 'copy')) {
        @commands = keys %subcommand_classes;
    }

    # 'atlas-config preset copy <command>' command will complete any presets for <command>
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m3 && $rargv_m3 eq 'preset') && (defined $rargv_m2 && $rargv_m2 eq 'copy') && (defined $rargv_m1)) {
        @commands = ast_preset_subsystem::get_all_presets_for_command($ast_path, $rargv_m1);
    }

    # 'atlas-config preset copy-global' command will complete any global presets
    elsif ((defined $argv[0] && $argv[0] eq 'preset') && (defined $rargv_m2 && $rargv_m2 eq 'preset') && (defined $rargv_m1 && $rargv_m1 eq 'copy-global')) {
        @commands = ast_preset_subsystem::get_all_presets_for_command($ast_path, $ast_preset_subsystem::GLOBAL_FOLDER);
    }

    # 'atlas-config log' command will complete log subcommands
    elsif ((defined $argv[0] && $argv[0] eq 'log') && (defined $rargv_m1 && $rargv_m1 eq 'log')) {
        @commands = qw(reset set-level set-stream show);
    }

    # 'atlas-config log set-level' command will complete log levels
    elsif ((defined $argv[0] && $argv[0] eq 'log') && (defined $rargv_m2 && $rargv_m2 eq 'log') && (defined $rargv_m1 && $rargv_m1 eq 'set-level')) {
        @commands = qw(ALL TRACE DEBUG INFO WARN ERROR FATAL OFF);
    }

    # 'atlas-config log set-stream' command will complete log streams
    elsif ((defined $argv[0] && $argv[0] eq 'log') && (defined $rargv_m2 && $rargv_m2 eq 'log') && (defined $rargv_m1 && $rargv_m1 eq 'set-stream')) {
        @commands = qw(stdout stderr);
    }

    # 'atlas-config reset' command will complete reset subcommands
    elsif ((defined $argv[0] && $argv[0] eq 'reset') && (defined $rargv_m1 && $rargv_m1 eq 'reset')) {
        @commands = qw(all index log modules presets repos);
    }

    # Generate completion matches based on prefix of current word
    my @completion_matches = completion_match_prefix($rargv, \@commands);
    print join("\n", @completion_matches) . "\n";

    return 1;
}

sub debug_dump_string {
    my $string = shift;
    my $file = shift;
    open my $handle, '>>', $file;
    print $handle $string;
    close $handle;
}

# Perl modules must return a value. Returning a value perl considers "truthy"
# signals that the module loaded successfully.
1;
