package com.ibmcloud.example.cosgallerystarter.service;

import android.content.Context;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.ibmcloud.example.cosgallerystarter.R;

import java.util.Observable;

public class COSSingleton {
    private static AmazonS3Client cos;

    private static String accessKey;
    private static String secretAccessKey;
    private static String endpoint;
    private static String region;

    private static String defaultBucket;

    private static Observable observable;

    public static AmazonS3Client createInstance() {

        AmazonS3Client cos = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretAccessKey));
        cos.setRegion(Region.getRegion(Regions.US_EAST_1));
        cos.setEndpoint(endpoint);

        COSSingleton.cos = cos;
        return cos;
    }


    public static AmazonS3Client getInstance() {
        if (cos == null) {
            cos = createInstance();
        }
        return cos;
    }

    public static void initialize(Context context) {
        accessKey = context.getString(R.string.objectstorageAccessKey1);
        secretAccessKey = context.getString(R.string.objectstorageSecretAccessKey1);
        defaultBucket = context.getString(R.string.objectstorageBucket1);
        region = context.getString(R.string.objectstorageRegion1);
        endpoint = context.getString(R.string.objectstorageEndpoint1);
    }

    public static String getDefaultBucket() {
        return defaultBucket;
    }

    public static Observable getUpdates() {
        if (observable == null) {
            observable = new Observable();
        }
        return observable;
    }
}
