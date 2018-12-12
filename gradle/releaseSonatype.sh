export RELEASE_PAYLOAD='{'\
'   "data":{'\
'      "stagedRepositoryIds":['\
'         "'$STAGING_ID'"'\
'      ],'\
'      "description":"Releasing Atlas repo"'\
'   }'\
'}'

curl --fail -s -u $SONATYPE_USERNAME:$SONATYPE_PASSWORD \
    -X POST \
    -H "Content-Type:application/json" \
    -d "$RELEASE_PAYLOAD" \
    "$API_ENDPOINT/staging/bulk/promote" \
