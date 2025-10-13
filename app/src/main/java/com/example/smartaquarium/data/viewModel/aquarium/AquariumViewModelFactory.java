package com.example.smartaquarium.data.viewModel.aquarium;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

public class AquariumViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final ViewModelStoreOwner owner;

    public AquariumViewModelFactory(@NonNull Application application, @NonNull ViewModelStoreOwner owner) {
        this.application = application;
        this.owner = owner;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AquariumViewModel.class)) {
            try {
                return (T) new AquariumViewModel(application, owner);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
