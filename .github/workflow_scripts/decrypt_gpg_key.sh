#!/bin/sh

# Decrypt the file

# --batch to prevent interactive command
# --yes to assume "yes" for questions
gpg --quiet --batch --yes --decrypt --passphrase="$GPG_AES256_PASSPHRASE" \
--output $GPG_KEY_LOCATION .github/workflow_data/secret.gpg.aes256

chmod 700 $GPG_KEY_LOCATION
