#!/bin/sh

GPG_KEY_LOCATION="secring.gpg"

echo "GPG Key location: $GPG_KEY_LOCATION"
echo "GPG Key id: $GPG_KEY_ID"
echo "GPG passphrase: $GPG_PASSPHRASE"

echo ""
echo "Key size:"
wc -c $GPG_KEY_LOCATION
echo ""
echo "GPG Version:"
gpg --version
echo ""
echo "Export GPG TTY."
export GPG_TTY=$(tty)
echo ""
echo ""
echo ""

./gradlew -s \
    properties sign
    # -Psigning.keyId=$GPG_KEY_ID \
    # -Psigning.secretKeyRingFile="$GPG_KEY_LOCATION" \
    # -Psigning.password=$GPG_PASSPHRASE \
