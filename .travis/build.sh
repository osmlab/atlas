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
	echo "Skip integration tests in pull request builds"
	./gradlew clean build -x integrationTest
	./gradlew cleanPyatlas buildPyatlas
else
	echo "Temporarily skip integration tests in all builds. Too heavy for Travis"
	./gradlew clean build -x integrationTest
	./gradlew cleanPyatlas buildPyatlas
fi

if [ "$TRAVIS_EVENT_TYPE" = "cron" ];
then
	echo "Running sonarqube in a CRON build"
	./gradlew sonarqube \
		-Dsonar.organization=osmlab \
		-Dsonar.host.url=https://sonarcloud.io \
		-Dsonar.login=$SONAR_TOKEN \
		-Dsonar.junit.reportPaths=build/test-results/test \
		-Dsonar.jacoco.reportPaths=build/jacoco/test.exec
fi
