package com.nfrancoi.delivery.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nfrancoi.delivery.repository.Repository;
import com.nfrancoi.delivery.room.entities.Delivery;


public class UploadNoteWorker extends Worker {


    private static final String TAG = UploadNoteWorker.class.toString();

    public static final String PARAM_DELIVERY_ID = "PARAM_DELIVERY_ID";
    private final long deliveryId;


    public UploadNoteWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        Data inputData = workerParams.getInputData();
        this.deliveryId = inputData.getLong(PARAM_DELIVERY_ID, 0);
    }

    @NonNull
    @Override
    public Result doWork() {

        if (getRunAttemptCount() > 10) {
            return Result.failure();
        }

        Delivery delivery = Repository.getInstance().getDeliverySync(deliveryId);
        delivery.syncErrorMessage = null;
        Repository.getInstance().updateSync(delivery);

        boolean IsProcessOk;
        IsProcessOk = Repository.getInstance().saveDeliveryNoteToGoogleDrive(delivery);
        if (!IsProcessOk) {
            return Result.retry();
        }

        IsProcessOk =  Repository.getInstance().saveDeliveryDetailsToGoogleSpreadSheet(delivery);
        if (!IsProcessOk) {
            return Result.retry();
        }

        return Result.success();


    }

}
