package com.nfrancoi.delivery.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.worker.SyncNotesWorker;

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

                //save note file
                Constraints networkConstraint = new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build();

                OneTimeWorkRequest syncNoteFiles = new OneTimeWorkRequest.Builder(SyncNotesWorker.class)
                        .setConstraints(networkConstraint).addTag("SyncNotesWorker").build();



                WorkManager.getInstance(requireActivity().getApplicationContext()).getWorkInfoByIdLiveData(syncNoteFiles.getId()).observe(getActivity(), workInfo -> {
                    if (workInfo != null) {
                        Data progress = workInfo.getProgress();
                        int percentage = progress.getInt(SyncNotesWorker.PROGRESS, 0);
                        syncNotesFeedback.append(percentage + "\n");
                        String log = progress.getString(SyncNotesWorker.LOG);
                        if (log != null) {
                            syncNotesFeedback.append(log + "\n");
                        }
                    }
                });

                WorkManager.getInstance(requireActivity().getApplicationContext()).enqueueUniqueWork("SyncNotesWorker", ExistingWorkPolicy.REPLACE, syncNoteFiles);





                syncNotesFeedback.append("Attente du démarrage" + "\n");


            }
        });

        return view;
    }

}