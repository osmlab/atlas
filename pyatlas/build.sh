#!/usr/bin/env bash

# Simple check to prevent users from running this script directly
if [ "$1" != "ranFromGradle" ];
then
    echo "This script should be run using the atlas gradle task 'buildPyatlas'"
    exit 1
fi

if command -v virtualenv > /dev/null 2> /dev/null; then
    virtualenv_command="$(command -v virtualenv)"
else
    echo "ERROR: 'command -v virtualenv' returned non-zero exit status"
    exit 1
fi

pyatlas_dir="pyatlas"
gradle_project_root_dir="$(pwd)"
pyatlas_root_dir="$gradle_project_root_dir/$pyatlas_dir"

echo "Setting up pyatlas venv..."
venv_path="$pyatlas_root_dir/__pyatlasvenv__"
if ! $virtualenv_command --python=python2.7 "$venv_path";
then
    echo "ERROR: virtualenv command returned non-zero exit status"
    exit 1
fi
source "$venv_path/bin/activate"

echo "Preparing protoc..."

echo "Building and packaging pyatlas module..."
python "$pyatlas_root_dir/setup.py" sdist -d "$pyatlas_root_dir/dist" bdist_wheel -d "$pyatlas_root_dir/dist" bdist_egg -d "$pyatlas_root_dir/dist" > /dev/null 2> /dev/null

echo "Tearing down pyatlas venv..."
deactivate
rm -rf "$venv_path"
