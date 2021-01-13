#!/bin/sh

CURRENT_BRANCH=$(echo "$GITHUB_REF" | awk 'BEGIN { FS = "/" } ; { print $3 }')
echo "Criterion for Publishing artifacts to Maven Central:"
echo "Current Branch (Should be main): $CURRENT_BRANCH"
echo "Pull Request (Should be empty): $GITHUB_HEAD_REF"
echo "Manual Release Triggered (Should be true): $MANUAL_RELEASE_TRIGGERED"

if [ "$CURRENT_BRANCH" = "main" ] && [ -z "$GITHUB_HEAD_REF" ];
then
    echo "On branch main, not in a Pull Request"
    if [ "$MANUAL_RELEASE_TRIGGERED" = "true" ];
    then
        echo "Sign, Upload archives to local repo, Upload archives to Sonatype, Close and release repository."
        ./gradlew uploadArchives publishToNexusAndClose
        #python -m pip install --user --upgrade twine
        #twine upload ./pyatlas/dist/*
    else
        echo "Not publishing artifacts, since MANUAL_RELEASE_TRIGGERED=$MANUAL_RELEASE_TRIGGERED"
    fi
else
    echo "Not publishing artifacts, since not on branch main, or in a Pull Request"
fi
