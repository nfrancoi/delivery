package com.nfrancoi.delivery.worker;

import android.accounts.Account;
import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.nfrancoi.delivery.repository.Repository;
import com.nfrancoi.delivery.room.entities.PointOfDelivery;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class SyncDataBaseWorker extends Worker {


    private static final String KEY_INPUT_URL = "KEY_INPUT_URL";
    private static final String KEY_OUTPUT_FILE_NAME = "KEY_OUTPUT_FILE_NAME";

    private NotificationManager notificationManager;


    public SyncDataBaseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        try {
            JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
            NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
            GoogleAccountCredential googleAccountCredential = this.getCredential(HTTP_TRANSPORT);


            Sheets sheets = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, googleAccountCredential)
                    .setApplicationName("test")
                    .build();

            String spreadsheetId = "1xt0yeaN2Am2xavqhbowQmaL9bzo-Sgp21ByBGuKWppk";

            //
            ValueRange result = sheets.spreadsheets().values().get(spreadsheetId, "PointsDeLivraison").setKey("AIzaSyCV_8QInXhAn9q92QsfJhRcaIRndDswOMc").execute();
            List<List<Object>> podObjects = result.getValues();

            List<PointOfDelivery> pods = this.mapToPointOfDelivery(result);

            Repository.getInstance().updateAllPointOfDelivery(pods);



        } catch (Exception e) {
            e.printStackTrace();
            WorkerUtils.makeStatusNotification("Error:"+e.getMessage(), getApplicationContext());
            return Result.failure();

        }

        String progress = "done";
        WorkerUtils.makeStatusNotification(progress, getApplicationContext());


        return Result.success();
    }


    private GoogleAccountCredential getCredential(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        GoogleSignInAccount gsoAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        Account account = gsoAccount.getAccount();
        String[] SCOPESArray = {"https://www.googleapis.com/auth/spreadsheets.readonly"};
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(SCOPESArray));
        credential.setBackOff(new ExponentialBackOff());
        credential.setSelectedAccount(account);
        return credential;
    }


    private List<PointOfDelivery> mapToPointOfDelivery(ValueRange result)  {
        List<List<Object>> resultList = result.getValues();
        List<PointOfDelivery> pods = resultList.stream().skip(1).map(objects -> {
            int i=0;
            Long podId = Long.valueOf(""+objects.get(i++));
            String name = ""+objects.get(i++);
            String address = ""+objects.get(i++);
            String mails = ""+objects.get(i++);
            BigDecimal discountPercentage = BigDecimal.valueOf(Double.parseDouble(""+objects.get(i++)));
            Boolean isActive =(""+objects.get(i++)).equals("Oui")?true:false;

            PointOfDelivery pod = new PointOfDelivery(podId,name, address,discountPercentage, mails, isActive);
            return pod;

        }).collect(Collectors.toList());

        return pods;

    }

}
