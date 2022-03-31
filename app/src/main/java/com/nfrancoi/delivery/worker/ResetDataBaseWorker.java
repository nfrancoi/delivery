package com.nfrancoi.delivery.worker;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nfrancoi.delivery.DeliveryApplication;
import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.repository.Repository;


public class ResetDataBaseWorker extends Worker {

    private static final String TAG = ResetDataBaseWorker.class.toString();


    private static String NOTIFICATION_ERROR_TITLE = DeliveryApplication.getInstance().getString(R.string.preference_about_reset_notification_ko);
    private static String NOTIFICATION_OK_TITLE = DeliveryApplication.getInstance().getString(R.string.preference_about_reset_notification_ok);

    public ResetDataBaseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Repository.getInstance().resetDatabaseSync();
        } catch (Exception e) {
            WorkerUtils.makeStatusNotification(NOTIFICATION_ERROR_TITLE, e.getMessage(), getApplicationContext());

            SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(DeliveryApplication.getInstance().getBaseContext()).edit();
            prefs.putString("preference_about_reset", NOTIFICATION_ERROR_TITLE +" "+e.getMessage());
            prefs.commit();


            new HandlerToast(getApplicationContext(), NOTIFICATION_ERROR_TITLE);

            return Result.failure();
        }

        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(DeliveryApplication.getInstance().getBaseContext()).edit();
        prefs.putString("preference_about_reset", NOTIFICATION_OK_TITLE);
        prefs.commit();

        new HandlerToast(getApplicationContext(), NOTIFICATION_OK_TITLE);

        return Result.success();
    }


}
