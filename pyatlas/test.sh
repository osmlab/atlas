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
pyatlas_dir="pyatlas"
pyatlas_srcdir="pyatlas"
pyatlas_testdir="unit_tests"
gradle_project_root_dir="$(pwd)"
pyatlas_root_dir="$gradle_project_root_dir/$pyatlas_dir"
#################################################################


### abort the script if the pyatlas source folder is not present ###
####################################################################
if [ ! -d "$pyatlas_root_dir/$pyatlas_srcdir" ];
then
    err_shutdown "pyatlas source folder not found"
fi
####################################################################


### determine if virtualenv is installed ###
############################################
if command -v virtualenv;
then
    virtualenv_command="$(command -v virtualenv)"
else
    err_shutdown "'command -v virtualenv' returned non-zero exit status"
fi
#################################################################


### test the module ###
#######################
# start the venv
echo "Setting up pyatlas venv..."
venv_path="$pyatlas_root_dir/__pyatlas_test_venv__"
if ! $virtualenv_command --python=python2.7 "$venv_path";
then
    err_shutdown "virtualenv command returned non-zero exit status"
fi
# shellcheck source=/dev/null
source "$venv_path/bin/activate"

# enter the pyatlas project directory so the unittest code can discover tests
pushd "$pyatlas_root_dir"
echo "Discovering and running unit tests..."
echo "----------------------------------------------------------------------"
python -m unittest discover -v "$pyatlas_testdir"
# get back to gradle project directory
popd

# shutdown the venv
echo "Tearing down pyatlas venv..."
deactivate
rm -rf "$venv_path"
#################################################################
