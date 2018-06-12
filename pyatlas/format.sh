#!/usr/bin/env bash

# general case script abort if a command fails
# this can be overridden with a custom error message using '|| err_shutdown'
set -e
set -o pipefail

### define utility functions ###
################################
err_shutdown() {
    echo "format.sh: ERROR: $1"
    deactivate
    exit 1
}


### check to prevent users from running this script directly ###
################################################################
if [ "$1" != "ranFromGradle" ];
then
    err_shutdown "this script should be run using the atlas gradle task 'formatPyatlas'"
fi


### get CHECK or APPLY mode ###
###############################
format_mode=$2

### set up variables to store directory names ###
#################################################
gradle_project_root_dir="$(pwd)"
pyatlas_dir="pyatlas"
pyatlas_srcdir="pyatlas"
pyatlas_testdir="unit_tests"
pyatlas_root_dir="$gradle_project_root_dir/$pyatlas_dir"
venv_path="$pyatlas_root_dir/__pyatlas_venv__"
pyatlas_format_script="yapf_format.py"


### abort the script if the pyatlas source folder is not present ###
####################################################################
if [ ! -d "$pyatlas_root_dir/$pyatlas_srcdir" ];
then
    err_shutdown "pyatlas source folder not found"
fi


### format the module source code ###
#####################################
# start the venv
if [ ! -d "$venv_path" ];
then
    err_shutdown "missing $venv_path"
fi
# shellcheck source=/dev/null
source "$venv_path/bin/activate"

# enter the pyatlas project directory so the formatting script will work
pushd "$pyatlas_root_dir"
pip install yapf==0.22.0

if ! python "$pyatlas_format_script" "$pyatlas_srcdir" "$format_mode";
then
    err_shutdown "CHECK format step failed: run './gradlew applyFormatPyatlas'"
fi

if ! python "$pyatlas_format_script" "$pyatlas_testdir" "$format_mode";
then
    err_shutdown "CHECK format step failed: run './gradlew applyFormatPyatlas'"
fi

# get back to gradle project directory
popd

# shutdown the venv
deactivate
