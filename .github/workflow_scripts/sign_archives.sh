#!/bin/sh

GPG_HOME="$HOME/.gnupg"
GPG_KEY_LOCATION="$GPG_HOME/secring.gpg"

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

./gradlew -i -s \
    -Dsigning.keyId=$GPG_KEY_ID \
    -Dsigning.secretKeyRingFile=$GPG_KEY_LOCATION \
    -Dsigning.password=$GPG_PASSPHRASE \
    sign
