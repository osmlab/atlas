# Atlas Shell Tools
A command line interface for `atlas`

## What are the Atlas Shell Tools?
`atlas-shell-tools` is a command line interface for executing commands defined in `atlas` and its downstream repositories (like `atlas-generator`). It provides Unix-like option parsing, autocomplete functionality, a feature-full option preset system (for commands that need lots of options), module/repository management, and much more.

To get a basic installation running, see the **Installation** section.

To build a command, all you need to do is subclass `AbstractAtlasShellToolsCommand` - then your command will be automatically integrated into the tools! For more information on this, see the **Creating A Command** section.

## Installation
`atlas-shell-tools` comes with some quick install scripts for users of select shells.
Note that the quick install scripts prompt to modify your shell's startup file(s)
with some code `atlas-shell-tools` needs to run (`.bash_profile` and `~/.bashrc`
for `~/bash`, `~/.zshrc` and `~/.zshenv` for `zsh`). If you do not want this behaviour
and just want to configure your startup files yourself, select 'n' at the appropriate prompts.



#### For `bash` users:
```
$ curl -O https://raw.githubusercontent.com/osmlab/atlas/master/atlas-shell-tools/quick_install_bash.sh
# Inspect the downloaded file and ensure you are satisfied it is safe to run:
$ vim quick_install_bash.sh
$ sh quick_install_bash.sh
# Answer the prompts, and restart your terminal once this finishes to get started!
```

#### For `zsh` users:
```
$ curl -O https://raw.githubusercontent.com/osmlab/atlas/master/atlas-shell-tools/quick_install_zsh.sh
# Inspect the downloaded file and ensure you are satisfied it is safe to run:
$ vim quick_install_zsh.sh
$ sh quick_install_zsh.sh
# Answer the prompts, and restart your terminal once this finishes to get started!
```

#### Manual install
If you are not running one of the supported shells, or you want to manually
install `atlas-shell-tools`, please follow the below steps.
##### Building and Installing
```
$ cd /path/to/desired/install/location
$ git clone https://github.com/osmlab/atlas.git atlas-shell-tools
$ cd atlas-shell-tools
$ git checkout master
$ ./gradlew clean shaded -x check -x javadoc
$ chmod +x ./atlas-shell-tools/scripts/atlas ./atlas-shell-tools/scripts/atlas-config
$ ./atlas-shell-tools/scripts/atlas-config repo add atlas https://github.com/osmlab/atlas.git
$ ./atlas-shell-tools/scripts/atlas-config repo install atlas
```
##### Setting up your shell startup file
Next, open whichever configuration file your shell uses to set environment variables (e.g. `~/.bash_profile` for `bash`, or `~/.zshenv` for `zsh`). Export the following environment variables using your shell's equivalent `export` statement.
```
# In bash/zsh we can use 'export', other shells may use different syntax

# Point ATLAS_SHELL_TOOLS_HOME at the 'atlas-shell-tools' subfolder within your 'atlas-shell-tools' installation
export ATLAS_SHELL_TOOLS_HOME=/path/to/atlas-shell-tools/atlas-shell-tools

# configure your PATH
export PATH="$PATH:$ATLAS_SHELL_TOOLS_HOME/scripts"
```
##### Autocomplete support
Additionally, `atlas-shell-tools` supports autocomplete for `bash` and `zsh` through [ast_completions.bash](https://github.com/osmlab/atlas/blob/master/atlas-shell-tools/ast_completions.bash) and [ast_completions.zsh](https://github.com/osmlab/atlas/blob/master/atlas-shell-tools/ast_completions.zsh), respectively. To get these set up, you'll need to source them in your shell's appropriate startup file (`~/.bashrc` for `bash` or `~/.zshrc` for `zsh`).

An example for `bash`:
```
##### ~/.bashrc file #####
#
# other stuff here....
#
source "$ATLAS_SHELL_TOOLS_HOME/ast_completions.bash"
```

An example for `zsh`:
```
##### ~/.zshrc file #####
#
# other stuff here....
#
source "$ATLAS_SHELL_TOOLS_HOME/ast_completions.zsh"
```

## Creating A Command
To create a new command for `atlas-shell-tools`, simply create a class that `extends` [AbstractAtlasShellToolsCommand](https://github.com/osmlab/atlas/blob/master/src/main/java/org/openstreetmap/atlas/utilities/command/abstractcommand/AbstractAtlasShellToolsCommand.java). Once you fill in the abstract methods appropriately (and add a main method), you should build a fat JAR file containing your command, and install it with: 
```
$ atlas-config install /path/to/JARfile.jar --symlink
```
This will install the JAR file to the module workspace using a symlink, so iterative changes to the JAR will be automatically picked up by `atlas-shell-tools`.

For a comprehensive example of the `AbstractAtlasShellToolsCommand` API, check out the demo class [DemoSubcommand](https://github.com/osmlab/atlas/blob/master/src/main/java/org/openstreetmap/atlas/utilities/command/subcommands/AtlasShellToolsDemoCommand.java). This class demonstrates how to implement the abstract methods, as well as how to structure the main method.
