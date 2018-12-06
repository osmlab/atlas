
# What is this script for?
#
# This script provides autocomplete functionality for Atlas Shell Tools in the
# bash shell.
#
# How do I use it?
#
# Run the following command:
#    $ source /path/to/ash_completions.bash
# Then add 'source /path/to/ash_completions.bash' to your '~/.bashrc'
# file to pick up the completions in every new shell!


# Return "true" if bash major version is greater than or equal to 4.
# Bash4 has a better auto-complete API, so using bash4 if possible is better.
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

_complete ()
{
    local completion_mode="default";
    if [ "$1" == "ash" ];
    then
        local completion_mode="__completion_ash__"
    elif [ "$1" == "ash-config" ];
    then
        local completion_mode="__completion_ashcfg__"
    fi

    if [ "$completion_mode" == "default" ];
    then
        echo "complete error: ${completion_mode} was still default"
        return 1
    fi

    # disable readline default autocompletion, we are going to customize
    if [ $(is_bash_at_least_version_4) == "true" ];
    then
        compopt +o default
    fi

    local reply=$(ash-config "${completion_mode}" "${COMP_WORDS[@]}");

    if [ "$reply" = "__ash_sentinel_complete_filenames__" ];
    then
        # re-enable bash default completion for filenames
        if [ $(is_bash_at_least_version_4) == "true" ];
        then
            compopt -o default
            COMPREPLY=()
        else
            local cur=${COMP_WORDS[COMP_CWORD]}

            # We must locally set IFS to '\n' in case there are filenames with whitespace.
            # Without this, a filename like "file with spaces" would present itself
            # as 3 discrete completion options, "file", "with", and "spaces".
            local IFS=$'\n'

            COMPREPLY=($(compgen -o filenames -f -- $cur))
        fi
        return 0
    else
        COMPREPLY=(${reply})
    fi
}

complete -o filenames -o bashdefault -F _complete ash
complete -o filenames -o bashdefault -F _complete ash-config