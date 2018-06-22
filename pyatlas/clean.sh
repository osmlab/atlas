#!/usr/bin/env bash

# general case script abort if a command fails
# this can be overridden with a custom error message using '|| err_shutdown'
set -e
set -o pipefail

### define utility functions ###
################################
err_shutdown() {
    echo "clean.sh: ERROR: $1"
    deactivate
    exit 1
}


### check to prevent users from running this script directly ###
################################################################
if [ "$1" != "ranFromGradle" ];
then
    err_shutdown "this script should be run using the atlas gradle task 'cleanPyatlas'"
fi


### set up variables to store directory names ###
#################################################
gradle_project_root_dir="$(pwd)"
pyatlas_dir="pyatlas"
pyatlas_srcdir="pyatlas"
pyatlas_testdir="unit_tests"
pyatlas_docdir="doc"
pyatlas_root_dir="$gradle_project_root_dir/$pyatlas_dir"
venv_path="$pyatlas_root_dir/__pyatlas_venv__"
protoc_path="$pyatlas_root_dir/protoc"


### abort the script if the pyatlas source folder is not present ###
####################################################################
if [ ! -d "$pyatlas_root_dir/$pyatlas_srcdir" ];
then
    err_shutdown "pyatlas source folder not found"
fi


### clean up the build artifacts ###
####################################
echo "Cleaning build artifacts if present..."
rm -rf "$pyatlas_root_dir/build"
rm -rf "$pyatlas_root_dir/dist"
rm -rf "$pyatlas_root_dir/pyatlas.egg-info"
rm -f "$pyatlas_root_dir/LICENSE"
rm -rf "$venv_path"
rm -f "$protoc_path"
# use 'find' to handle case where filenames contain spaces
find "$pyatlas_root_dir/$pyatlas_srcdir/autogen" -type f -name "*_pb2.py" -delete
find "$pyatlas_root_dir/$pyatlas_srcdir" -type f -name "*.pyc" -delete
find "$pyatlas_root_dir/$pyatlas_srcdir/autogen" -type f -name "*.pyc" -delete
find "$pyatlas_root_dir/$pyatlas_testdir" -type f -name "*.pyc" -delete
find "$pyatlas_root_dir/$pyatlas_docdir" -type f -name "*.html" -delete
