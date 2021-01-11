#!/bin/sh

GPG_KEY_LOCATION="$HOME/secrets/secret.gpg"

echo "GPG Key location: $GPG_KEY_LOCATION"
echo "GPG Key id: $GPG_KEY_ID"
echo "GPG passphrase: $GPG_PASSPHRASE"

wc -c $GPG_KEY_LOCATION

./gradlew -d sign -x check
