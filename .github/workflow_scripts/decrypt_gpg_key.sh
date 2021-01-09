#!/bin/sh

export GPG_KEY_LOCATION="$HOME/secrets/secret.gpg"
# Decrypt the file
mkdir $HOME/secrets
# --batch to prevent interactive command
# --yes to assume "yes" for questions
gpg --quiet --batch --yes --decrypt --passphrase="$GPG_AES256_PASSPHRASE" \
--output $GPG_KEY_LOCATION .github/workflow_scripts/secret.gpg.aes256
