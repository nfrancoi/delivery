package com.nfrancoi.delivery.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nfrancoi.delivery.DeliveryApplication;
import com.nfrancoi.delivery.googleapi.GoogleApiGateway;
import com.nfrancoi.delivery.repository.Repository;
import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.tools.CalendarTools;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;


public class SyncNotesWorker extends Worker {


    private static final String TAG = SyncNotesWorker.class.toString();
    public static final String PROGRESS = "PROGRESS";
    public static final String LOG = "LOG";

    public SyncNotesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        Data.Builder builder = new Data.Builder();
        //
        // Create directory
        //
        String rootDirectoryId;
        String missingNotesDirectoryId;
        try {
            rootDirectoryId = GoogleApiGateway.getInstance().createDirectory("DeliveryWorkspace", null);
            missingNotesDirectoryId = GoogleApiGateway.getInstance().createDirectory("_NotesManquantes", rootDirectoryId);
        } catch (IOException e) {
            setProgressAsync(builder.putString(LOG, e.getMessage()).build());
            return Result.failure();
        }
        setProgressAsync(builder.putString(LOG, "Répertoire créés").build());

        //
        // Loop on local files
        //
        File notesDirectory = DeliveryApplication.getApplicationNotesStorageDirectory();
        FilenameFilter filenamePdfFilter = (file, name) -> name.toLowerCase().endsWith(".pdf");

        File[] noteFiles = notesDirectory.listFiles(filenamePdfFilter);
        if (noteFiles != null) {
            int totalCountNoteFiles = noteFiles.length;
            setProgressAsync(builder.putString(LOG, totalCountNoteFiles + "Notes d'envoi").build());

            for (int i = 0; totalCountNoteFiles > i; i++) {
                setProgressAsync(builder.putInt(PROGRESS, i).build());


                try {
                    //
                    // Check if  the note is on the google drive
                    //
                    File currentFile = noteFiles[i];
                    if (GoogleApiGateway.getInstance().getFileIdByNameOnGoogleDrive(currentFile.getName()) != null) {
                        setProgressAsync(builder.putString(LOG, currentFile.getName() + " Synchro").build());
                    } else {

                        //
                        // Save the note
                        //
                        Delivery delivery = Repository.getInstance().getDeliveryFromNoteFileNameSync(currentFile.getName());

                        if (delivery == null) {
                            //save pdf file in missing notes
                            setProgressAsync(builder.putString(LOG, currentFile.getName() + " MANQUANTE sur le drive et la db").build());
                            GoogleApiGateway.getInstance().savePdfFileOnGoogleDrive(currentFile, missingNotesDirectoryId);
                        } else {
                            String podDirectoryId = GoogleApiGateway.getInstance().createDirectory(delivery.pointOfDelivery.name, rootDirectoryId);
                            String podDirectoryDateId = GoogleApiGateway.getInstance().createDirectory(CalendarTools.YYYYMM.format(delivery.startDate.getTime()), podDirectoryId);

                            setProgressAsync(builder.putString(LOG, currentFile.getName() + " MANQUANTE sur le drive").build());
                            GoogleApiGateway.getInstance().savePdfFileOnGoogleDrive(currentFile, podDirectoryDateId);

                            Repository.getInstance().updateDeliveryNoteSentSync(delivery.deliveryId);

                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    setProgressAsync(builder.putString(LOG, e.getMessage()).build());
                    return Result.failure();
                }
            }
            setProgressAsync(builder.putString(LOG, "Terminé").build());


        }
        return Result.success();

    }
}
