#!/usr/bin/env sh

if [ "$TRAVIS_BRANCH" = "main" ] && [ "$TRAVIS_PULL_REQUEST" = "false" ];
then
	if [ "$MANUAL_RELEASE_TRIGGERED" = "true" ];
	then
		echo "Sign, Upload archives to local repo, Upload archives to Sonatype, Close and release repository."
		./gradlew uploadArchives publishToNexusAndClose
		#python -m pip install --user --upgrade twine
		#twine upload ./pyatlas/dist/*
	fi
fi
