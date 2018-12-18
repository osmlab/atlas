export DROP_PAYLOAD='{'\
'   "data":{'\
'      "stagedRepositoryIds":['\
'         "'$STAGING_ID'"'\
'      ],'\
'      "description":"Dropping Atlas repo"'\
'   }'\
'}'

curl --fail -u $SONATYPE_USERNAME:$SONATYPE_PASSWORD \
    -X POST \
    -H "Content-Type:application/json" \
    -d "$DROP_PAYLOAD" \
    "$API_ENDPOINT/staging/bulk/drop"
