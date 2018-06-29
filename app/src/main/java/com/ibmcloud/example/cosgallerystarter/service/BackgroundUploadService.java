package com.ibmcloud.example.cosgallerystarter.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.ibmcloud.example.cosgallerystarter.R;
import com.ibmcloud.example.cosgallerystarter.model.ImageObjectSummaryModel;

import java.io.File;

public class BackgroundUploadService extends IntentService {
    public static final String OBJECT_KEY = "key";
    public static final String LOCAL_PATH = "uri";

    public BackgroundUploadService() {
        super("BackgroundUploadService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String key = intent.getStringExtra(OBJECT_KEY);
        String path = intent.getStringExtra(LOCAL_PATH);

        String bucket = COSSingleton.getDefaultBucket();

        CharSequence channelName = getString(R.string.notification_channel_name);
        String channelDescription = getString(R.string.notification_channel_description);
        String channelId = channelName.toString();

        //Notification setup for Android O and up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        TransferUtility transferUtility = TransferUtility.builder()
                .s3Client(COSSingleton.getInstance())
                .context(getApplicationContext())
                .build();

        File file = new File(path);

        TransferObserver observer = transferUtility.upload(bucket, key, file);

        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Image upload")
                .setContentText("State: NULL")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setChannelId(channelId)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        final int PROGRESS_MAX = 100;
        final int PROGRESS_CURRENT = 0;

        // Base our notification IDs off the TransferObserver
        final int notificationId = observer.getId();

        mBuilder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, true);
        notificationManager.notify(notificationId, mBuilder.build());

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    notificationManager.cancel(id);

                    COSSingleton.getUpdates().notifyObservers();
                } else {
                    mBuilder.setProgress(PROGRESS_CURRENT, PROGRESS_MAX, state != TransferState.IN_PROGRESS);
                    mBuilder.setContentText("State: " + state.toString());
                    notificationManager.notify(id, mBuilder.build());
                }

            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int progress = (int)(100d * bytesCurrent / bytesTotal);
                mBuilder.setProgress(progress, PROGRESS_MAX, false);
                notificationManager.notify(id, mBuilder.build());
            }

            @Override
            public void onError(int id, Exception ex) {
                mBuilder.setProgress(0, 0, false);
                mBuilder.setOngoing(false);
                mBuilder.setContentText("Error: " + ex.toString());
                Log.e("BackgroundUploadService", "Error in notification", ex);
            }
        });
    }
}
