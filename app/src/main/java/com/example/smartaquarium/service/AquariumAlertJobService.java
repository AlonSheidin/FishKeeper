package com.example.smartaquarium.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.example.smartaquarium.data.datasource.FirestoreDataSource;
import com.example.smartaquarium.data.model.AquariumData;
import com.example.smartaquarium.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class AquariumAlertJobService extends JobService {

    private static final String TAG = "AquariumAlertJob";
    private FirestoreDataSource dataSource;
    private NotificationHelper notificationHelper;
    private FirebaseFirestore database;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob: System triggered the background check.");

        init();

        // Start the alert check logic
        checkAquariumLimits(params);

        // Return true because we are performing asynchronous Firestore calls
        return true;
    }

    /**
     * Initialize dependencies for the job execution.
     */
    private void init() {
        if (dataSource == null) {
            dataSource = new FirestoreDataSource();
        }
        if (notificationHelper == null) {
            notificationHelper = new NotificationHelper(this);
        }
        if (database == null) {
            database = FirebaseFirestore.getInstance();
        }
    }

    private void checkAquariumLimits(JobParameters params) {
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId == null) {
            Log.e(TAG, "User not authenticated. Stopping job.");
            jobFinished(params, false);
            return;
        }

        // 1. Fetch User Settings (Thresholds)
        database.collection("users").document(userId)
                .collection("settings").document("userSettings")
                .get()
                .addOnSuccessListener(settingsSnapshot -> {
                    if (!settingsSnapshot.exists()) {
                        Log.w(TAG, "Settings document 'userSettings' does not exist.");
                        jobFinished(params, false);
                        return;
                    }

                    Double maxTemperature = settingsSnapshot.getDouble("maxTemperature");
                    Double minTemperature = settingsSnapshot.getDouble("minTemperature");

                    if (maxTemperature == null || minTemperature == null) {
                        Log.e(TAG, "Temperature thresholds are missing in Firestore.");
                        jobFinished(params, false);
                        return;
                    }

                    // 2. Fetch all aquariums to check them
                    fetchAndCheckAquariums(userId, maxTemperature, minTemperature, params);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch settings: " + e.getMessage());
                    jobFinished(params, true); // Reschedule on failure
                });
    }

    private void fetchAndCheckAquariums(String userId, double maxT, double minT, JobParameters params) {
        database.collection("users").document(userId)
                .collection("aquariums")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> aquariumDocuments = querySnapshot.getDocuments();

                    if (aquariumDocuments.isEmpty()) {
                        jobFinished(params, false);
                        return;
                    }

                    // For each aquarium, get the latest reading
                    for (DocumentSnapshot doc : aquariumDocuments) {
                        processLatestTankData(userId, doc.getId(), maxT, minT);
                    }

                    // We notify the system that the work is queued.
                    // In a simple setup, we finish after the loop,
                    // but the async listeners for each tank will still trigger notifications.
                    jobFinished(params, false);
                });
    }

    private void processLatestTankData(String userId, String aquariumId, double maxT, double minT) {
        database.collection("users").document(userId)
                .collection("aquariums").document(aquariumId)
                .collection("history")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        AquariumData latestReading = querySnapshot.getDocuments().get(0).toObject(AquariumData.class);
                        if (latestReading != null) {
                            validateTemperature(aquariumId, latestReading, maxT, minT);
                        }
                    }
                });
    }

    private void validateTemperature(String tankName, AquariumData data, double max, double min) {
        double currentTemp = data.getTemperature();
        Log.d(TAG, "Validating " + tankName + ": " + currentTemp + "°C (Limits: " + min + "-" + max + ")");

        if (currentTemp > max) {
            notificationHelper.sendAlert("High Temperature Alert!",
                    tankName + " is at " + currentTemp + "°C (Max limit: " + max + "°C)");
        } else if (currentTemp < min) {
            notificationHelper.sendAlert("Low Temperature Alert!",
                    tankName + " is at " + currentTemp + "°C (Min limit: " + min + "°C)");
        }
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job stopped by system.");
        return true; // Reschedule if the job was interrupted
    }
}