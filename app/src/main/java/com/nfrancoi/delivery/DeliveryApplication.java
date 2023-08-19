package com.nfrancoi.delivery;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.DialogConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.data.StringFormat;

import java.io.File;

public class DeliveryApplication extends Application {

    private static DeliveryApplication instance = null;

    public static DeliveryApplication getInstance() {
        return instance;
    }


    public static File getApplicationNotesStorageDirectory(){
        return getApplicationExternalStorageDirectoryDocument();
    }

    ///storage/emulated/0/
    public static File getApplicationExternalStorageDirectoryDocument(){

        // File directory = new File(Environment.getExternalStorageDirectory(), BuildConfig.APPLICATION_ID);
        File directory = getInstance().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

        if(!directory.exists()){
            directory.mkdir();
        }
        return directory;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        instance = this;
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this, new CoreConfigurationBuilder()
                //core configuration:
                .withBuildConfigClass(BuildConfig.class)
                .withReportFormat(StringFormat.KEY_VALUE_LIST)
                .withPluginConfigurations(new MailSenderConfigurationBuilder()
                        .withMailTo("nfrancoi@gmail.com")
                        .withSubject("Error report")
                        .withReportAsFile(false)
                        .build(),
                        new DialogConfigurationBuilder()
                                .withText(getString(R.string.acra_dialog_errormessage))
                                .build())
        );
    }



}
