#!/usr/bin/env sh

if [ $TRAVIS_TEST_RESULT -eq 0 ];
then
	./gradlew cleanPyatlas buildPyatlas
	RETURN_VALUE=$?
	if [ "$RETURN_VALUE" != "0" ];
	then
		exit $RETURN_VALUE
	fi
fi
