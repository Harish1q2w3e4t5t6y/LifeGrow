package com.example.lifegrow.ui.others;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OtherViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public OtherViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Welcome to Other Settings"); // Optional default text
    }

    public LiveData<String> getText() {
        return mText;
    }
}
