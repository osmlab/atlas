package ast_repo_subsystem;

use warnings;
use strict;

use Exporter qw(import);
use File::Path qw(make_path rmtree);
use ast_tty;

# Export symbols: variables and subroutines
our @EXPORT = qw(
    REPOS_FOLDER
    create_repo
    list_repos
    remove_repo
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

# Create a new repo.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output output
#   $repo: the name of the repo
#   $url: the repo URL
# Return: 1 on success, 0 on failure
sub create_repo {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $repo = shift;
    my $url = shift;

    my $repo_subfolder = File::Spec->catfile($ast_path, $REPOS_FOLDER, $repo);
    if (-d $repo_subfolder) {
        ast_utilities::error_output($program_name, "repo ${bold_stderr}${repo}${reset_stderr} already exists");
        return 0;
    }

    make_path("$repo_subfolder", {
        verbose => 0,
        mode => 0755
    });

    my $default_branch = "master";
    my $config_contents = "# CONFIG file for repo ${repo}
# Lines beginning with \"#\" are ignored
#
# Add exclude packages for gradle using \"exclude\".
# To exclude multiple packages, simply repeat this directive for each package. E.g.
# exclude=com.example.package
# exclude=com.example.anotherpackage
#
# Add gradle task skips using \"skip\".
# To skip multiple tasks, simply repeat this directive for each task. E.g.
# skip=javadoc
# skip=integrationTest
#
url=${url}
branch=${default_branch}
";

    my $repo_config_file = File::Spec->catfile($repo_subfolder, $REPO_CONFIG);
    open my $file_handle, '>', "$repo_config_file";
    print $file_handle "${config_contents}\n";
    close $file_handle;

    unless ($quiet) {
        print "Created repo ${bold_stdout}${repo}${reset_stdout} with URL ${bold_stdout}${url}${reset_stdout} on branch ${bold_stdout}${default_branch}${reset_stdout}.\n";
    }

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

    if (scalar @filtered_repos == 0) {
        ast_utilities::error_output($program_name, "found no repos");
        return 0;
    }

    print "${bold_stdout}Atlas Shell Tools${reset_stdout} repos:\n\n";
    for my $found_repo (sort {lc $a cmp lc $b} @filtered_repos) {
        print "    ${bold_stdout}${found_repo}${reset_stdout}\n";
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
        ast_utilities::error_output($program_name, "repo ${bold_stderr}${repo}${reset_stderr} does not exist");
        return 0;
    }

    rmtree($repo_subfolder);

    unless ($quiet) {
        print "Removed repo ${bold_stdout}${repo}${reset_stdout}.\n";
    }

    return 1;
}

# Perl modules must return a value. Returning a value perl considers "truthy"
# signals that the module loaded successfully.
1;
