package com.nfrancoi.delivery.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SyncNotesWorker extends Worker {
    public SyncNotesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        WorkerUtils.makeStatusNotification("Sync JOBBBB start", "sub message", super.getApplicationContext());
        return Result.success();
    }
}
