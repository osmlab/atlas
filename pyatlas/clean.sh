#!/usr/bin/env bash

### define utility functions ###
################################
err_shutdown() {
    echo "clean.sh: ERROR: $1"
    deactivate
    exit 1
}
#################################################################


### Check to prevent users from running this script directly ###
################################################################
if [ "$1" != "ranFromGradle" ];
then
    err_shutdown "this script should be run using the atlas gradle task 'cleanPyatlas'"
fi
#################################################################


### set up variables to store directory names ###
#################################################
pyatlas_dir="pyatlas"
pyatlas_srcdir="pyatlas"
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


### clean up the build artifacts ###
####################################
rm -rf "$pyatlas_root_dir/build"
rm -rf "$pyatlas_root_dir/dist"
rm -rf "$pyatlas_root_dir/pyatlas.egg-info"
# use 'find' to handle case where filenames contain spaces
find "$pyatlas_root_dir/$pyatlas_srcdir" -type f -name "*_pb2.py" -delete
find "$pyatlas_root_dir/$pyatlas_srcdir" -type f -name "*.pyc" -delete
#################################################################
