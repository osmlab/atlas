#!/usr/bin/env sh

GITHUB_REPO="osmlab/atlas"
MERGE_BRANCH=main
SOURCE_BRANCH=dev

FUNCTION_NAME="merge-$SOURCE_BRANCH-to-$MERGE_BRANCH"
CURRENT_BRANCH=$(echo $GITHUB_REF | awk 'BEGIN { FS = "/" } ; { print $3 }')

echo "$FUNCTION_NAME: $GITHUB_REPO"
echo "$FUNCTION_NAME: CURRENT_BRANCH = $CURRENT_BRANCH"
echo "$FUNCTION_NAME: GITHUB_HEAD_REF = $GITHUB_HEAD_REF"

if [ "$CURRENT_BRANCH" != "$SOURCE_BRANCH" ];
then
	echo "$FUNCTION_NAME: Exiting! Branch is not $SOURCE_BRANCH: ($CURRENT_BRANCH)"
    exit 0;
fi

if [ -n "$GITHUB_HEAD_REF" ];
then
	PULL_REQUEST_NUMBER=$(echo $GITHUB_REF | awk 'BEGIN { FS = "/" } ; { print $3 }')
	echo "$FUNCTION_NAME: Exiting! This is a Pull Request: $PULL_REQUEST_NUMBER"
    exit 0;
fi

: ${MERGE_TAG_GITHUB_SECRET_TOKEN:?"MERGE_TAG_GITHUB_SECRET_TOKEN needs to be set in the workflow yml file!"}
: ${GITHUB_SHA:?"GITHUB_SHA needs to be available to tag the right commit!"}

TEMPORARY_REPOSITORY=$(mktemp -d)
git clone "https://github.com/$GITHUB_REPO" "$TEMPORARY_REPOSITORY"
cd $TEMPORARY_REPOSITORY

echo "Checking out $SOURCE_BRANCH"
git checkout $SOURCE_BRANCH
git checkout -b tomerge $GITHUB_SHA

echo "Checking out $MERGE_BRANCH"
git checkout $MERGE_BRANCH

echo "Merging temporary branch tomerge ($GITHUB_SHA) from $SOURCE_BRANCH into $MERGE_BRANCH"
git merge --ff-only "tomerge"

echo "Pushing to $GITHUB_REPO"
# Redirect to /dev/null to avoid secret leakage
git push "https://$MERGE_TAG_GITHUB_SECRET_TOKEN@github.com/$GITHUB_REPO" $MERGE_BRANCH > /dev/null 2>&1
