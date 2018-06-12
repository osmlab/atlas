#!/usr/bin/env bash

# general case script abort if a command fails
# this can be overridden with a custom error message using '|| err_shutdown'
set -e
set -o pipefail

### define utility functions ###
################################
err_shutdown() {
    echo "venv.sh: ERROR: $1"
    deactivate
    exit 1
}


### check to prevent users from running this script directly ###
################################################################
if [ "$1" != "ranFromGradle" ];
then
    err_shutdown "this script should be run using the atlas gradle task 'formatPyatlas'"
fi


### set up variables to store directory names ###
#################################################
gradle_project_root_dir="$(pwd)"
pyatlas_dir="pyatlas"
pyatlas_srcdir="pyatlas"
pyatlas_root_dir="$gradle_project_root_dir/$pyatlas_dir"
venv_path="$pyatlas_root_dir/__pyatlas_venv__"


### abort the script if the pyatlas source folder is not present ###
####################################################################
if [ ! -d "$pyatlas_root_dir/$pyatlas_srcdir" ];
then
    err_shutdown "pyatlas source folder not found"
fi


### exit the script if the venv folder already exists ###
#########################################################
if [ -d "$venv_path" ];
then
    echo "INFO: $venv_path exists. Proceeding. Run './gradlew cleanPyatlas' to refresh."
    exit 0
fi


### determine if virtualenv is installed ###
############################################
if command -v virtualenv;
then
    virtualenv_command="$(command -v virtualenv)"
else
    err_shutdown "'command -v virtualenv' returned non-zero exit status"
fi


### set up the virtual environment ###
######################################
echo "Setting up pyatlas venv..."
venv_path="$pyatlas_root_dir/__pyatlas_venv__"
if ! ${virtualenv_command} --python=python2.7 "$venv_path";
then
    err_shutdown "virtualenv command returned non-zero exit status"
fi
