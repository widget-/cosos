package com.ibmcloud.example.cosgallerystarter.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.ibmcloud.example.cosgallerystarter.service.COSSingleton;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageObjectSummaryModel extends ViewModel {
    private MutableLiveData<List<S3ObjectSummary>> mObjectList;

    public LiveData<List<S3ObjectSummary>> getObjectList() {
        if (mObjectList == null) {
            mObjectList = new MutableLiveData<>();
            refreshObjects();
        }
        return mObjectList;
    }

    public void refreshObjects() {
        ExecutorService service = Executors.newCachedThreadPool();
        final String bucketName = COSSingleton.getDefaultBucket();
        service.submit(new Runnable() {
            @Override
            public void run() {
                AmazonS3Client cos = COSSingleton.getInstance();
                ObjectListing listing = null;
                try {
                    listing = cos.listObjects(bucketName);
                } catch (Exception e) {
                    Log.e("ImageObjectSummaryModel", e.getMessage(), e);
                    return;
                }
                List<S3ObjectSummary> objects = listing.getObjectSummaries();
                mObjectList.postValue(objects);

                COSSingleton.getUpdates().addObserver(new Observer() {
                    @Override public void update(Observable o, Object arg) {
                        refreshObjects();
                    }
                });
            }
        });
    }

}
