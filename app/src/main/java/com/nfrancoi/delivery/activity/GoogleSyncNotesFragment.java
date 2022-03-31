package com.nfrancoi.delivery.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nfrancoi.delivery.DeliveryApplication;
import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.googleapi.GoogleApiGateway;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class GoogleSyncNotesFragment extends Fragment {

    private Button syncNotesStartButton;
    private TextView syncNotesFeedback;


    public static GoogleSyncNotesFragment newInstance() {
        GoogleSyncNotesFragment fragment = new GoogleSyncNotesFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        requireActivity().setTitle(R.string.fragment_sync_notes_fragment_title);

        View view = inflater.inflate(R.layout.fragment_google_sync_notes, container, false);

        syncNotesFeedback = view.findViewById(R.id.fragment_sync_notes_result_text);

        syncNotesStartButton = view.findViewById(R.id.fragment_sync_notes_start_button);
        syncNotesStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncNotesFeedback.setText("");

                syncNotes();
            }
        });


        return view;
    }

    private void syncNotes() {

        Executors.newSingleThreadExecutor().execute(() -> {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            //
            // Create directory
            //
            String missingNotesDirectoryId;
            try {
                String rootDirectoryId = GoogleApiGateway.getInstance().createDirectory("DeliveryWorkspace", null);
                missingNotesDirectoryId = GoogleApiGateway.getInstance().createDirectory("NotesManquantes", rootDirectoryId);
            } catch (IOException e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    syncNotesFeedback.setText(e.toString());
                });
                return;
            }

            //
            // Loop on local files
            //
            File notesDirectory = DeliveryApplication.getApplicationNotesStorageDirectory();
            FilenameFilter filenamePdfFilter = (file, name) -> name.toLowerCase().endsWith(".pdf");

            File[] noteFiles = notesDirectory.listFiles(filenamePdfFilter);
            mainHandler.post(() -> {
                syncNotesFeedback.append(noteFiles.length + " Notes d'envoi" + "\n");
            });

            if (noteFiles != null) {
                Arrays.stream(noteFiles)
                        .forEach(file -> {
                            try {
                                //
                                // Check if  the note is on the google drive
                                //
                                if (!GoogleApiGateway.getInstance().isFileExistsByNameOnGoogleDrive(file.getName())) {
                                    GoogleApiGateway.getInstance().savePdfFileOnGoogleDrive(file, missingNotesDirectoryId);
                                    mainHandler.post(() -> {
                                        syncNotesFeedback.append(file.getName() + " MANQUANT ENVOYE !!" + "\n");
                                    });
                                }else{
                                    mainHandler.post(() -> {
                                        syncNotesFeedback.append(file.getName() + " Synchro \n");
                                    });
                                }

                            } catch (IOException e) {
                                mainHandler.post(() -> {
                                    syncNotesFeedback.append(file.getAbsolutePath() + " ne peut pas être envoyé \n");
                                    syncNotesFeedback.append(e.toString());
                                });
                                return;
                            }


                        });
            }
            mainHandler.post(() -> {
                syncNotesFeedback.append("Terminé");
            });

        });


    }

    private void sentToGoogleDriveIfNotAlreadyUploaded(File file) {


    }
}