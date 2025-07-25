package com.example.lifegrow.ui.kanban;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class KanbanViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public KanbanViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}