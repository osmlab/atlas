#!/usr/bin/env sh

if [ "$TRAVIS_PULL_REQUEST" != "false" ];
then
	SONAR_PULLREQUEST_BRANCH="$(echo $TRAVIS_PULL_REQUEST_SLUG | awk '{split($0,a,"/"); print a[1]}')/$TRAVIS_PULL_REQUEST_BRANCH"
	echo "Running sonarqube in Pull Request $TRAVIS_PULL_REQUEST"
	echo "sonar.pullrequest.key=$TRAVIS_PULL_REQUEST"
	echo "sonar.pullrequest.branch=$SONAR_PULLREQUEST_BRANCH"
	echo "sonar.pullrequest.base=$TRAVIS_BRANCH"
	./gradlew sonarqube \
		-Dsonar.organization=osmlab \
		-Dsonar.host.url=https://sonarcloud.io \
		-Dsonar.login=$SONAR_TOKEN \
		-Dsonar.junit.reportPaths=build/test-results/test \
		-Dsonar.jacoco.reportPaths=build/jacoco/test.exec \
		-Dsonar.pullrequest.key=$TRAVIS_PULL_REQUEST \
		-Dsonar.pullrequest.branch=$SONAR_PULLREQUEST_BRANCH \
		-Dsonar.pullrequest.base=$TRAVIS_BRANCH \
		-Dsonar.pullrequest.provider=github \
		-Dsonar.pullrequest.github.repository=osmlab/atlas \
		-Dsonar.pullrequest.github.endpoint=https://api.github.com/ \
		-Dsonar.pullrequest.github.token.secured=$SONAR_PR_DECORATION_GITHUB_TOKEN
else
	echo "Running sonarqube in a regular build"
	./gradlew sonarqube \
		-Dsonar.branch.name=$TRAVIS_BRANCH \
		-Dsonar.organization=osmlab \
		-Dsonar.host.url=https://sonarcloud.io \
		-Dsonar.login=$SONAR_TOKEN \
		-Dsonar.junit.reportPaths=build/test-results/test \
		-Dsonar.jacoco.reportPaths=build/jacoco/test.exec
fi
