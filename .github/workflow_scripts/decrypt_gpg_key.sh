#!/bin/sh

GPG_HOME="$HOME/.gnupg"
GPG_KEY_LOCATION="$HOME/.gnupg/secring.gpg"

# Decrypt the file
mkdir $GPG_HOME
chmod -R 700 $GPG_HOME

# --batch to prevent interactive command
# --yes to assume "yes" for questions
gpg --quiet --batch --yes --decrypt --passphrase="$GPG_AES256_PASSPHRASE" \
--output $GPG_KEY_LOCATION .github/workflow_data/secret.gpg.aes256
