## Vector_store file loader utility

build it
```sh
./gradlew build shadowJar -x test
```
prepare config file
```
s3:
  accessKey: 
  secretKey: 
  endpoint: https://storage.yandexcloud.net/
  region: ru-central1
  bucket: pol-some-docs
  prefix: files/Д
  extension: txt
aistudio:
  apiKey: 
  baseUrl: https://rest-assistant.api.cloud.yandex.net/v1
  project: 
#  indexId: fvt9dlohrb9suldut647
  indexName: myTestIndex
  retryCount: 100
  waitMillis: 1000
  finalStatus: ADDED
pg:
  jdbcUrl: jdbc:postgresql://rc1a-yandexcloudid.mdb.yandexcloud.net:6432/pol?targetServerType=master&ssl=true&sslmode=verify-full
  username: 
  password: 
  connectionTimeout: 5000
  maximumPoolSize: 15
```

run it 
```sh
java -jar build/libs/VectorSearch-1.0-SNAPSHOT-all.jar  ../application.json &
```