package org.vsearch;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;

public class S3Tools {
    public static S3Service service;
    public static void init(String accessKey,
                            String secretKey,
                            String endpoint,
                            String region) {
        service = new S3Service(accessKey, secretKey, endpoint, region);
    }
    public static void init(){
        init(
                Utils.getString("s3", "accessKey"),
                Utils.getString("s3", "secretKey"),
                Utils.getString("s3", "endpoint"),
                Utils.getString("s3", "region")
        );
    }
    public static S3Object getObject(String bucket, String key){
        return service.getObject(bucket, key);
    }
    public static ObjectListing getObjects(String bucket, String prefix){
        return service.getObjects(bucket, prefix);
    }

}
