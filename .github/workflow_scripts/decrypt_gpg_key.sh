#!/bin/sh

# Decrypt the file

if [ "$MANUAL_RELEASE_TRIGGERED" = "true" ];
then
    # --batch to prevent interactive command
    # --yes to assume "yes" for questions
    gpg --quiet --batch --yes --decrypt --passphrase="$GPG_AES256_PASSPHRASE" \
    --output "$GPG_KEY_LOCATION" .github/workflow_data/secret.gpg.aes256

    chmod 700 "$GPG_KEY_LOCATION"
else
    echo "Not decrypting key, since MANUAL_RELEASE_TRIGGERED=$MANUAL_RELEASE_TRIGGERED"
fi
