#!/usr/bin/env sh

chmod u+x gradlew

if [ "$MANUAL_RELEASE_TRIGGERED" = "true" ];
then
	# This is a release job, triggered manually
	# Change the version locally to remove the -SNAPSHOT
	sed -i "s/-SNAPSHOT//g" gradle.properties
	echo "This is a manual release!"
else
	echo "Not a manual release"
fi

if [ "$TRAVIS_PULL_REQUEST" != "false" ];
then
	./gradlew clean build
else
	./gradlew clean build
fi
