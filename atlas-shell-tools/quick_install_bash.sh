#!/bin/sh

# Define a prompt function for re-use
prompt_yn_was_yes() {
    prompt=$1
    while true;
    do
        echo "$prompt"
	# TODO FIXME this infinite loops on EOF (CTRL-D)
        read -r answer
        if [ "$answer" != "${answer#[Yy]}" ];
        then
            # case user entered 'y'
            return 0
        elif [ "$answer" != "${answer#[Nn]}" ];
        then
            # case user entered 'n'
            return 1
        fi
        echo "Please enter 'y' or 'n'..."
    done
}

# Utilize POSIX sh features ONLY for installation. Also, we exit the script
# on any error.
set -e

# Grab the install location from first argument if present.
install_location=$1

# Verify that you have programs needed by atlas-shell-tools
if ! command -v less > /dev/null;
then
    echo "Error: atlas-shell-tools requires the 'less' paging program."
    # Unfortunately, we cannot use $LINENO in POSIX sh. Make sure to manually
    # maintain this line number.
    echo "To install anyway, open $0 and comment out check on lines 31-38."
    exit 1
fi

if ! command -v man > /dev/null;
then
    echo "Error: atlas-shell-tools requires the 'man' program."
    # Unfortunately, we cannot use $LINENO in POSIX sh. Make sure to manually
    # maintain this line number.
    echo "To install anyway, open $0 and comment out check on lines 40-47."
    exit 1
fi

if ! command -v vim > /dev/null;
then
    echo "Error: atlas-shell-tools requires the 'vim' editor."
    # Unfortunately, we cannot use $LINENO in POSIX sh. Make sure to manually
    # maintain this line number.
    echo "To install anyway, open $0 and comment out check on lines 49-56."
    exit 1
fi

# Check the install location. If blank, we will use $HOME as default after
# prompting the user for confirmation.
if [ -z "$install_location" ];
then
    echo "Using the value of \$HOME ($HOME) as install location."
    echo "If an alternative location is desired, select 'n' and try:"
    echo
    echo "    \$ $0 /install/path"
    echo
    if prompt_yn_was_yes "OK to continue (y/n)?";
    then
        install_location="$HOME"
    else
        exit 0
    fi
fi

if [ ! -d "$install_location" ];
then
    echo "Error: $install_location does not exist"
    exit 1
fi

if [ ! -w "$install_location" ];
then
    echo "Error: you do not have write permissions for $install_location"
    exit 1
fi

base_folder="atlas-shell-tools"
full_installation_path="$install_location/$base_folder"

if [ -e "$full_installation_path" ];
then
    echo "Error: $full_installation_path already exists"
    exit 1
fi

# TODO: change this URL to the main atlas dev url
git clone https://github.com/lucaspcram/atlas.git "$full_installation_path"
cd "$full_installation_path"
# TODO: change this to 'git checkout dev'
git checkout ast
./gradlew clean shaded -x check -x javadoc
chmod +x atlas-shell-tools/scripts/atlas atlas-shell-tools/scripts/atlas-config

# Modify the bash startup files with appropriate settings
start_startup_line="# atlas-shell-tools startup: added automatically by quick_install_bash.sh"
end_startup_line="# END atlas-shell-tools startup"
execute_bashrc_comment="# Generated by atlas-shell-tools. This should ALWAYS be the last line of ~/.bash_profile."
export_home_line="export ATLAS_SHELL_TOOLS_HOME=\"$full_installation_path/atlas-shell-tools\""
export_path_line="export PATH=\"\$PATH:\$ATLAS_SHELL_TOOLS_HOME/scripts\""
source_line="source \"\$ATLAS_SHELL_TOOLS_HOME/ast_completions.bash\""
# shellcheck disable=SC2016
execute_bashrc='if [ -f ${HOME}/.bashrc ]; then source ${HOME}/.bashrc; fi'

echo
echo "About to append ~/.bash_profile with:"
echo
echo "    $start_startup_line";
echo "    $export_home_line";
echo "    $export_path_line";
echo "    $end_startup_line";
echo
if prompt_yn_was_yes "Is this OK (y/n)?";
then
    {
        echo;
        echo "$start_startup_line";
        echo "$export_home_line";
        echo "$export_path_line";
        echo "$end_startup_line";
        echo;
    } >> "$HOME/.bash_profile"
fi
echo
echo "About to append ~/.bashrc with:"
echo
echo "    $start_startup_line";
echo "    $source_line";
echo "    $end_startup_line";
echo
if prompt_yn_was_yes "Is this OK (y/n)?";
then
    {
        echo;
        echo "$start_startup_line";
        echo "$source_line";
        echo "$end_startup_line";
        echo;
    } >> "$HOME/.bashrc"
fi
echo
echo "About to append ~/.bash_profile with:"
echo
echo "    $execute_bashrc_comment"
echo "    $execute_bashrc";
echo
if prompt_yn_was_yes "Is this OK (y/n)?";
then
    {
        echo;
        echo "$execute_bashrc_comment"
        echo "$execute_bashrc";
        echo;
    } >> "$HOME/.bash_profile"
fi

# Install the core Atlas module
find "$full_installation_path/build/libs" -type f -name "*-shaded.jar" -exec ./atlas-shell-tools/scripts/atlas-config install \{\} \;

# Complete the installation
echo
echo "Installation complete."
echo "Restart your terminal program, then try 'man atlas-shell-tools' to get started."
