#!/bin/sh

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
    echo "To install anyway, open $0 and comment out check on lines 11-18."
    exit 1
fi

if ! command -v man > /dev/null;
then
    echo "Error: atlas-shell-tools requires the 'man' program."
    # Unfortunately, we cannot use $LINENO in POSIX sh. Make sure to manually
    # maintain this line number.
    echo "To install anyway, open $0 and comment out check on lines 20-27."
    exit 1
fi

if ! command -v vim > /dev/null;
then
    echo "Error: atlas-shell-tools requires the 'vim' editor."
    # Unfortunately, we cannot use $LINENO in POSIX sh. Make sure to manually
    # maintain this line number.
    echo "To install anyway, open $0 and comment out check on lines 29-36."
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
    echo "OK to continue (y/n)? "
    read -r answer
    if [ "$answer" != "${answer#[Yy]}" ];
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

export_home_line="export ATLAS_SHELL_TOOLS_HOME=\"$full_installation_path/atlas-shell-tools\""
export_path_line="export PATH=\"\$PATH:\$ATLAS_SHELL_TOOLS_HOME/scripts\""
# We use external "cat" process here since "read -r -d" is not POSIX sh compatible
execute_bashrc=$(cat <<'EOF'
if [ -f ${HOME}/.bashrc ];
then
    source ${HOME}/.bashrc
fi
EOF
)
source_line="source \"\$ATLAS_SHELL_TOOLS_HOME/ast_completions.bash\""
{
    echo;
    echo '# atlas-shell-tools startup: added automatically by quick_install_bash.sh';
    echo "$export_home_line";
    echo "$export_path_line";
    echo "$execute_bashrc";
    echo "# END atlas-shell-tools startup";
    echo;
} >> "$HOME/.bash_profile"
{
    echo;
    echo '# atlas-shell-tools startup: added automatically by quick_install_bash.sh';
    echo "$source_line";
    echo "# END atlas-shell-tools startup";
    echo;
} >> "$HOME/.bashrc"
# Install the core Atlas module
find "$full_installation_path/build/libs" -type f -name "*-shaded.jar" -exec ./atlas-shell-tools/scripts/atlas-config install \{\} \;

echo
echo "Installation complete."
echo "Restart your terminal program, then try 'man atlas-shell-tools' to get started."
