#!/usr/bin/env sh

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
