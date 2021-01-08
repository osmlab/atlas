#!/usr/bin/env sh

if [ -z "$GITHUB_HEAD_REF" ];
then
	PULL_REQUEST_NUMBER=$(echo $GITHUB_REF | awk 'BEGIN { FS = "/" } ; { print $3 }')
	echo "Running sonarqube in Pull Request $PULL_REQUEST_NUMBER"
	echo "sonar.pullrequest.key=$PULL_REQUEST_NUMBER"
	echo "sonar.pullrequest.branch=$GITHUB_HEAD_REF"
	echo "sonar.pullrequest.base=$GITHUB_BASE_REF"
	# ./gradlew jacocoTestReport sonarqube \
	# 	-Dsonar.organization=osmlab \
	# 	-Dsonar.host.url=https://sonarcloud.io \
	# 	-Dsonar.login=$SONAR_TOKEN \
	# 	-Dsonar.junit.reportPaths=build/test-results/test \
	# 	-Dsonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/test/jacocoTestReport.xml \
	# 	-Dsonar.pullrequest.key=$PULL_REQUEST_NUMBER \
	# 	-Dsonar.pullrequest.branch=$GITHUB_HEAD_REF \
	# 	-Dsonar.pullrequest.base=$GITHUB_BASE_REF \
	# 	-Dsonar.pullrequest.provider=github \
	# 	-Dsonar.pullrequest.github.repository=osmlab/atlas \
	# 	-Dsonar.pullrequest.github.endpoint=https://api.github.com/ \
	# 	-Dsonar.pullrequest.github.token.secured=$SONAR_PR_DECORATION_GITHUB_TOKEN
else
	CURRENT_BRANCH=$(echo $GITHUB_REF | awk 'BEGIN { FS = "/" } ; { print $3 }')
	echo "Running sonarqube in branch $CURRENT_BRANCH"
	# ./gradlew jacocoTestReport sonarqube \
	# 	-Dsonar.branch.name=$TRAVIS_BRANCH \
	# 	-Dsonar.organization=osmlab \
	# 	-Dsonar.host.url=https://sonarcloud.io \
	# 	-Dsonar.login=$SONAR_TOKEN \
	# 	-Dsonar.junit.reportPaths=build/test-results/test \
	# 	-Dsonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/test/jacocoTestReport.xml
fi
