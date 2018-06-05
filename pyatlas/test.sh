#!/usr/bin/env bash

# general case script abort if a command fails
# this can be overridden with a custom error message using '|| err_shutdown'
set -e
set -o pipefail

### define utility functions ###
################################
err_shutdown() {
    echo "test.sh: ERROR: $1"
    deactivate
    exit 1
}
#################################################################


### check to prevent users from running this script directly ###
################################################################
if [ "$1" != "ranFromGradle" ];
then
    err_shutdown "this script should be run using the atlas gradle task 'testPyatlas'"
fi
#################################################################


### set up variables to store directory names ###
#################################################
gradle_project_root_dir="$(pwd)"
pyatlas_dir="pyatlas"
pyatlas_srcdir="pyatlas"
pyatlas_testdir="unit_tests"
pyatlas_root_dir="$gradle_project_root_dir/$pyatlas_dir"
venv_path="$pyatlas_root_dir/__pyatlas_venv__"
#################################################################


### abort the script if the pyatlas tests folder is not present ###
###################################################################
if [ ! -d "$pyatlas_root_dir/$pyatlas_testdir" ];
then
    err_shutdown "pyatlas tests folder not found"
fi
####################################################################


### run the tests ###
#####################
# start the venv
if [ ! -d "$venv_path" ];
then
    err_shutdown "missing $venv_path"
fi
# shellcheck source=/dev/null
source "$venv_path/bin/activate"

# enter the pyatlas project directory so the unittest code can discover tests
pushd "$pyatlas_root_dir"
pip install -e "$pyatlas_root_dir"
echo "Discovering and running unit tests..."
echo "----------------------------------------------------------------------"
python -m unittest discover -v -s "$pyatlas_testdir" || err_shutdown "a test failed, aborting early..."
# get back to gradle project directory
popd

# shutdown the venv
deactivate
#################################################################
