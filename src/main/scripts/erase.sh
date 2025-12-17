#!/bin/bash
YA_ENDPOINT=https://rest-assistant.api.cloud.yandex.net/v1/files
for i in $(curl $YA_ENDPOINT \
  -H "Authorization: Bearer $YA_API_KEY" \
  -H "OpenAI-Project: $FOLDER_ID" | jq '.data[].id' --raw-output); do
  echo "deleting $i";
  curl $YA_ENDPOINT/$i \
  -X DELETE \
  -H "Authorization: Bearer $YA_API_KEY" \
  -H "OpenAI-Project: $FOLDER_ID"
done
