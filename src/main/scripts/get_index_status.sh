#!/bin/sh
YA_ENDPOINT=https://rest-assistant.api.cloud.yandex.net/v1 && \
res=$(curl -s $YA_ENDPOINT/vector_stores/$1/files?filter=in_progress \
 -H "Authorization: Bearer $YA_API_KEY" \
 -H "OpenAI-Project: $FOLDER_ID" \
  | jq '[.data[]]|length' \
  )
echo "Not comleted docs: $res"
