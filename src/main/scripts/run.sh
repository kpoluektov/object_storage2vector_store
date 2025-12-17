#!/bin/sh
AWS_JAVA_V1_DISABLE_DEPRECATION_ANNOUNCEMENT=true
java -jar vector-store-loader/build/libs/VectorSearch-1.0-SNAPSHOT-all.jar application.json
