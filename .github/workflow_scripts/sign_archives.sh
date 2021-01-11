#!/bin/sh

GPG_KEY_LOCATION="$HOME/secrets/secring.gpg"

echo "GPG Key location: $GPG_KEY_LOCATION"
echo "GPG Key id: $GPG_KEY_ID"
echo "GPG passphrase: $GPG_PASSPHRASE"

echo "Key size:"
wc -c $GPG_KEY_LOCATION
echo "GPG Version:"
gpg --version

./gradlew -s sign -x check
