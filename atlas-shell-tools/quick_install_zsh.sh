#!/bin/sh

# Define a prompt function for re-use
prompt_yn_was_yes() {
    prompt=$1
    while true;
    do
        echo "$prompt"
        # handle EOF case
        if ! read -r answer;
        then
            exit 0
        fi
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
    echo "To install anyway, open $0 and comment out check on lines 35-42."
    exit 1
fi

if ! command -v man > /dev/null;
then
    echo "Error: atlas-shell-tools requires the 'man' program."
    # Unfortunately, we cannot use $LINENO in POSIX sh. Make sure to manually
    # maintain this line number.
    echo "To install anyway, open $0 and comment out check on lines 44-51."
    exit 1
fi

if ! command -v vim > /dev/null;
then
    echo "Error: atlas-shell-tools requires the 'vim' editor."
    # Unfortunately, we cannot use $LINENO in POSIX sh. Make sure to manually
    # maintain this line number.
    echo "To install anyway, open $0 and comment out check on lines 53-60."
    exit 1
fi

# Check the install location. If blank, we will use $HOME as default after
# prompting the user for confirmation.
if [ -z "$install_location" ];
then
    echo "Using the value of \$HOME ($HOME) as install location."
    echo "If an alternative location is desired, select 'n' and try:"
    echo
    echo "    \$ sh quick_install_bash.sh /install/path"
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

git clone https://github.com/osmlab/atlas.git "$full_installation_path"
cd "$full_installation_path"
git checkout master
chmod +x atlas-shell-tools/scripts/atlas atlas-shell-tools/scripts/atlas-config
export ATLAS_SHELL_TOOLS_HOME="$full_installation_path/atlas-shell-tools"
export PATH="$PATH:$ATLAS_SHELL_TOOLS_HOME/scripts"

# Install the core Atlas module using a repo
./atlas-shell-tools/scripts/atlas-config repo add atlas https://github.com/osmlab/atlas.git master
./atlas-shell-tools/scripts/atlas-config repo install atlas

# Modify the bash startup files with appropriate settings
start_startup_line="# atlas-shell-tools startup: added automatically by quick_install_zsh.sh"
end_startup_line="# END atlas-shell-tools startup"
export_home_line="export ATLAS_SHELL_TOOLS_HOME=\"$full_installation_path/atlas-shell-tools\""
export_path_line="export PATH=\"\$PATH:\$ATLAS_SHELL_TOOLS_HOME/scripts\""
source_line="source \"\$ATLAS_SHELL_TOOLS_HOME/ast_completions.zsh\""

echo
echo "About to append ~/.zshenv with:"
echo
echo "    $start_startup_line";
echo "    $export_home_line";
echo "    $export_path_line";
echo "    $source_line";
echo "    $end_startup_line";
echo
if prompt_yn_was_yes "Is this OK (y/n)?";
then
    {
        echo;
        echo "$start_startup_line";
        echo "$export_home_line";
        echo "$export_path_line";
        echo "$source_line";
        echo "$end_startup_line";
        echo;
    } >> "$HOME/.zshenv"
fi
echo
echo "About to append ~/.zshrc with:"
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
    } >> "$HOME/.zshrc"
fi

# Complete the installation
echo
echo "Installation complete."
echo "Restart your terminal and try 'man atlas-shell-tools' to get started."
