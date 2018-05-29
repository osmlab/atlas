#!/usr/bin/env bash

# THIS ENTIRE SCRIPT IS A MASSIVE HACK
# THIS SHOULD REALLY BE DONE WITH GRADLE


### define utility functions ###
################################
err_shutdown() {
    deactivate
    exit 1
}
#################################################################


### Check to prevent users from running this script directly ###
################################################################
if [ "$1" != "ranFromGradle" ];
then
    echo "This script should be run using the atlas gradle task 'buildPyatlas'"
    err_shutdown
fi
#################################################################


### set up variables to store directory names ###
#################################################
pyatlas_dir="pyatlas"
gradle_project_root_dir="$(pwd)"
pyatlas_root_dir="$gradle_project_root_dir/$pyatlas_dir"
protofiles_dir="$gradle_project_root_dir/src/main/proto"
#################################################################


### determine if virtualenv is installed ###
############################################
if command -v virtualenv;
then
    virtualenv_command="$(command -v virtualenv)"
else
    echo "ERROR: 'command -v virtualenv' returned non-zero exit status"
    err_shutdown
fi
#################################################################


### determine if wget is installed ###
######################################
if command -v wget ;
then
    wget_command="$(command -v wget)"
else
    echo "ERROR: 'command -v wget' returned non-zero exit status: install wget to run this script"
    err_shutdown
fi


### download protoc and compile the atlas proto files into python ###
#####################################################################
echo "Preparing protoc..."
# hack to grab the protoc version from dependencies.gradle
protoc_version=$(grep 'protoc' "$gradle_project_root_dir/dependencies.gradle" | awk -F':' '{print $2; exit}' | tr -d "'")
protoc_path="/tmp/protoc"
# detemine what platform we are on
if [ "$(uname)" == "Darwin" ];
then
    $wget_command "https://repo1.maven.org/maven2/com/google/protobuf/protoc/${protoc_version}/protoc-${protoc_version}-osx-x86_32.exe" -O $protoc_path
elif [ "$(uname)" == "Linux" ];
then
    $wget_command "https://repo1.maven.org/maven2/com/google/protobuf/protoc/${protoc_version}/protoc-${protoc_version}-linux-x86_32.exe" -O $protoc_path
else
    echo "ERROR: unrecognized platform $(uname)"
    err_shutdown
fi
chmod 700 $protoc_path

# FIXME this line will break horribly if any proto files have a space in their names
$protoc_path "$protofiles_dir/"*.proto --proto_path="$protofiles_dir" --python_out="$pyatlas_root_dir/pyatlas"

rm -f $protoc_path
#################################################################


### Build the module ###
########################
# start the venv
echo "Setting up pyatlas venv..."
venv_path="$pyatlas_root_dir/__pyatlasvenv__"
if ! $virtualenv_command --python=python2.7 "$venv_path";
then
    echo "ERROR: virtualenv command returned non-zero exit status"
    err_shutdown
fi
source "$venv_path/bin/activate"

# enter the pyatlas project directory so module metadata is generated correctly
pushd "$pyatlas_root_dir"
echo "Building and packaging pyatlas module..."
python "$pyatlas_root_dir/setup.py" sdist -d "$pyatlas_root_dir/dist" bdist_wheel -d "$pyatlas_root_dir/dist"
# get back to gradle project directory
popd

# shutdown the venv
echo "Tearing down pyatlas venv..."
deactivate
rm -rf "$venv_path"
#################################################################
