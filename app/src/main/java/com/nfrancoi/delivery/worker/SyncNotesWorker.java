package com.nfrancoi.delivery.worker;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.nfrancoi.delivery.DeliveryApplication;
import com.nfrancoi.delivery.googleapi.GoogleApiGateway;
import com.nfrancoi.delivery.repository.Repository;
import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.tools.CalendarTools;
import com.nfrancoi.delivery.viewmodel.SyncNotesViewModel;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;


public class SyncNotesWorker {


    private static final String TAG = SyncNotesWorker.class.toString();

    private Fragment fragment;
    private SyncNotesViewModel syncNotesViewModel;


    private String rootDirectoryId;
    private String missingNotesDirectoryId;


    public SyncNotesWorker(Fragment fragment) {
        this.fragment = fragment;

    }


    public void doWork() {

        syncNotesViewModel = new ViewModelProvider(fragment.getActivity()).get(SyncNotesViewModel.class);


        //
        // Create directory
        //
        try {
            rootDirectoryId = GoogleApiGateway.getInstance().createDirectory("DeliveryWorkspace", null);
            missingNotesDirectoryId = GoogleApiGateway.getInstance().createDirectory("_NotesManquantes", rootDirectoryId);
        } catch (IOException e) {
            syncNotesViewModel.addLog(e.getMessage());
            return;
        }

        syncNotesViewModel.addLog("Répertoire créés");

        //
        // Loop on local files
        //
        File notesDirectory = DeliveryApplication.getApplicationNotesStorageDirectory();
        Calendar fromCalendar = syncNotesViewModel.getFromDateLiveData().getValue();

        syncNotesViewModel.addLog("Recherche des notes d'envoi depuis: " + CalendarTools.YYYYMMDD.format(fromCalendar.getTime()));

        FilenameFilter filenamePdfFilter = (file, name) -> {
            if (!name.toLowerCase().endsWith(".pdf")) {
                return false;
            }
            try {
                Date fileDate = CalendarTools.YYYYMMDD.parse(name.toLowerCase().substring(0, 8));
                Calendar fileCalendar = Calendar.getInstance();
                fileCalendar.setTime(fileDate);

                if (fileCalendar.getTimeInMillis() < fromCalendar.getTimeInMillis()) {
                    return false;
                } else {
                    return true;
                }


            } catch (ParseException e) {
                syncNotesViewModel.addLog(e.getMessage());
                return false;
            }

        };

        File[] noteFiles = notesDirectory.listFiles(filenamePdfFilter);
        if (noteFiles != null) {
            int totalCountNoteFiles = noteFiles.length;
            syncNotesViewModel.addLog(totalCountNoteFiles + "Notes d'envoi");

            syncNotesViewModel.setProgress(0);
            for (int i = 0; i < totalCountNoteFiles; i++) {
                this.syncNote(noteFiles[i]);
                int progress = Math.round((Float.valueOf(i) / Float.valueOf(totalCountNoteFiles)) * 100);
                syncNotesViewModel.setProgress(progress);
            }
            syncNotesViewModel.setProgress(100);
        }
        syncNotesViewModel.addLog("Terminé");

    }

    private StringBuilder syncNote(File noteFile) {
        StringBuilder log = new StringBuilder(noteFile.getName());
        try {
            Delivery delivery = Repository.getInstance().getDeliveryFromNoteFileNameSync(noteFile.getName());
            boolean isFileOnDrive = GoogleApiGateway.getInstance().getFileIdByNameOnGoogleDrive(noteFile.getName()) != null;


            if (!isFileOnDrive) {
                if (delivery == null) {
                    //save pdf file only
                    if (GoogleApiGateway.getInstance().savePdfFileOnGoogleDrive(noteFile, missingNotesDirectoryId) != null) {
                        log.append(" PDF RENVOYE");
                    } else {
                        log.append(" PDF ECHEC");
                    }

                } else {
                    //save pdf file & update db
                    delivery.syncErrorMessage = null;
                    Repository.getInstance().updateSync(delivery);
                    if (Repository.getInstance().saveDeliveryNoteToGoogleDrive(delivery)) {
                        log.append(" PDF RENVOYE");
                    } else {
                        log.append(" PDF ECHEC: " + delivery.syncErrorMessage);
                    }


                }

            } else {
                log.append(" PDF OK");
            }
            log.append(" |");


            if (delivery != null) {
                boolean isBillingDataOnDrive = Repository.getInstance().isDeliveryDetailsToGoogleSpreadSheet(delivery);
                if (isBillingDataOnDrive) {
                    log.append(" XLS OK");
                } else {
                    delivery.syncErrorMessage = null;
                    Repository.getInstance().updateSync(delivery);
                    if (Repository.getInstance().saveDeliveryDetailsToGoogleSpreadSheet(delivery)) {
                        log.append(" XLS RENVOYE");
                    } else {
                        log.append(" XLS ECHEC: " + delivery.syncErrorMessage);
                    }
                }
            } else {
                log.append(" XLS MANQUANT");
            }


            syncNotesViewModel.addLog(log.toString());


        } catch (Exception e) {
            e.printStackTrace();
            syncNotesViewModel.addLog(log.toString() + " ERROR: " + e.getMessage());
        }
        return log;

    }

}


