#!/bin/sh

CURRENT_BRANCH=$(echo "$GITHUB_REF" | awk 'BEGIN { FS = "/" } ; { print $3 }')
echo "Current Branch: $CURRENT_BRANCH"

if [ "$CURRENT_BRANCH" = "main" ] && [ "$MANUAL_RELEASE_TRIGGERED" = "true" ];
then
	echo "This is a manual release, change the version locally to remove the -SNAPSHOT"
	sed -i "s/-SNAPSHOT//g" gradle.properties
else
	echo "Not a manual release, keeping -SNAPSHOT"
fi
