
# What is this script for?
#
# This script provides autocomplete functionality for Atlas Shell Tools in the
# zsh shell.
#
# How do I use it?
#
# Run the following command:
#    $ source "$ATLAS_SHELL_TOOLS_HOME/ast_completions.zsh"
# Then add 'source "$ATLAS_SHELL_TOOLS_HOME/ast_completions.zsh"' to your '~/.zshrc'
# file to pick up the completions in every new shell!

_complete_atlas_shell_tools_zsh ()
{
    local completion_mode="default";
    if [ "$1" = "atlas" ];
    then
        local completion_mode="__completion_atlas_zsh__"
    elif [ "$1" = "atlas-config" ];
    then
        local completion_mode="__completion_atlascfg_zsh__"
    fi

    if [ "$completion_mode" = "default" ];
    then
        echo "complete error: ${completion_mode} was still default"
        return 1
    fi

    local reply=$(atlas-config "${completion_mode}" "${COMP_CWORD}" "${COMP_WORDS[@]}");

    if [ "$reply" = "__atlas-shell-tools_sentinel_complete_filenames__" ];
    then
        local cur=${COMP_WORDS[COMP_CWORD]}

        # We must locally set IFS to '\n' in case there are filenames with whitespace.
        # Without this, a filename like "file with spaces" would present itself
        # as 3 discrete completion options, "file", "with", and "spaces".
        local IFS=$'\n'

        COMPREPLY=($(compgen -o filenames -f -- "$cur"))
        return 0
    else
        COMPREPLY=(${reply})
    fi
}

autoload compinit
compinit
autoload bashcompinit
bashcompinit
complete -o filenames -o bashdefault -F _complete_atlas_shell_tools_zsh atlas
complete -o filenames -o bashdefault -F _complete_atlas_shell_tools_zsh atlas-config
