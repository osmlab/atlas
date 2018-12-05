#!/usr/bin/env sh

if [ "$TRAVIS_EVENT_TYPE" = "cron" ];
then
	echo "Running sonarqube in a CRON build"
	./gradlew sonarqube \
		-Dsonar.branch.name=$TRAVIS_BRANCH \
		-Dsonar.organization=osmlab \
		-Dsonar.host.url=https://sonarcloud.io \
		-Dsonar.login=$SONAR_TOKEN \
		-Dsonar.junit.reportPaths=build/test-results/test \
		-Dsonar.jacoco.reportPaths=build/jacoco/test.exec
fi

if [ "$TRAVIS_PULL_REQUEST" != "false" ];
then
	echo "Running sonarqube in Pull Request $TRAVIS_PULL_REQUEST"
	./gradlew sonarqube \
		-Dsonar.organization=osmlab \
		-Dsonar.host.url=https://sonarcloud.io \
		-Dsonar.login=$SONAR_TOKEN \
		-Dsonar.junit.reportPaths=build/test-results/test \
		-Dsonar.jacoco.reportPaths=build/jacoco/test.exec \
		-Dsonar.pullrequest.key=$TRAVIS_PULL_REQUEST \
		-Dsonar.pullrequest.branch=$TRAVIS_PULL_REQUEST_SLUG \
		-Dsonar.pullrequest.base=$TRAVIS_BRANCH \
		-Dsonar.pullrequest.provider=github \
		-Dsonar.pullrequest.github.repository=osmlab/atlas \
		-Dsonar.pullrequest.github.endpoint=https://api.github.com/ \
		-Dsonar.pullrequest.github.token.secured=$SONAR_PR_DECORATION_GITHUB_TOKEN
fi
