#!/usr/bin/env sh

if [ "$TRAVIS_BRANCH" = "main" ] && [ "$TRAVIS_PULL_REQUEST" = "false" ];
then
    openssl aes-256-cbc -K $encrypted_7b323cc104d6_key -iv $encrypted_7b323cc104d6_iv -in $ENCRYPTED_GPG_KEY_LOCATION -out $GPG_KEY_LOCATION -d
fi
