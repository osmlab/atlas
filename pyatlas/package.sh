#!/usr/bin/env bash

# THIS ENTIRE SCRIPT IS A MASSIVE HACK
# THIS SHOULD REALLY BE DONE WITH GRADLE

# general case script abort if a command fails
# this can be overridden with a custom error message using '|| err_shutdown'
set -e
set -o pipefail

### define utility functions ###
################################
err_shutdown() {
    echo "package.sh: ERROR: $1"
    deactivate
    exit 1
}
#################################################################


### check to prevent users from running this script directly ###
################################################################
if [ "$1" != "ranFromGradle" ];
then
    err_shutdown "this script should be run using the atlas gradle task 'packagePyatlas'"
fi
#################################################################


### set up variables to store directory names ###
#################################################
pyatlas_dir="pyatlas"
pyatlas_srcdir="pyatlas"
gradle_project_root_dir="$(pwd)"
pyatlas_root_dir="$gradle_project_root_dir/$pyatlas_dir"
protofiles_dir="$gradle_project_root_dir/src/main/proto"
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


### determine if wget is installed ###
######################################
if command -v wget ;
then
    wget_command="$(command -v wget)"
else
    err_shutdown "'command -v wget' returned non-zero exit status: install wget to run this script"
fi


### download protoc and compile the atlas proto files into python ###
#####################################################################
echo "Preparing protoc..."
# hack to grab the protoc version from dependencies.gradle
protoc_version=$(grep 'protoc' "$gradle_project_root_dir/dependencies.gradle" | awk -F':' '{print $2; exit}' | tr -d "'")
protoc_path="$pyatlas_root_dir/protoc"
# detemine what platform we are on
if [ "$(uname)" == "Darwin" ];
then
    download_link="https://repo1.maven.org/maven2/com/google/protobuf/protoc/${protoc_version}/protoc-${protoc_version}-osx-x86_64.exe"
    $wget_command "$download_link" -O "$protoc_path" || err_shutdown "wget of '$download_link' failed"
elif [ "$(uname)" == "Linux" ];
then
    download_link="https://repo1.maven.org/maven2/com/google/protobuf/protoc/${protoc_version}/protoc-${protoc_version}-linux-x86_64.exe"
    $wget_command "$download_link" -O "$protoc_path" || err_shutdown "wget of '$download_link' failed"
else
    err_shutdown "unrecognized platform $(uname)"
fi
chmod 700 $protoc_path

# complicated mess to handle case where a proto filename has a space
# basically, 'find' outputs each file separated by a NUL terminator
# read -r -d '' reads raw input delimited by NUL characters
while IFS= read -r -d '' protofile
do
    $protoc_path "$protofile" --proto_path="$protofiles_dir" --python_out="$pyatlas_root_dir/$pyatlas_srcdir/autogen" || err_shutdown "protoc invocation failed"
done < <(find "$protofiles_dir" -type f -name "*.proto" -print0)

rm -f "$protoc_path"
#################################################################


### build the module ###
########################
# start the venv
echo "Setting up pyatlas venv..."
venv_path="$pyatlas_root_dir/__pyatlas_build_venv__"
if ! $virtualenv_command --python=python2.7 "$venv_path";
then
    err_shutdown "virtualenv command returned non-zero exit status"
fi
# shellcheck source=/dev/null
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
