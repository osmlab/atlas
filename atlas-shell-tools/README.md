# Atlas Shell Tools
A command line interface for [`osmlab/atlas`](https://github.com/osmlab/atlas) (and downstream repositories) tools.

##### Table of Contents
1. [What are the Atlas Shell Tools?](#whatare)
2. [Installation](#install)
   * [Auto-install for `bash` users](#bashinstall)
   * [Auto-install for `zsh` users](#zshinstall)
   * [Manual install](#manualinstall)
3. [Managing Your Installation](#managing)
   * [Installing Modules](#moduleinstall)
   * [Switching The Active Module](#moduleswitch)
   * [Viewing Command Documentation](#viewdocs)
   * [Saving Command Option Presets](#savepresets)
   * [And Much More](#andmuchmore)
4. [Creating Your Own Command](#creating)
5. [Updating The Commands And Tools](#updating)

<a name="whatare"/>
## What are the Atlas Shell Tools?
Atlas Shell Tools is a command line interface for executing commands defined in [`osmlab/atlas`](https://github.com/osmlab/atlas)
and its downstream repositories (like [`osmlab/atlas-generator`](https://github.com/osmlab/atlas-generator)). It provides Unix-like option parsing,
autocomplete functionality, a feature-ful option preset system (for commands that need lots
of options), module/repository management, and much more.

To get a basic installation running, see the [**Installation**](#install) section.

You can manage your installation with the `atlas-config(1)` command. Management features include:
1. Installing modules (JARs containing command classes) from various repositories
2. Switching the active module
3. Viewing command documentation
4. Saving command option presets
5. And much more...

See the [**Managing Your Installation**](#managing) section for more information.

To build a command, all you need to do is subclass `AbstractAtlasShellToolsCommand`(or one of its further subclasses) - 
then your command will be automatically integrated into the tools! For more information on this,
see the [**Creating Your Own Command**](#creating) section.

<a name="install"/>
## Installation
Atlas Shell Tools comes with some quick install scripts for users of select shells.
Note that the quick install scripts prompt to modify your shell's startup file(s)
with some code Atlas Shell Tools needs to run (`.bash_profile` and `~/.bashrc`
for `~/bash`, `~/.zshrc` and `~/.zshenv` for `zsh`). If you do not want this behaviour
and just want to configure your startup files yourself, select 'n' at the appropriate prompts.

<a name="bashinstall"/>
#### Auto-install for `bash` users:
```
$ curl -O https://raw.githubusercontent.com/osmlab/atlas/master/atlas-shell-tools/quick_install_bash.sh
# Inspect the downloaded file and ensure you are satisfied it is safe to run:
$ vim quick_install_bash.sh
$ sh quick_install_bash.sh
# Answer the prompts, and restart your terminal once this finishes to get started!
```

<a name="zshinstall"/>
#### Auto-install for `zsh` users:
```
$ curl -O https://raw.githubusercontent.com/osmlab/atlas/master/atlas-shell-tools/quick_install_zsh.sh
# Inspect the downloaded file and ensure you are satisfied it is safe to run:
$ vim quick_install_zsh.sh
$ sh quick_install_zsh.sh
# Answer the prompts, and restart your terminal once this finishes to get started!
```

<a name="manualinstall"/>
#### Manual install:
If you are not running one of the supported shells, or you want to manually
install Atlas Shell Tools, please follow these steps:
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
Next, open whichever configuration file your shell uses to set environment variables
(e.g. `~/.bash_profile` for `bash`, or `~/.zshenv` for `zsh`). Export the following
environment variables using your shell's equivalent `export` statement.
```
# In bash/zsh we can use 'export', other shells may use different syntax

# Point ATLAS_SHELL_TOOLS_HOME at the 'atlas-shell-tools' subfolder within your 'atlas-shell-tools' installation
export ATLAS_SHELL_TOOLS_HOME=/path/to/atlas-shell-tools/atlas-shell-tools

# configure your PATH
export PATH="$PATH:$ATLAS_SHELL_TOOLS_HOME/scripts"
```
##### Autocomplete support
Additionally, Atlas Shell Tools supports autocomplete for `bash` and `zsh`
through [ast_completions.bash](https://github.com/osmlab/atlas/blob/master/atlas-shell-tools/ast_completions.bash)
and [ast_completions.zsh](https://github.com/osmlab/atlas/blob/master/atlas-shell-tools/ast_completions.zsh),
respectively. To get these set up, you'll need to source them in your shell's appropriate
startup file (`~/.bashrc` for `bash` or `~/.zshrc` for `zsh`).

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

<a name="managing"/>
## Managing Your Installation
Both `atlas(1)` and `atlas-config(1)` provide numerous ways to manage your installation. Some common
operations include:
<a name="moduleinstall"/>
#### Installing A New Module From A Repo
Suppose the git repository `me/my-repo` depends on [`osmlab/atlas`](https://github.com/osmlab/atlas) and contains a command implementation you would
like to run from the CLI. First, you can save the `my-repo` information with the `repo` subcommand of `atlas-config(1)`:
```
$ atlas-config repo add my-repo 'https://github.com/me/my-repo'
```
Then, to install a new module based on `my-repo`'s `main` branch, simply run:
```
$ atlas-config repo install my-repo
```
See the `atlas-config-repo(1)` man page for more information about repos.
<a name="moduleswitch"/>
#### Switching The Active Module
After running the command from `me/my-repo`, you may want to switch back to another repo, like `me/my-other-repo`.
Assuming you have a module from `me/my-other-repo` installed, this is straightforward. First, you can list your installed
modules with:
```
$ atlas-config list
```
Then, assuming you see the module you want - for sake of example say it's called `my-other-repo-ef3381a` - run:
```
$ atlas-config activate my-other-repo-ef3381a
```
You can check to see that it worked with `$ atlas-config list` again.
<a name="viewdocs"/>
#### Viewing Command Documentation
Atlas Shell Tools comes with extensive documentation, both in the form of man pages as well as subcommand documentation.
To see all available man pages, check the Atlas Shell Tools index man page: `atlas-shell-tools(7)`:
```
$ man atlas-shell-tools
```
From there you can jump to whichever page interests you. Subcommand implementations also provide their own documentation, part
of which is auto-generated and part of which can be optionally supplied by the command author. To view this documentation, try the
subcommand's `--help` option (every subcommand automatically responds to `--help`):
```
$ atlas some-command --help
```
OR
```
$ atlas --help some-command
```
<a name="savepresets"/>
#### Saving Command Option Presets
Some commands require many options, and option presets can make repeated use much easier. Assuming your command is called `my-command`,
you might save an option preset like:
```
$ atlas --save-preset my-command-preset-1 my-command arg1 --opt1=optarg1 --opt2
```
You could then run
```
$ atlas --preset my-command-preset-1 my-command arg1
```
and Atlas Shell Tools would fill in `--opt1=optarg1 --opt2` for you automatically. There is much more to be said about the presets feature.
Please see the `atlas-presets(7)`, `atlas-config-preset(1)`, and `atlas(1)` man pages for all the details.
<a name="andmuchmore"/>
#### And Much More...
There are many more Atlas Shell Tools features just waiting to be found. Feel free to peruse all the man pages
available in `atlas-shell-tools(7)`.

<a name="creating"/>
## Creating Your Own Command
To create a new command for Atlas Shell Tools, simply create a class that `extends`
[AbstractAtlasShellToolsCommand](https://github.com/osmlab/atlas/blob/master/src/main/java/org/openstreetmap/atlas/utilities/command/abstractcommand/AbstractAtlasShellToolsCommand.java).
Once you fill in the abstract methods appropriately (and add a main method), you should build a
fat JAR file containing your command, and install it with:
```
$ atlas-config install /path/to/JARfile.jar --symlink
```
This will install the JAR file to the module workspace using a symlink, so iterative
changes to the JAR will be automatically picked up by Atlas Shell Tools.

For a comprehensive example of the `AbstractAtlasShellToolsCommand` API, check out the
demo class [DemoSubcommand](https://github.com/osmlab/atlas/blob/master/src/main/java/org/openstreetmap/atlas/utilities/command/subcommands/AtlasShellToolsDemoCommand.java).
This class demonstrates how to implement the abstract methods, as well as how to structure
the main method.

<a name="updating"/>
## Updating The Commands And Tools
If you just want to quickly update everything, go ahead and run:
```
$ atlas-config update
```
This will update the toolkit, which includes the code for `atlas(1)`, `atlas-config(1)`, and the various man pages
(see the `atlas-glossary(7)` man page for a definition of the toolkit).

Then, to update the basic commands by installing a new module from the default [`osmlab/atlas`](https://github.com/osmlab/atlas)
repo, run:
```
$ atlas-config repo install atlas
```
If you have any other repos you'd like to update, you can run a `repo install` on those as well to get
the latest versions of any commands.
