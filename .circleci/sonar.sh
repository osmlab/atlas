#!/usr/bin/env sh

if [ -z "$CIRCLE_PR_NUMBER" ];
then
    echo "Running sonarqube in a regular build"
	#TODO: Remove echo below
	echo ./gradlew jacocoTestReport sonarqube \
		-Dsonar.branch.name=$CIRCLE_BRANCH \
		-Dsonar.organization=osmlab \
		-Dsonar.host.url=https://sonarcloud.io \
		-Dsonar.login=$SONAR_TOKEN \
		-Dsonar.junit.reportPaths=build/test-results/test \
		-Dsonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/test/jacocoTestReport.xml
else
    CIRCLE_PR_BRANCH=`curl -s https://api.github.com/repos/$CIRCLE_PROJECT_USERNAME/$CIRCLE_PROJECT_REPONAME/pulls/$CIRCLE_PR_NUMBER | jq -r '.head.ref'`
	SONAR_PULLREQUEST_BRANCH="$CIRCLE_PR_USERNAME/$CIRCLE_PR_BRANCH"
	echo "Running sonarqube in Pull Request $CIRCLE_PR_NUMBER"
	echo "sonar.pullrequest.key=$CIRCLE_PR_NUMBER"
	echo "sonar.pullrequest.branch=$SONAR_PULLREQUEST_BRANCH"
	echo "sonar.pullrequest.base=$CIRCLE_BRANCH"
	#TODO: Remove echo below
	echo ./gradlew jacocoTestReport sonarqube \
		-Dsonar.organization=osmlab \
		-Dsonar.host.url=https://sonarcloud.io \
		-Dsonar.login=$SONAR_TOKEN \
		-Dsonar.junit.reportPaths=build/test-results/test \
		-Dsonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/test/jacocoTestReport.xml \
		-Dsonar.pullrequest.key=$CIRCLE_PR_NUMBER \
		-Dsonar.pullrequest.branch=$SONAR_PULLREQUEST_BRANCH \
		-Dsonar.pullrequest.base=$CIRCLE_BRANCH \
		-Dsonar.pullrequest.provider=github \
		-Dsonar.pullrequest.github.repository=osmlab/atlas \
		-Dsonar.pullrequest.github.endpoint=https://api.github.com/ \
		-Dsonar.pullrequest.github.token.secured=$SONAR_PR_DECORATION_GITHUB_TOKEN
fi
