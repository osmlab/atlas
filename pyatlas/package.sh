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


### check to prevent users from running this script directly ###
################################################################
if [ "$1" != "ranFromGradle" ];
then
    err_shutdown "this script should be run using the atlas gradle task 'packagePyatlas'"
fi


### set up variables to store directory names ###
#################################################
gradle_project_root_dir="$(pwd)"
pyatlas_dir="pyatlas"
pyatlas_srcdir="pyatlas"
doc_dir="doc"
pyatlas_root_dir="$gradle_project_root_dir/$pyatlas_dir"
venv_path="$pyatlas_root_dir/__pyatlas_venv__"
protofiles_dir="$gradle_project_root_dir/src/main/proto"


### abort the script if the pyatlas source folder is not present ###
####################################################################
if [ ! -d "$pyatlas_root_dir/$pyatlas_srcdir" ];
then
    err_shutdown "pyatlas source folder not found"
fi


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
if [ ! -f "$protoc_path" ];
then
    if [ "$(uname)" == "Darwin" ];
    then
        download_link="https://repo1.maven.org/maven2/com/google/protobuf/protoc/${protoc_version}/protoc-${protoc_version}-osx-x86_64.exe"
        "$wget_command" "$download_link" -O "$protoc_path" || err_shutdown "wget of '$download_link' failed"
    elif [ "$(uname)" == "Linux" ];
    then
        download_link="https://repo1.maven.org/maven2/com/google/protobuf/protoc/${protoc_version}/protoc-${protoc_version}-linux-x86_64.exe"
        "$wget_command" "$download_link" -O "$protoc_path" || err_shutdown "wget of '$download_link' failed"
    else
        err_shutdown "unrecognized platform $(uname)"
fi
fi
chmod 700 "$protoc_path"

# complicated mess to handle case where a proto filename has a space
# basically, 'find' outputs each file separated by a NUL terminator
# read -r -d '' reads raw input delimited by NUL characters
while IFS= read -r -d '' protofile
do
    "$protoc_path" "$protofile" --proto_path="$protofiles_dir" --python_out="$pyatlas_root_dir/$pyatlas_srcdir/autogen" || err_shutdown "protoc invocation failed"
done < <(find "$protofiles_dir" -type f -name "*.proto" -print0)


### build the module and documentation ###
##########################################
# start the venv
if [ ! -d "$venv_path" ];
then
    err_shutdown "missing $venv_path"
fi
# shellcheck source=/dev/null
source "$venv_path/bin/activate"

# copy the LICENSE to the pyatlas folder
cp "$gradle_project_root_dir/LICENSE" "$pyatlas_root_dir"

# grab the build version from gradle.properties and inject it into setup.py
# remove the -SNAPSHOT text if present
atlas_version=$(grep "version=" "$gradle_project_root_dir/gradle.properties" | cut -f2 -d "=" | sed 's/-SNAPSHOT//g')
# GNU and BSD sed have different "in-place" flag syntax
if [ "$(uname)" == "Darwin" ];
then
    sed -i "" "s/version=.*/version=\"$atlas_version\",/" "$pyatlas_root_dir/setup.py"
elif [ "$(uname)" == "Linux" ];
then
    sed --in-place="" "s/version=.*/version=\"$atlas_version\",/" "$pyatlas_root_dir/setup.py"
else
    err_shutdown "unrecognized platform $(uname)"
fi

# enter the pyatlas project directory so module metadata is generated correctly
pushd "$pyatlas_root_dir"

echo "Building and packaging pyatlas module..."
python "setup.py" sdist -d "dist" bdist_wheel -d "dist"

# self-install and create the docs
pip install -e .
# hack to make pydoc work
export PYTHONPATH="$PYTHONPATH:$pyatlas_root_dir/$pyatlas_srcdir:$pyatlas_root_dir/$pyatlas_srcdir/autogen"
# FIXME this will fail if source file has a space
pydoc -w "$pyatlas_srcdir"/*.py
mv ./*.html "$doc_dir"
# this would be the correct way, but for some reason the 'find exec' fails on atlas.py
#find "$pyatlas_srcdir"/*.py -exec pydoc -w {} \;
#find "$pyatlas_root_dir/"*.html -exec mv {} "$doc_dir" \;

# get back to gradle project directory
popd

# reset version field in setup.py
# GNU and BSD sed have different "in-place" flag syntax
if [ "$(uname)" == "Darwin" ];
then
    sed -i "" "s/version=.*/version=/" "$pyatlas_root_dir/setup.py"
elif [ "$(uname)" == "Linux" ];
then
    sed --in-place="" "s/version=.*/version=/" "$pyatlas_root_dir/setup.py"
else
    err_shutdown "unrecognized platform $(uname)"
fi

# shutdown the venv
deactivate
