#!/usr/bin/env sh

# Use Travis to trigger a release from Master

GITHUB_ORGANIZATION=osmlab
GITHUB_REPOSITORY_NAME=atlas

# Assumptions
# - This is called from the root of the project
# - The travis client is installed: gem install travis
# - travis login --org has been called to authenticate

TRAVIS_PERSONAL_TOKEN=$(travis token)

: ${TRAVIS_PERSONAL_TOKEN:?"TRAVIS_PERSONAL_TOKEN needs to be set to access the Travis API to trigger the build"}

body='
{
	"request":
	{
		"branch": "master",
		"config":
		{
			"before_script": "export MANUAL_RELEASE_TRIGGERED=true"
		}
	}
}'

curl -s -X POST \
	-H "Content-Type: application/json" \
	-H "Accept: application/json" \
	-H "Travis-API-Version: 3" \
	-H "Authorization: token $TRAVIS_PERSONAL_TOKEN" \
	-d "$body" \
	https://api.travis-ci.org/repo/$GITHUB_ORGANIZATION%2F$GITHUB_REPOSITORY_NAME/requests
