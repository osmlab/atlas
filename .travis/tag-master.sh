#!/usr/bin/env sh

GITHUB_REPO="matthieun/wkb-wkt-converter"
RELEASE_BRANCH=master

FUNCTION_NAME="tag-$RELEASE_BRANCH"

echo "$FUNCTION_NAME: $GITHUB_REPO"
echo "$FUNCTION_NAME: TRAVIS_BRANCH = $TRAVIS_BRANCH"
echo "$FUNCTION_NAME: TRAVIS_PULL_REQUEST = $TRAVIS_PULL_REQUEST"

if [ "$TRAVIS_BRANCH" != "$RELEASE_BRANCH" ];
then
	echo "$FUNCTION_NAME: Exiting! Branch is not $RELEASE_BRANCH: ($TRAVIS_BRANCH)"
    exit 0;
fi

if [ "$TRAVIS_PULL_REQUEST" != "false" ];
then
	echo "$FUNCTION_NAME: Exiting! This is a Pull Request: $TRAVIS_PULL_REQUEST"
    exit 0;
fi

if [ "$MANUAL_RELEASE_TRIGGERED" != "true" ];
then
	echo "$FUNCTION_NAME: Exiting! This is not a release build."
    exit 0;
fi

: ${GITHUB_SECRET_TOKEN:?"GITHUB_SECRET_TOKEN needs to be set in .travis.yml!"}

export GIT_COMMITTER_EMAIL="travis@travis.org"
export GIT_COMMITTER_NAME="Travis CI"

TEMPORARY_REPOSITORY=$(mktemp -d)
git clone "https://github.com/$GITHUB_REPO" "$TEMPORARY_REPOSITORY"
cd $TEMPORARY_REPOSITORY

echo "Checking out $RELEASE_BRANCH"
git checkout $RELEASE_BRANCH

PROJECT_VERSION=$(cat gradle.properties | grep "\-SNAPSHOT" | awk -F '=' '{print $2}' | awk -F '-' '{print $1}')
: ${PROJECT_VERSION:?"PROJECT_VERSION could not be found."}

echo "Tagging $RELEASE_BRANCH at version $PROJECT_VERSION"
git tag -a $PROJECT_VERSION -m "Release $PROJECT_VERSION"

echo "Pushing tag $PROJECT_VERSION to $GITHUB_REPO"
# Redirect to /dev/null to avoid secret leakage
git push "https://$GITHUB_SECRET_TOKEN@github.com/$GITHUB_REPO" $PROJECT_VERSION > /dev/null 2>&1
