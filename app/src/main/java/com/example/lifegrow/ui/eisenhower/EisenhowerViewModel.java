package com.example.lifegrow.ui.eisenhower;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EisenhowerViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public EisenhowerViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}