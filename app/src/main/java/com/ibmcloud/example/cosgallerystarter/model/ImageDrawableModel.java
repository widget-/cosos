package com.ibmcloud.example.cosgallerystarter.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.graphics.drawable.Drawable;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.ibmcloud.example.cosgallerystarter.service.COSSingleton;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageDrawableModel extends ViewModel {
    private Map<String, MutableLiveData<Drawable>> mDrawableList;

    public LiveData<Drawable> getImage(final S3ObjectSummary summary, boolean forceRefresh) {
        if (mDrawableList == null) {
            mDrawableList = new HashMap<>();
        }

        if (mDrawableList.containsKey(summary.getKey()) && !forceRefresh) {
            return mDrawableList.get(summary.getKey());
        } else {
            final MutableLiveData<Drawable> data = new MutableLiveData<>();
            mDrawableList.put(summary.getKey(), data);

            ExecutorService service = Executors.newCachedThreadPool();
            service.submit(new Runnable() {
                @Override public void run() {
                    AmazonS3Client cos = COSSingleton.getInstance();
                    S3Object object = cos.getObject(summary.getBucketName(), summary.getKey());
                    S3ObjectInputStream stream = object.getObjectContent();
                    Drawable img = Drawable.createFromStream(stream, summary.getKey());
                    data.postValue(img);
                }
            });

            return data;
        }
    }

}
