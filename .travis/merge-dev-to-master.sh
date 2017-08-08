#!/usr/bin/env sh

GITHUB_REPO="matthieun/wkb-wkt-converter"
MERGE_BRANCH=master
SOURCE_BRANCH=dev

FUNCTION_NAME="merge-$SOURCE_BRANCH-to-$MERGE_BRANCH"

echo "$FUNCTION_NAME: $GITHUB_REPO"
echo "$FUNCTION_NAME: TRAVIS_BRANCH = $TRAVIS_BRANCH"
echo "$FUNCTION_NAME: TRAVIS_PULL_REQUEST = $TRAVIS_PULL_REQUEST"

if [ "$TRAVIS_BRANCH" != "$SOURCE_BRANCH" ];
then
	echo "$FUNCTION_NAME: Exiting! Branch is not $SOURCE_BRANCH: ($TRAVIS_BRANCH)"
    exit 0;
fi

if [ "$TRAVIS_PULL_REQUEST" != "false" ];
then
	echo "$FUNCTION_NAME: Exiting! This is a Pull Request: $TRAVIS_PULL_REQUEST"
    exit 0;
fi

: ${GITHUB_SECRET_TOKEN:?"GITHUB_SECRET_TOKEN needs to be set in .travis.yml!"}

export GIT_COMMITTER_EMAIL="travis@travis.org"
export GIT_COMMITTER_NAME="Travis CI"

TEMPORARY_REPOSITORY=$(mktemp -d)
git clone "https://github.com/$GITHUB_REPO" "$TEMPORARY_REPOSITORY"
cd $TEMPORARY_REPOSITORY

echo "Checking out $SOURCE_BRANCH"
git checkout $SOURCE_BRANCH

echo "Checking out $MERGE_BRANCH"
git checkout $MERGE_BRANCH

echo "Merging $SOURCE_BRANCH into $MERGE_BRANCH"
git merge --ff-only "$SOURCE_BRANCH"

echo "Pushing to $GITHUB_REPO"
# Redirect to /dev/null to avoid secret leakage
git push "https://$GITHUB_SECRET_TOKEN@github.com/$GITHUB_REPO" $MERGE_BRANCH > /dev/null 2>&1
