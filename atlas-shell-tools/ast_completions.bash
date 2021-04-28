
# What is this script for?
#
# This script provides autocomplete functionality for Atlas Shell Tools in the
# bash shell.
#
# How do I use it?
#
# Run the following command:
#    $ source "$ATLAS_SHELL_TOOLS_HOME/ast_completions.bash"
# Then add 'source "$ATLAS_SHELL_TOOLS_HOME/ast_completions.bash"' to your '~/.bashrc'
# file to pick up the completions in every new shell!


# Return "true" if bash major version is greater than or equal to 4.
# Bash4 has a better auto-complete API, so using bash4 if possible is better.
is_bash_at_least_version_4 ()
{
    local version=$BASH_VERSION
    local major=$(echo "$version" | cut -d. -f1)
    if [ "$major" -ge "4" ];
    then
        echo "true"
    else
        echo "false"
    fi
}

_complete_atlas_shell_tools ()
{
    local completion_mode="default";
    if [ "$1" = "atlas" ];
    then
        local completion_mode="__completion_atlas__"
    elif [ "$1" = "atlas-config" ];
    then
        local completion_mode="__completion_atlascfg__"
    fi

    if [ "$completion_mode" = "default" ];
    then
        echo "complete error: ${completion_mode} was still default"
        return 1
    fi

    # disable readline default autocompletion, we are going to customize
    if [ "$(is_bash_at_least_version_4)" = "true" ];
    then
        compopt +o default
    fi

    local reply=$(atlas-config "${completion_mode}" "${COMP_CWORD}" "${COMP_WORDS[@]}");

    if [ "$reply" = "__atlas-shell-tools_sentinel_complete_filenames__" ];
    then
        # re-enable bash default completion for filenames
        if [ "$(is_bash_at_least_version_4)" = "true" ];
        then
            compopt -o default
            COMPREPLY=()
        else
            local cur=${COMP_WORDS[COMP_CWORD]}

            # We must locally set IFS to '\n' in case there are filenames with whitespace.
            # Without this, a filename like "file with spaces" would present itself
            # as 3 discrete completion options, "file", "with", and "spaces".
            local IFS=$'\n'

            COMPREPLY=($(compgen -o filenames -f -- "$cur"))
        fi
        return 0
    else
        COMPREPLY=(${reply})
    fi
}

complete -o filenames -o bashdefault -F _complete_atlas_shell_tools atlas
complete -o filenames -o bashdefault -F _complete_atlas_shell_tools atlas-config
