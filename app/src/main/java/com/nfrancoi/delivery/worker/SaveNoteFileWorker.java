package com.nfrancoi.delivery.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nfrancoi.delivery.googleapi.GoogleApiGateway;
import com.nfrancoi.delivery.repository.Repository;

import java.io.File;
import java.io.IOException;


public class SaveNoteFileWorker extends Worker {


    private static final String TAG = SaveNoteFileWorker.class.toString();

    public static final String PARAM_DELIVERY_ID = "PARAM_DELIVERY_ID";
    private final long deliveryId;

    public static final String PARAM_FILE_URI = "PARAM_FILEPATH";
    private final String fileUriPath;

    public static final String PARAM_GOOGLE_DIRECTORY_NAME = "PARAM_GOOGLE_DIRECTORY_URI";
    private final String googleDirectoryName;

    public SaveNoteFileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        Data inputData = workerParams.getInputData();
        this.fileUriPath = inputData.getString(PARAM_FILE_URI);
        this.deliveryId = inputData.getLong(PARAM_DELIVERY_ID, 0l);
        this.googleDirectoryName = inputData.getString(PARAM_GOOGLE_DIRECTORY_NAME);
    }

    @NonNull
    @Override
    public Result doWork() {


        File file = new File(fileUriPath);
        if (!file.exists()) {
            return Result.failure();
        }

        if(getRunAttemptCount() > 3){
            return Result.failure();
        }

        //save pdf file
        try {//create the directory Delivery

           String rootDirectoryId = GoogleApiGateway.getInstance().createDirectory("DeliveryWorkspace", null);

           String pointOfDeliveryDirectoryId = GoogleApiGateway.getInstance().createDirectory(googleDirectoryName, rootDirectoryId);


            GoogleApiGateway.getInstance().savePdfFileOnGoogleDrive(file, pointOfDeliveryDirectoryId);


        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();

        }

        int nbrUpdated = Repository.getInstance().updateDeliveryNoteSentSync(deliveryId);
        if (nbrUpdated != 1) {
            throw new IllegalStateException("deliveryId not exists?:" + deliveryId);
        }


        return Result.success();
    }

}
