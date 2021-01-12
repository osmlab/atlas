#!/usr/bin/env sh

GITHUB_REPO="osmlab/atlas"
RELEASE_BRANCH=main

FUNCTION_NAME="tag-$RELEASE_BRANCH"
CURRENT_BRANCH=$(echo $GITHUB_REF | awk 'BEGIN { FS = "/" } ; { print $3 }')

echo "$FUNCTION_NAME: $GITHUB_REPO"
echo "$FUNCTION_NAME: CURRENT_BRANCH = $CURRENT_BRANCH"
echo "$FUNCTION_NAME: GITHUB_HEAD_REF = $GITHUB_HEAD_REF"

if [ "$CURRENT_BRANCH" != "$RELEASE_BRANCH" ];
then
	echo "$FUNCTION_NAME: Exiting! Branch is not $RELEASE_BRANCH: ($CURRENT_BRANCH)"
    exit 0;
fi

if [ -n "$GITHUB_HEAD_REF" ];
then
	PULL_REQUEST_NUMBER=$(echo $GITHUB_REF | awk 'BEGIN { FS = "/" } ; { print $3 }')
	echo "$FUNCTION_NAME: Exiting! This is a Pull Request: $PULL_REQUEST_NUMBER"
    exit 0;
fi

if [ "$MANUAL_RELEASE_TRIGGERED" != "true" ];
then
	echo "$FUNCTION_NAME: Exiting! This is not a release build."
    exit 0;
fi

: ${MERGE_TAG_MERGE_TAG_GITHUB_SECRET_TOKEN:?"MERGE_TAG_MERGE_TAG_GITHUB_SECRET_TOKEN needs to be set in the workflow yml file!"}
: ${GITHUB_SHA:?"GITHUB_SHA needs to be available to tag the right commit!"}

export GIT_COMMITTER_EMAIL="github-actions@github.com"
export GIT_COMMITTER_NAME="Github Actions CI"

TEMPORARY_REPOSITORY=$(mktemp -d)
git clone "https://github.com/$GITHUB_REPO" "$TEMPORARY_REPOSITORY"
cd $TEMPORARY_REPOSITORY

echo "Checking out $RELEASE_BRANCH (commit $GITHUB_SHA)"
git checkout $GITHUB_SHA

PROJECT_VERSION=$(cat gradle.properties | grep "\-SNAPSHOT" | awk -F '=' '{print $2}' | awk -F '-' '{print $1}')
: ${PROJECT_VERSION:?"PROJECT_VERSION could not be found."}

echo "Tagging $RELEASE_BRANCH (commit $GITHUB_SHA) at version $PROJECT_VERSION"
git tag -a $PROJECT_VERSION -m "Release $PROJECT_VERSION"

echo "Pushing tag $PROJECT_VERSION to $GITHUB_REPO"
# Redirect to /dev/null to avoid secret leakage
git push "https://$MERGE_TAG_GITHUB_SECRET_TOKEN@github.com/$GITHUB_REPO" $PROJECT_VERSION > /dev/null 2>&1
