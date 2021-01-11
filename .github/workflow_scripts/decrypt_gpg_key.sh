#!/bin/sh

GPG_KEY_LOCATION="$HOME/secrets/secring.gpg"
# Decrypt the file
mkdir $HOME/secrets
# --batch to prevent interactive command
# --yes to assume "yes" for questions
gpg --quiet --batch --yes --decrypt --passphrase="$GPG_AES256_PASSPHRASE" \
--output $GPG_KEY_LOCATION .github/workflow_data/secret.gpg.aes256
