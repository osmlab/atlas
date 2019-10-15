
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

# TODO figure out how to do zsh completions

_complete_atlas_shell_tools_zsh ()
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

    local reply=$(atlas-config "${completion_mode}" "${COMP_WORDS[@]}");

    if [ "$reply" = "__atlas-shell-tools_sentinel_complete_filenames__" ];
    then
        COMPREPLY=()
        return 0
    else
        COMPREPLY=(${reply})
    fi
}

autoload compinit
compinit
autoload bashcompinit
bashcompinit
complete -o filenames -o default -F _complete_atlas_shell_tools_zsh atlas
complete -o filenames -o default -F _complete_atlas_shell_tools_zsh atlas-config
