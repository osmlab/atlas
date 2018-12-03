
# What is this script for?
#
# This script provides autocomplete functionality for 'closm' and 'closmcfg' in
# the bash shell.
#
# How do I use it?
#
# Run the following command:
#    $ source /path/to/closm_completions.bash
# Then add 'source /path/to/closm_completions.bash' to your '~/.bashrc'
# file to pick up the completions in every new shell!

is_bash_at_least_version_4 ()
{
    local version=$BASH_VERSION
    local major=$(echo $version | cut -d. -f1)
    if [ "$major" -ge "4" ];
    then
        echo "true"
    else
        echo "false"
    fi
}

_closm ()
{
    COMPREPLY=()
}

_closmcfg ()
{
    # disable bash default autocompletion, we are going to customize
    if [ $(is_bash_at_least_version_4) == "true" ];
    then
        compopt +o default
    fi
    COMPREPLY=()

    local word="${COMP_WORDS[COMP_CWORD]}";

    if [ "$COMP_CWORD" -eq 1 ];
    then
        # 'atlascfg <TAB><TAB>' completes subcommand names
        COMPREPLY=($(compgen -W "$(closmcfg __commands__)" -- "$word"));
    else
        # 'atlascfg COMMAND <TAB><TAB>' contextually completes args to COMMAND
        local words=("${COMP_WORDS[@]}")
        local subcommand=${words[1]}
        unset words[0]
        unset words[$COMP_CWORD]

        # 'install' is a special case, here we just want to complete filenames
        if [ "${subcommand}" = "install" ];
        then
            # re-enable bash default completion for filenames
            if [ $(is_bash_at_least_version_4) == "true" ];
            then
                compopt -o default
                COMPREPLY=()
            else
                local cur=${COMP_WORDS[COMP_CWORD]}
                local IFS=$'\n'
                COMPREPLY=($(compgen -o filenames -f -- $cur))
            fi
            return 0
        else
            local completions=$(closmcfg __completions__ "${words[@]}");
            COMPREPLY=($(compgen -W "$completions" -- "$word"));
        fi
    fi
}

complete -F _closm closm
complete -F _closmcfg closmcfg
