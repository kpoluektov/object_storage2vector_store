package org.vsearch;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;

public class S3Service {
    private final String accessKey;
    private final String secretKey;
    private final String endpoint;
    private final String region;
    private final AmazonS3 s3Client;


    public S3Service(
            String accessKey,
            String secretKey,
            String endpoint,
            String region)
    {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.endpoint = endpoint;
        this.region = region;
        this.s3Client = initializeS3();
    }

    public AmazonS3 initializeS3() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AwsClientBuilder.EndpointConfiguration config = new AwsClientBuilder.EndpointConfiguration(endpoint, region);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(config)
                .build();
    }
    public S3Object getObject(String bucket, String key){
        return s3Client.getObject(bucket, key);
    }
    public ObjectListing getObjects(String bucket, String prefix){
        return s3Client.listObjects(bucket, prefix);
    }
}
