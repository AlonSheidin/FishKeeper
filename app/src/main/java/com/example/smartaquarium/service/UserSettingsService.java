package com.example.smartaquarium.service;

import android.util.Log;import androidx.lifecycle.LiveData;

import com.example.smartaquarium.data.datasource.FirestoreDataSource; // Assuming this class exists
import com.example.smartaquarium.data.model.UserSettings;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A service class dedicated to handling the business logic for user settings.
 * It acts as an intermediary between the UI (e.g., SettingsFragment) and the
 * data layer (FirestoreDataSource), promoting a clean architecture.
 */
public class UserSettingsService {

    private static final String TAG = "UserSettingsService";

    // --- Dependencies ---
    private final FirestoreDataSource firestoreDataSource;
    private final FirebaseAuth firebaseAuth;

    /**
     * Public constructor for the service.
     */
    public UserSettingsService() {
        // Use an "init" style function to set up the required dependencies.
        this.firestoreDataSource = initializeDataSource();
        this.firebaseAuth = initializeFirebaseAuth();
    }

    /**
     * Initializes the data source dependency.
     * @return An instance of FirestoreDataSource.
     */
    private FirestoreDataSource initializeDataSource() {
        return new FirestoreDataSource();
    }

    /**
     * Initializes the authentication service dependency.
     * @return An instance of FirebaseAuth.
     */
    private FirebaseAuth initializeFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    /**
     * Retrieves the settings for the currently logged-in user.
     * This method encapsulates the logic of getting the user ID.
     *
     * @return A LiveData object containing the UserSettings. The LiveData will
     *         contain default settings if no user is logged in or if an error occurs.
     */
    public LiveData<UserSettings> getSettingsForCurrentUser() {
        String currentUserId = getCurrentUserId();
        // The getUserSettings method in the data source is already designed
        // to handle a null or empty ID, so this is safe.
        return firestoreDataSource.getUserSettings(currentUserId);
    }

    /**
     * Saves the provided settings for the currently logged-in user.
     * This method handles validation before attempting to save.
     *
     * @param settingsToSave The UserSettings object with the new values.
     * @return A Task that can be used to track the completion of the save operation.
     *         The Task will fail if no user is logged in.
     */
    public Task<Void> saveSettingsForCurrentUser(UserSettings settingsToSave) {
        String currentUserId = getCurrentUserId();

        // Perform validation before calling the data source
        if (currentUserId == null || currentUserId.isEmpty()) {
            String errorMessage = "Cannot save settings. No user is logged in.";
            Log.e(TAG, errorMessage);
            // Return a failed task immediately if validation fails.
            return Tasks.forException(new IllegalStateException(errorMessage));
        }

        if (settingsToSave == null) {
            String errorMessage = "Cannot save null settings.";
            Log.e(TAG, errorMessage);
            return Tasks.forException(new IllegalArgumentException(errorMessage));
        }

        Log.d(TAG, "Proceeding to save settings for user: " + currentUserId);
        return firestoreDataSource.saveUserSettings(currentUserId, settingsToSave);
    }

    /**
     * A helper method to safely get the current user's unique ID.
     *
     * @return The user's UID, or null if no user is logged in.
     */
    private String getCurrentUserId() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }
        return null;
    }
}
