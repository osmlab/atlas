#!/usr/bin/env sh

if [ "$TRAVIS_BRANCH" = "master" ] && [ "$TRAVIS_PULL_REQUEST" = "false" ];
then
	echo "Sign Archives"
	./gradlew signArchives
	echo "Upload Archives"
	./gradlew uploadArchives
	if [ "$MANUAL_RELEASE_TRIGGERED" = "true" ];
	then
		echo "Promote repository"
		./gradlew closeAndReleaseRepository
	fi
fi
