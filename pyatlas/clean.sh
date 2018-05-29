#!/usr/bin/env bash

# Simple check to prevent users from running this script directly
if [ "$1" != "ranFromGradle" ];
then
    echo "This script should be run using the atlas gradle task 'buildPyatlas'"
    exit 1
fi

pyatlas_dir="pyatlas"
gradle_project_root_dir="$(pwd)"
pyatlas_root_dir="$gradle_project_root_dir/$pyatlas_dir"

rm -rf "$pyatlas_root_dir/build"
rm -rf "$pyatlas_root_dir/dist"
rm -rf "$pyatlas_root_dir/pyatlas.egg-info"

