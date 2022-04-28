package com.nfrancoi.delivery.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class SyncNotesViewModel extends AndroidViewModel {

    private MutableLiveData<List<String>> logsLiveData = new MutableLiveData<>(new LinkedList<>());
    private MutableLiveData<Integer> progressLiveData = new MutableLiveData<>(0);
    private MutableLiveData<Calendar> fromDateLiveData = new MutableLiveData<>();



    public SyncNotesViewModel(Application application) {
        super(application);

    }

    public LiveData<List<String>> getLogsLiveData() {
        return logsLiveData;
    }

    public void deleteLog() {
        logsLiveData.setValue(new LinkedList<>());

    }

    public void addLog(String log) {
        List<String> logs = logsLiveData.getValue();
        logs.add(log);
        logsLiveData.postValue(logs);

    }

    public void setProgress(Integer progress) {
        progressLiveData.postValue(progress);

    }
    public LiveData<Integer> getProgressLiveData() {
        return progressLiveData;

    }

    public LiveData<Calendar> getFromDateLiveData() {
        return fromDateLiveData;

    }
    public void setFromDate(Calendar from){
        fromDateLiveData.postValue(from);
    }




}