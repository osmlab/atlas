#!/usr/bin/env bash

### Check to prevent users from running this script directly ###
################################################################
if [ "$1" != "ranFromGradle" ];
then
    echo "This script should be run using the atlas gradle task 'cleanPyatlas'"
    exit 1
fi
#################################################################

### set up variables to store directory names ###
#################################################
pyatlas_dir="pyatlas"
gradle_project_root_dir="$(pwd)"
pyatlas_root_dir="$gradle_project_root_dir/$pyatlas_dir"
#################################################################

### clean up the build artifacts ###
####################################
rm -rf "$pyatlas_root_dir/build"
rm -rf "$pyatlas_root_dir/dist"
rm -rf "$pyatlas_root_dir/pyatlas.egg-info"
rm -f "$pyatlas_root_dir/pyatlas/"*_pb2.py
rm -f "$pyatlas_root_dir/pyatlas/"*.pyc
#################################################################
