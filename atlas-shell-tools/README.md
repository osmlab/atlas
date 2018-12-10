# Atlas Shell Tools
A command line interface for `atlas`

## What are the Atlas Shell Tools?
`atlas-shell-tools` is a command line interface for executing commands defined in `atlas` and its downstream repositories (like `atlas-generator`). It provides Unix-like option parsing, autocomplete functionality, a feature-full option preset system (for commands that need lots of options), module/repository management, and much more.

To get a basic installation running, see the **Installation** section.

To build a command, all you need to do is subclass `AbstractAtlasShellToolsCommand` - then your command will be automatically integrated into the tools! For more information on this, see the **Creating A Command** section.

## Installation
`atlas-shell-tools` comes with some quick install scripts for users of select shells. If you are using `bash`, try the following commands:
```
$ curl -O https://raw.githubusercontent.com/osmlab/atlas/dev/atlas-shell-tools/quick_install_bash.sh
# Inspect the downloaded file and ensure you are satisfied it is safe to run:
$ vim quick_install_bash.sh
$ ./quick_install_bash.sh
# Answer the prompts, and restart your terminal once this finishes to get started!
```

**TODO actually implement zsh support**

For `zsh` users, try running:
```
$ curl -O https://raw.githubusercontent.com/osmlab/atlas/dev/atlas-shell-tools/quick_install_zsh.sh
# Inspect the downloaded file and ensure you are satisfied it is safe to run:
$ vim quick_install_zsh.sh
$ ./quick_install_zsh.sh
# Answer the prompts, and restart your terminal once this finishes to get started!
```

Note that both of these scripts prompt to modify your shell's startup file(s)
(`.bash_profile` and `~/.bashrc` for `~/bash`, `~/.zshrc` and `~/.zshenv` for `zsh`) with some code
`atlas-shell-tools` needs to run. If you do not want this behaviour and just want
to configure your startup scripts yourself, select 'n' at appropriate the prompts.

If you are not running one of the supported shells, or you want to manually
install `atlas-shell-tools`, please see the following steps.

