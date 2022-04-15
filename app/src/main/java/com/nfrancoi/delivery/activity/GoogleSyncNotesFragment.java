package com.nfrancoi.delivery.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.viewmodel.SyncNotesViewModel;
import com.nfrancoi.delivery.worker.SyncNotesWorker;

import java.util.Calendar;
import java.util.concurrent.Executors;

public class GoogleSyncNotesFragment extends Fragment {

    private SyncNotesViewModel syncNotesViewModel;


    private Button syncNotesStartButton;
    private TextView syncNotesFeedback;
    private DatePicker fromDatePicker;


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

        syncNotesViewModel = new ViewModelProvider(this.requireActivity()).get(SyncNotesViewModel.class);


        //
        // Date Picker
        //
        fromDatePicker = view.findViewById(R.id.fragment_sync_notes_from_datePicker);
        DatePicker.OnDateChangedListener dpListener = new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int day) {
                Calendar fromCalendar = Calendar.getInstance();
                fromCalendar.set(Calendar.YEAR, year);
                fromCalendar.set(Calendar.MONTH, month );
                fromCalendar.set(Calendar.DAY_OF_MONTH, day);
                fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
                fromCalendar.set(Calendar.MINUTE, 0);
                fromCalendar.set(Calendar.SECOND, 0);
                fromCalendar.set(Calendar.MILLISECOND, 0);

                syncNotesViewModel.setFromDate(fromCalendar);


            }
        };

        if (syncNotesViewModel.getFromDateLiveData().getValue() == null) {
            //init livedata
            Calendar previousMonth = Calendar.getInstance();
            previousMonth.add(Calendar.MONTH, -1);
            int year = previousMonth.get(Calendar.YEAR);
            int month = previousMonth.get(Calendar.MONTH);
            int day = previousMonth.get(Calendar.DAY_OF_MONTH);
            fromDatePicker.init(year, month, day, dpListener);
            syncNotesViewModel.setFromDate(previousMonth);
        } else {
            Calendar fromCalendar = syncNotesViewModel.getFromDateLiveData().getValue();
            int year = fromCalendar.get(Calendar.YEAR);
            int month = fromCalendar.get(Calendar.MONTH);
            int day = fromCalendar.get(Calendar.DAY_OF_MONTH);
            fromDatePicker.init(year, month, day, dpListener);
        }


        syncNotesFeedback = view.findViewById(R.id.fragment_sync_notes_result_text);
        syncNotesViewModel.getLogsLiveData().observe(getViewLifecycleOwner(), strings -> {

            if (strings != null && strings.size() > 0) {
                syncNotesFeedback.setText("");

                StringBuilder textStringBuilder = new StringBuilder();
                for (String string : strings) {
                    textStringBuilder.append(string + "\n");
                }
                syncNotesFeedback.append(textStringBuilder.toString());
            }
        });


        //
        // Progress Bar
        //
        ProgressBar progressbar = view.findViewById(R.id.fragment_sync_notes_result_text_progressbar);
        progressbar.setMax(100);
        progressbar.setMin(0);
        syncNotesViewModel.getProgressLiveData().observe(getViewLifecycleOwner(), progress -> {
                    progressbar.setProgress(progress);
                }
        );
        //
        // Start button
        //
        syncNotesStartButton = view.findViewById(R.id.fragment_sync_notes_start_button);
        syncNotesStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncNotesViewModel.deleteLog();
                syncNotesViewModel.setProgress(0);

                Executors.newSingleThreadExecutor().execute(() -> {
                            SyncNotesWorker worker = new SyncNotesWorker(GoogleSyncNotesFragment.this);
                            worker.doWork();
                        }
                );
            }
        });


        return view;
    }

}