package com.nfrancoi.delivery;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;
import org.acra.data.StringFormat;

import java.io.File;


@AcraCore(buildConfigClass = org.acra.BuildConfig.class,
        logcatArguments = {"-t", "200", "-v", "time"},
        reportFormat = StringFormat.KEY_VALUE_LIST,
        reportContent = {
                ReportField.USER_COMMENT,
                ReportField.APP_VERSION_NAME,
                ReportField.APP_VERSION_CODE,
                ReportField.ANDROID_VERSION,
                ReportField.PHONE_MODEL,
                ReportField.CUSTOM_DATA,
                ReportField.STACK_TRACE,
                ReportField.LOGCAT}
)
@AcraMailSender(reportAsFile = false,resSubject = R.string.acra_mail_subject,
        mailTo = "nfrancoi@gmail.com"
)
@AcraDialog(resText = R.string.acra_dialog_errormessage)
public class DeliveryApplication extends Application {

    ///storage/emulated/0/
    public static File getApplicationExternalStorageDirectory(){
        File directory = new File(Environment.getExternalStorageDirectory(), BuildConfig.APPLICATION_ID);
        if(!directory.exists()){
            directory.mkdir();
        }
        return directory;
    }

    @Override
    public void onCreate(){
        super.onCreate();
     //   StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
       // StrictMode.setVmPolicy(builder.build());
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }



}
