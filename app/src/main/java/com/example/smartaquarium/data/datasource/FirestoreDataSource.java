package com.example.smartaquarium.data.datasource;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smartaquarium.data.model.AquariumData;
import com.example.smartaquarium.data.model.UserSettings; // Import the UserSettings model
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentReference;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles all direct data operations with the Google Firestore database.
 * This class is the lowest level of our application's data layer, responsible for
 * fetching and saving both aquarium data and user settings.
 */
public class FirestoreDataSource {

    private static final String TAG = "FirestoreDataSource";
    private static final String USERS_COLLECTION = "users";
    private static final String AQUARIUM_DATA_COLLECTION = "aquariumData";
    private static final String SETTINGS_COLLECTION = "settings"; // A dedicated subcollection for settings
    private static final String SETTINGS_DOCUMENT_NAME = "userSettings";

    private final FirebaseFirestore firestoreDatabase;

    /**
     * Public constructor for the data source.
     */
    public FirestoreDataSource() {
        // Use an "init" style function to set up the required dependency.
        this.firestoreDatabase = initializeFirestore();
    }

    /**
     * Initializes the Firestore database instance.
     * @return The singleton instance of FirebaseFirestore.
     */
    private FirebaseFirestore initializeFirestore() {
        return FirebaseFirestore.getInstance();
    }

    // --- Aquarium Data Methods ---

    /**
     * Retrieves a real-time stream of aquarium data for a specific user from Firestore.
     * Note: This implementation has a potential for memory leaks. A better approach
     * would be to use a custom LiveData class that manages the listener lifecycle.
     */
    public LiveData<List<AquariumData>> getUserAquariumData(String userId) {
        MutableLiveData<List<AquariumData>> liveData = new MutableLiveData<>();

        if (isInvalid(userId)) {
            Log.e(TAG, "Cannot get aquarium data for an invalid user ID.");
            liveData.setValue(Collections.emptyList());
            return liveData;
        }

        Log.i(TAG, "Getting aquarium data for userId: " + userId);
        firestoreDatabase.collection(USERS_COLLECTION)
                .document(userId)
                .collection(AQUARIUM_DATA_COLLECTION)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening to aquarium data", e);
                        return;
                    }

                    if (snapshot != null && !snapshot.isEmpty()) {
                        List<AquariumData> dataList = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            AquariumData data = doc.toObject(AquariumData.class);
                            if (data != null) {
                                dataList.add(data);
                            }
                        }
                        liveData.setValue(dataList);
                    } else {
                        liveData.setValue(Collections.emptyList());
                    }
                });

        return liveData;
    }

    /**
     * Adds a new aquarium data point to Firestore.
     */
    public void addNewData(String userId, AquariumData data) {
        if (isInvalid(userId)) {
            Log.e(TAG, "userId is null or empty, cannot add new data.");
            return;
        }

        Log.i(TAG, "Adding new aquarium data for userId: " + userId);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("temperature", data.getTemperature());
        dataMap.put("ph", data.getPh());
        dataMap.put("oxygen", data.getOxygen());
        dataMap.put("waterLevel", data.getWaterLevel());
        dataMap.put("date", data.getDate());
        // Use a server-side timestamp to ensure chronological order and consistency
        dataMap.put("timestamp", FieldValue.serverTimestamp());

        firestoreDatabase.collection(USERS_COLLECTION)
                .document(userId)
                .collection(AQUARIUM_DATA_COLLECTION)
                .add(dataMap)
                .addOnSuccessListener(documentRef ->
                        Log.d(TAG, "Aquarium data added successfully: " + documentRef.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error adding aquarium data", e));
    }

    // --- User Settings Methods ---

    /**
     * Fetches a user's settings document from Firestore. This is the method
     * that was missing. It listens for real-time updates.
     *
     * @param userId The ID of the user whose settings to fetch.
     * @return A LiveData object that will contain the UserSettings.
     */
    public LiveData<UserSettings> getUserSettings(String userId) {
        MutableLiveData<UserSettings> settingsLiveData = new MutableLiveData<>();

        if (isInvalid(userId)) {
            Log.w(TAG, "getUserSettings called with invalid userId. Returning default settings.");
            settingsLiveData.setValue(new UserSettings()); // Return default settings on error
            return settingsLiveData;
        }

        DocumentReference settingsDocRef = firestoreDatabase
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(SETTINGS_COLLECTION)
                .document(SETTINGS_DOCUMENT_NAME);

        settingsDocRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Log.e(TAG, "Listen failed for user settings.", error);
                settingsLiveData.setValue(new UserSettings()); // Return default on error
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                UserSettings settings = snapshot.toObject(UserSettings.class);
                settingsLiveData.setValue(settings);
                Log.d(TAG, "User settings loaded successfully from Firestore.");
            } else {
                // If no settings document exists yet for the user, provide a default one.
                Log.d(TAG, "No settings document found for user, providing defaults.");
                settingsLiveData.setValue(new UserSettings());
            }
        });

        return settingsLiveData;
    }

    /**
     * Saves a user's settings to a specific document in Firestore.
     * The .set() method creates the document if it doesn't exist or overwrites it if it does.
     *
     * @param userId The user's ID.
     * @param settingsToSave The UserSettings object to save.
     * @return A Task that resolves upon completion.
     */
    public Task<Void> saveUserSettings(String userId, UserSettings settingsToSave) {
        if (isInvalid(userId)) {
            return Tasks.forException(new IllegalArgumentException("User ID cannot be null or empty."));
        }

        DocumentReference settingsDocRef = firestoreDatabase
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(SETTINGS_COLLECTION)
                .document(SETTINGS_DOCUMENT_NAME);

        // The .set() method is perfect for settings, as it handles both creation and updates.
        return settingsDocRef.set(settingsToSave)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "User settings saved successfully for user: " + userId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error saving user settings for user: " + userId, e));
    }


    /**
     * Helper method to check for invalid IDs to reduce code duplication.
     */
    private boolean isInvalid(String id) {
        return id == null || id.trim().isEmpty();
    }
}
