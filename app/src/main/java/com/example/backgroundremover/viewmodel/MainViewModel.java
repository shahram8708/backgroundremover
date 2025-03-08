package com.example.backgroundremover.viewmodel;

import android.graphics.Bitmap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<Bitmap> originalImage = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> processedImage = new MutableLiveData<>();

    public void setOriginalImage(Bitmap bitmap) {
        originalImage.setValue(bitmap);
    }
    
    public LiveData<Bitmap> getOriginalImage() {
        return originalImage;
    }

    public void setProcessedImage(Bitmap bitmap) {
        processedImage.setValue(bitmap);
    }
    
    public LiveData<Bitmap> getProcessedImage() {
        return processedImage;
    }
}
