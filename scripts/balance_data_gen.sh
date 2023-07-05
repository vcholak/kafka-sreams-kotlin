curl --request POST \
  --url http://localhost:8082/topics/Balance \
  --header 'accept: application/vnd.kafka.v2+json' \
  --header 'content-type: application/vnd.kafka.avro.v2+json' \
  --data '{
    "key_schema": "{\"name\":\"key\",\"type\": \"string\"}",
    "value_schema_id": "9",
    "records": [
        {
            "key" : "678",
            "value": {
                "balanceId": "1023",
                "accountId" : "12",
                "balance" : 20.34
            }
        }
    ]
}'