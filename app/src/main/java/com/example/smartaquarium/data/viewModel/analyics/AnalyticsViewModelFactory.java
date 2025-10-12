package com.example.smartaquarium.data.viewModel.analyics;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

/**
 * Factory class for creating instances of {@link AnalyticsViewModel}.
 * This factory is responsible for passing the required dependencies, {@link Application} and {@link ViewModelStoreOwner},
 * to the ViewModel's constructor. It ensures that the ViewModel is instantiated with the necessary context
 * and lifecycle owner, which are crucial for its operation, particularly for handling Android framework components.
 */
public class AnalyticsViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final ViewModelStoreOwner owner;

    public AnalyticsViewModelFactory(@NonNull Application application, @NonNull ViewModelStoreOwner owner) {
        this.application = application;
        this.owner = owner;
    }

    /**
     * Creates a new instance of the given {@code Class}.
     * <p>
     * This method checks if the requested {@code modelClass} is assignable from {@link AnalyticsViewModel}.
     * If it is, a new instance of {@link AnalyticsViewModel} is created and returned.
     * Otherwise, an {@link IllegalArgumentException} is thrown.
     *
     * @param modelClass a {@code Class} whose instance is requested
     * @param <T>        The type parameter for the ViewModel.
     * @return a newly created ViewModel
     * @throws IllegalArgumentException if the given {@code modelClass} is not a known ViewModel class.
     * @throws RuntimeException if the ViewModel instance cannot be created.
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AnalyticsViewModel.class)) {
            try {
                return (T) new AnalyticsViewModel(application, owner);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
