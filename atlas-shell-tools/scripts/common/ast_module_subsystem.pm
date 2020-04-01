package ast_module_subsystem;

use warnings;
use strict;

use Exporter qw(import);
use File::Basename qw(basename);
use File::Spec;
use ast_utilities;
use ast_tty;
use ast_log_subsystem;

# Export symbols: variables and subroutines
our @EXPORT = qw(
    MODULE_SUFFIX
    DEACTIVATED_SUFFIX
    DEACTIVATED_MODULE_SUFFIX
    MODULES_FOLDER
    ACTIVE_INDEX_FILE
    ACTIVE_INDEX_PATH
    ACTIVATED
    DEACTIVATED
    GOOD_SYMLINK
    BROKEN_SYMLINK
    REAL_FILE
    SOURCE_KEY
    URI_KEY
    DATE_TIME_KEY
    REPO_NAME_KEY
    REPO_REF_KEY
    REPO_COMMIT_KEY
    METADATA_SEPARATOR
    get_subcommand_to_class_hash
    get_subcommand_to_description_hash
    get_module_to_status_hash
    get_module_to_symlink_hash
    get_module_to_target_hash
    get_module_to_metadata_hash
    get_activated_modules
    get_deactivated_modules
    perform_uninstall
    perform_activate
    perform_deactivate
    generate_active_module_index
    remove_active_module_index
);

our $METADATA_SUFFIX = '.metadata';
our $MODULE_SUFFIX = '.jar';
our $DEACTIVATED_SUFFIX = '.deactivated';
our $DEACTIVATED_MODULE_SUFFIX = $MODULE_SUFFIX . $DEACTIVATED_SUFFIX;
our $MODULES_FOLDER = 'modules';
our $ACTIVE_INDEX_FILE = '.active_module_index';
our $ACTIVE_INDEX_PATH = File::Spec->catfile($MODULES_FOLDER, $ACTIVE_INDEX_FILE);

our $ACTIVATED = 1;
our $DEACTIVATED = 0;

our $GOOD_SYMLINK = 1;
our $BROKEN_SYMLINK = -1;
our $REAL_FILE = 0;

# Module metadata keys/data
our $SOURCE_KEY = "source-type";
our $URI_KEY = "URI";
our $DATE_TIME_KEY = "date-time";
our $REPO_NAME_KEY = "repo-name";
our $REPO_REF_KEY = "repo-ref";
our $REPO_COMMIT_KEY = "repo-commit";
our $METADATA_SEPARATOR = ":";

# Use ASCII record separator as delimiter.
# This is also defined in Atlas class ActiveModuleIndexWriter.
my $INDEX_DELIMITER = "\x1E";

# The Java class that creates the active_module_index
my $INDEX_WRITER_CLASS = "org.openstreetmap.atlas.utilities.command.ActiveModuleIndexWriter";

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

# Get a hash that maps subcommand names to their respective classes. The hash is
# computed from the current active module index.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
# Return: a hash of all subcommands to their classes.
sub get_subcommand_to_class_hash {
    my $ast_path = shift;

    my $index_path = File::Spec->catfile($ast_path, $ACTIVE_INDEX_PATH);
    open my $index_fileIN, '<', $index_path or return();
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
#   $ast_path: the path to the atlas-shell-tools data folder
# Return: a hash of all subcommands to their descriptions.
sub get_subcommand_to_description_hash {
    my $ast_path = shift;

    my $index_path = File::Spec->catfile($ast_path, $ACTIVE_INDEX_PATH);
    open my $index_fileIN, '<', $index_path or return();
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
#   $ast_path: the path to the atlas-shell-tools data folder
# Return: a hash of all modules to their activation status.
sub get_module_to_status_hash {
    my $ast_path = shift;

    my @find_command = (
        "find", "${ast_path}/${MODULES_FOLDER}",
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
        }
        else {
            $module_activated = $ACTIVATED;
        }
        $module =~ s{$MODULE_SUFFIX}{};
        $module =~ s{$DEACTIVATED_SUFFIX}{};
        $modules{$module} = $module_activated;
    }
    close FIND;

    return %modules;
}

# Get a hash that maps all present module names to their symlink state.
# Symlinked modules are mapped to GOOD_SYMLINK, regular modules are mapped to
# REAL_FILE. Broken symlink modules are mapped to BROKEN_SYMLINK.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
# Return: a hash of all modules to their symlink state.
sub get_module_to_symlink_hash {
    my $ast_path = shift;

    my @find_command = (
        "find", "${ast_path}/${MODULES_FOLDER}",
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
            }
            else {
                $module_islink = $GOOD_SYMLINK;
            }
        }
        else {
            $module_islink = $REAL_FILE;
        }
        $module =~ s{$MODULE_SUFFIX}{};
        $module =~ s{$DEACTIVATED_SUFFIX}{};
        $modules{$module} = $module_islink;
    }
    close FIND;

    return %modules;
}

# Get a hash that maps all present module names to their symlink target.
# If a module is not a symlink, it maps to an empty string.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
# Return: a hash of all modules to their symlink target.
sub get_module_to_target_hash {
    my $ast_path = shift;

    my @find_command = (
        "find", "${ast_path}/${MODULES_FOLDER}",
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
        }
        else {
            $module_target = '';
        }
        $module =~ s{$MODULE_SUFFIX}{};
        $module =~ s{$DEACTIVATED_SUFFIX}{};
        $modules{$module} = $module_target;
    }
    close FIND;

    return %modules;
}

# Get a hash that maps all present module names to their metadata hashes, if present.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
# Return: a hash of all modules to their metadata hashes.
sub get_module_to_metadata_hash {
    my $ast_path = shift;

    my @find_command = (
        "find", "${ast_path}/${MODULES_FOLDER}",
        "-maxdepth", "1",
        "-name", "*$METADATA_SUFFIX",
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
        my $module_basename = basename($module);
        $module_basename =~ s{$METADATA_SUFFIX}{};

        my %metadata;
        open my $file_handle, '<', $module or die "Could not open module metadata file $module $!";
        # we have to change the separator to newline in order to parse the file properly
        $/ = "\n";
        while (my $line = <$file_handle>) {
            chomp $line;
            # trim excess whitespace from left and right
            $line =~ s/^\s+|\s+$//g;
            if ($line eq '' || substr($line, 0, 1) eq '#') {
                next;
            }
            my @line_split = split $METADATA_SEPARATOR, $line, 2;
            unless (defined $line_split[0]) {
                next;
            }
            # trim excess whitespace from left and right
            $line_split[0] =~ s/^\s+|\s+$//g;
            if (defined $line_split[1] && $line_split[1] !~ /^\s*$/) {
                # trim excess whitespace from left and right
                $line_split[1] =~ s/^\s+|\s+$//g;
                $metadata{$line_split[0]} = $line_split[1];
            }
        }
        close $file_handle;
        $modules{$module_basename} = \%metadata;
        # change the separator back to \0 for FIND
        $/ = "\0";
    }
    close FIND;

    return %modules;
}

# Get an array of all active modules, computed from the modules hash returned by
# the 'get_module_to_status_hash' subroutine.
# Params:
#   $modules_ref: a reference to the module hash returned by 'get_module_to_status_hash'
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

# Get an array of all deactived modules, computed from the modules hash returned by
# the 'get_module_to_status_hash' subroutine.
# Params:
#   $modules_ref: a reference to the module hash returned by 'get_module_to_status_hash'
# Return: an array of all deactived modules
sub get_deactivated_modules {
    my $modules_ref = shift;

    my %modules = %{$modules_ref};
    my @deactivated_modules = ();

    foreach my $module (keys %modules) {
        if ($modules{$module} == $DEACTIVATED) {
            push @deactivated_modules, $module;
        }
    }

    return @deactivated_modules;
}

# Install a module using some given parameters.
# Params:
#   $module_to_install: the path to the module to install
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the running program
#   $alternate_name: an alternate name for the module, empty to use path basename
#   $syminstall: install using a symlink instead of a copy
#   $skip_install: skip installation if module exists
#   $force_install: force overwrite if module exists
#   $install_deactivated: install module but do not activate
#   $metadata_ref: a reference to a metadata hash
#   $quiet: suppress non-essential output
#
# Return: 1 on success, 0 on failure
sub perform_install {
    my $module_to_install = shift;
    my $ast_path = shift;
    my $program_name = shift;
    my $alternate_name = shift;
    my $syminstall = shift;
    my $skip_install = shift;
    my $force_install = shift;
    my $install_deactivated = shift;
    my $metadata_ref = shift;
    my $quiet = shift;

    unless (-f $module_to_install || -l $module_to_install) {
        ast_utilities::error_output($program_name, "no such file ${bold_stderr}${module_to_install}${reset_stderr}");
        return 0;
    }

    my $modules_folder = File::Spec->catfile($ast_path, $MODULES_FOLDER);

    # Create the module name, respecting the alternate name if provided.
    my $module_basename;
    if (!$alternate_name eq "") {
        $module_basename = $alternate_name . $MODULE_SUFFIX;
    }
    else {
        $module_basename = basename($module_to_install);
    }
    # TODO: figure out how to interpolate $MODULE_SUFFIX into this regex
    unless ($module_basename =~ /.*\.jar$/) {
        ast_utilities::error_output($program_name, "module must end with '.jar' extension");
        return 0;
    }
    $module_basename =~ s{$MODULE_SUFFIX}{};

    # Handle the case where the module is already installed
    my %modules = get_module_to_status_hash($ast_path);
    if (defined $modules{$module_basename}) {
        ast_utilities::warn_output($program_name, "module ${bold_stderr}${module_basename}${reset_stderr} is already installed");
        if ($skip_install) {
            print STDERR "Skipping installation.\n";
            return 0;
        }
        unless ($force_install) {
            my $overwrite = ast_utilities::prompt_yn("Overwrite?");
            unless ($overwrite) {
                print STDERR "Skipping installation.\n";
                return 0;
            }
        }
        ast_utilities::warn_output($program_name, "overwriting ${bold_stderr}${module_basename}${reset_stderr}");
    }

    # Construct the new module paths.
    # We create a path for both an activated and deactivated version.
    my $module_new_path =
        File::Spec->catfile($modules_folder, $module_basename . $MODULE_SUFFIX);
    my $module_new_path_deactivated =
        File::Spec->catfile($modules_folder, $module_basename . $DEACTIVATED_MODULE_SUFFIX);

    # If we made it here we are go to overwrite, so clean up any matching existing modules.
    unlink $module_new_path;
    unlink $module_new_path_deactivated;

    my $exitcode;
    if ($syminstall) {
        my $module_to_install_abs = Cwd::realpath($module_to_install);
        my $module_to_install_rel = File::Spec->abs2rel($module_to_install_abs, $modules_folder);

        if ($install_deactivated) {
            symlink($module_to_install_rel, $module_new_path_deactivated);
        }
        else {
            symlink($module_to_install_rel, $module_new_path);
        }
        $exitcode = $? >> 8;
    }
    else {
        my @command = ();
        push @command, "cp";
        push @command, "$module_to_install";
        if ($install_deactivated) {
            push @command, "$module_new_path_deactivated";
            system {$command[0]} @command;
        }
        else {
            push @command, "$module_new_path";
            system {$command[0]} @command;
        }
        $exitcode = $? >> 8;
    }

    if ($exitcode) {
        print STDERR "${red_stderr}${bold_stderr}Installation of module ${module_basename} failed.${reset_stderr} Operation exited with $exitcode.\n";
        return 0;
    }
    else {
        unless ($install_deactivated) {
            my %modules = get_module_to_status_hash($ast_path);
            foreach my $module (keys %modules) {
                if ($modules{$module} == $ACTIVATED
                    && $module ne $module_basename) {
                    my $success = perform_deactivate($module, $ast_path, $program_name, $quiet);
                    unless ($success) {
                        ast_utilities::warn_output($program_name, "installation may not function properly");
                    }
                }
            }
            remove_active_module_index($ast_path, $program_name, $quiet);
            my $success = generate_active_module_index($ast_path, $program_name, $quiet, 0);
            unless ($success) {
                ast_utilities::warn_output($program_name, "partial installation may not function properly");
            }
        }

        my $success = write_module_metadata($modules_folder, $module_basename, $metadata_ref, $program_name);
        unless ($success) {
            ast_utilities::warn_output($program_name, "metadata not written");
        }

        unless ($quiet) {
            print "Module ${green_stdout}${bold_stdout}${module_basename}${reset_stdout} installed.\n";
        }
    }

    return 1;
}

# Uninstall a module with a given name.
# Params:
#   $module_to_uninstall: the name of the module to uninstall
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the running program
#   $quiet: suppress non-essential output
#   $force: force uninstall on activated module
# Return: 1 on success, 0 on failure
sub perform_uninstall {
    my $module_to_uninstall = shift;
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $force = shift;

    my %modules = get_module_to_status_hash($ast_path);

    unless (exists $modules{$module_to_uninstall}) {
        error_output($program_name, "no such module ${bold_stderr}${module_to_uninstall}${reset_stderr}");
        return 0;
    }

    if ($modules{$module_to_uninstall} eq $ACTIVATED && $force != 1) {
        warn_output($program_name, "skipping activated module ${bold_stderr}${module_to_uninstall}${reset_stderr}");
        return 0;
    }

    # try to remove the module
    my $modules_folder = File::Spec->catfile($ast_path, $MODULES_FOLDER);
    my $module_remove_path =
        File::Spec->catfile($modules_folder, $module_to_uninstall . $MODULE_SUFFIX);
    my $module_remove_path_deactivated =
        File::Spec->catfile($modules_folder, $module_to_uninstall . $DEACTIVATED_MODULE_SUFFIX);
    my $module_remove_path_metadata =
        File::Spec->catfile($modules_folder, $module_to_uninstall . $METADATA_SUFFIX);

    unlink $module_remove_path;
    unlink $module_remove_path_deactivated;
    unlink $module_remove_path_metadata;
    unless ($quiet) {
        print "Module ${bold_stdout}${module_to_uninstall}${reset_stdout} uninstalled.\n";
    }

    return 1;
}

# Activate a module with a given name.
# Params:
#   $module_to_activate: the name of the module to activate
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the running program
#   $quiet: suppress non-essential messages
# Return: 1 on success, 0 on failure
sub perform_activate {
    my $module_to_activate = shift;
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;

    my %modules = get_module_to_status_hash($ast_path);

    unless (exists $modules{$module_to_activate}) {
        ast_utilities::error_output($program_name, "no such module ${bold_stderr}${module_to_activate}${reset_stderr}");
        return 0;
    }

    my $status = $modules{$module_to_activate};
    if ($status == $ACTIVATED) {
        ast_utilities::warn_output($program_name, "module ${bold_stderr}${module_to_activate}${reset_stderr} already activated");
        return 1;
    }

    # We made it here, so we are good to activate the module!
    # Effectively, this just means removing the DEACTIVATED_MODULE_SUFFIX
    my $module_path_nosuffix = File::Spec->catfile($ast_path, $MODULES_FOLDER, $module_to_activate);
    my $old_module_path = $module_path_nosuffix . $DEACTIVATED_MODULE_SUFFIX;
    my $new_module_path = $module_path_nosuffix . $MODULE_SUFFIX;

    rename $old_module_path, $new_module_path;
    my $exitcode = $? >> 8;
    if ($exitcode) {
        print STDERR "${red_stderr}${bold_stderr}Activation of module ${module_to_activate} failed.${reset_stderr} Rename exited with $exitcode.\n";
        return 0;
    }
    else {
        remove_active_module_index($ast_path, $program_name, $quiet);
        generate_active_module_index($ast_path, $program_name, $quiet, 0);
        unless ($quiet) {
            print "Module ${green_stdout}${bold_stdout}${module_to_activate}${reset_stdout} activated.\n";
        }
    }

    return 1;
}

# Deactivate a module with a given name.
# Params:
#   $module_to_deactivate: the name of the module to deactivate
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the running program
#   $quiet: suppress non-essential messages
# Return: 1 on success, 0 on failure
sub perform_deactivate {
    my $module_to_deactivate = shift;
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;

    my %modules = get_module_to_status_hash($ast_path);

    unless (exists $modules{$module_to_deactivate}) {
        ast_utilities::error_output($program_name, "no such module ${bold_stderr}${module_to_deactivate}${reset_stderr}");
        return 0;
    }

    my $status = $modules{$module_to_deactivate};
    if ($status == $DEACTIVATED) {
        ast_utilities::warn_output($program_name, "module ${bold_stderr}${module_to_deactivate}${reset_stderr} already deactivated");
        return 0;
    }

    # We made it here, so we are good to deactivate the module!
    # Effectively, this just means adding the DEACTIVATED_MODULE_SUFFIX
    my $module_path_nosuffix = File::Spec->catfile($ast_path, $MODULES_FOLDER, $module_to_deactivate);
    my $old_module_path = $module_path_nosuffix . $MODULE_SUFFIX;
    my $new_module_path = $module_path_nosuffix . $DEACTIVATED_MODULE_SUFFIX;

    rename $old_module_path, $new_module_path;
    my $exitcode = $? >> 8;
    if ($exitcode) {
        print STDERR "${red_stderr}${bold_stderr}Deactivation of module ${module_to_deactivate} failed.${reset_stderr} Rename exited with $exitcode.\n";
        return 0;
    }
    else {
        unless ($quiet) {
            print "Module ${bold_stdout}${module_to_deactivate}${reset_stdout} deactivated.\n";
        }
    }

    return 1;
}

# Create the active module index for the currently activated module.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
#   $verbose_java: make the Java table printer use verbose output
# Return: 1 on success, 0 on failure
sub generate_active_module_index {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;
    my $verbose_java = shift;

    my %modules = get_module_to_status_hash($ast_path);
    my @activated_modules = get_activated_modules(\%modules);

    my $module = $activated_modules[0];
    my $full_path_to_modules_folder = File::Spec->catfile($ast_path, $MODULES_FOLDER, "$module" . $MODULE_SUFFIX);
    my $full_path_to_log4j = File::Spec->catfile($ast_path, $ast_log_subsystem::LOG4J_FILE_PATH);
    my $full_index_path = File::Spec->catfile($ast_path, $ACTIVE_INDEX_PATH);
    my @java_command = ();
    push @java_command, "java";
    push @java_command, "-Xms2G";
    push @java_command, "-Xmx2G";
    push @java_command, "-cp";
    push @java_command, "${full_path_to_modules_folder}";
    push @java_command, "-Dlog4j.configuration=file:${full_path_to_log4j}";
    push @java_command, "${INDEX_WRITER_CLASS}";
    push @java_command, "${full_index_path}";

    if ($verbose_java && !$quiet) {
        push @java_command, "--verbose";
    }

    if (scalar @activated_modules == 0) {
        ast_utilities::error_output($program_name, 'could not generate index');
        print STDERR "No active modules found\n";
        print STDERR "Try '${bold_stderr}${ast_utilities::CONFIG_PROGRAM} list${reset_stderr}' to see all installed modules.\n";
        print STDERR "Then try '${bold_stderr}${ast_utilities::CONFIG_PROGRAM} activate <module>${reset_stderr}' to activate.\n";
        return 0;
    }

    unless ($quiet) {
        print "Generating new index...\n";
    }

    system {$java_command[0]} @java_command;
    my $exitcode = $? >> 8;
    unless ($exitcode == 0) {
        ast_utilities::error_output($program_name, 'could not generate index');
        return 0;
    }

    unless ($quiet) {
        print "New index successfully generated.\n";
    }

    return 1;
}

# Delete the active module index.
# Params:
#   $ast_path: the path to the atlas-shell-tools data folder
#   $program_name: the name of the calling program
#   $quiet: suppress non-essential output
# Return: 1 on success, 0 on failure
sub remove_active_module_index {
    my $ast_path = shift;
    my $program_name = shift;
    my $quiet = shift;

    my $full_index_path = File::Spec->catfile($ast_path, $ACTIVE_INDEX_PATH);
    unlink $full_index_path;

    unless ($quiet) {
        print "Cleared index.\n";
    }
}

# Write a metadata hash for a module.
# Params:
#   $modules_folder: the path to the module folder
#   $module_basename: the module basename
#   $metadata_ref: a reference to the metadata hash
#   $program_name: the name of the calling program
# Return: 1 on success, 0 on failure
sub write_module_metadata {
    my $modules_folder = shift;
    my $module_basename = shift;
    my $metadata_ref = shift;
    my $program_name = shift;

    my %metadata = %{$metadata_ref};

    my $module_new_path_metadata =
        File::Spec->catfile($modules_folder, $module_basename . $METADATA_SUFFIX);

    open my $metadata_handle, '>', "$module_new_path_metadata";
    if (defined $metadata{$SOURCE_KEY}) {
        print $metadata_handle $SOURCE_KEY . $METADATA_SEPARATOR . $metadata{$SOURCE_KEY} . "\n";
    }
    else {
        ast_utilities::warn_output($program_name, "bad metadata: missing SOURCE_KEY");
        close $metadata_handle;
        unlink $module_new_path_metadata;
        return 0;
    }
    if (defined $metadata{$URI_KEY}) {
        print $metadata_handle $URI_KEY . $METADATA_SEPARATOR . $metadata{$URI_KEY} . "\n";
    }
    else {
        ast_utilities::warn_output($program_name, "bad metadata: missing URI_KEY");
        close $metadata_handle;
        unlink $module_new_path_metadata;
        return 0;
    }
    if (defined $metadata{$DATE_TIME_KEY}) {
        print $metadata_handle $DATE_TIME_KEY . $METADATA_SEPARATOR . $metadata{$DATE_TIME_KEY} . "\n";
    }
    else {
        ast_utilities::warn_output($program_name, "bad metadata: missing DATE_TIME_KEY");
        close $metadata_handle;
        unlink $module_new_path_metadata;
        return 0;
    }
    if (defined $metadata{$REPO_NAME_KEY}) {
        print $metadata_handle $REPO_NAME_KEY . $METADATA_SEPARATOR . $metadata{$REPO_NAME_KEY} . "\n";
    }
    if (defined $metadata{$REPO_REF_KEY}) {
        print $metadata_handle $REPO_REF_KEY . $METADATA_SEPARATOR . $metadata{$REPO_REF_KEY} . "\n";
    }
    if (defined $metadata{$REPO_COMMIT_KEY}) {
        print $metadata_handle $REPO_COMMIT_KEY . $METADATA_SEPARATOR . $metadata{$REPO_COMMIT_KEY} . "\n";
    }
    close $metadata_handle;

    return 1;
}

# Perl modules must return a value. Returning a value perl considers "truthy"
# signals that the module loaded successfully.
1;
