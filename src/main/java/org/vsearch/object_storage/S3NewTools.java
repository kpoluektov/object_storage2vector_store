package org.vsearch.object_storage;

import org.vsearch.Utils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URI;
import java.util.List;

public class S3NewTools {
    static S3Client s3Client;
    static ListObjectsV2Response lastResponse;
    static String nextContinuationToken = null;
    public static void init(String accessKey,
                     String secretKey,
                     String endpoint,
                     String region) {
        s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(() -> AwsBasicCredentials.create(accessKey, secretKey))
                .build();

    }
    public static void init(){
        init(
                Utils.getString(Utils.S3, "accessKey"),
                Utils.getString(Utils.S3, "secretKey"),
                Utils.getString(Utils.S3, "endpoint"),
                Utils.getString(Utils.S3, "region")
        );
    }
    public static List<S3Object> getPaginatedResponse(String bucket, String prefix){
        ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .continuationToken(nextContinuationToken);

        lastResponse = s3Client.listObjectsV2(requestBuilder.build());
        nextContinuationToken = lastResponse.nextContinuationToken();
        return lastResponse.contents();
    }

    public static boolean hasMore(){
        return nextContinuationToken != null;
    }

    public static void close(){
        s3Client.close();
    }
    public static ResponseInputStream<GetObjectResponse> getObjectContent(String bucket, String key){
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObject(request);
    }


}
