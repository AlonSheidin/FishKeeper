package com.example.smartaquarium.data.viewModel.aquariumData;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.MediatorLiveData;

import com.example.smartaquarium.R;
import com.example.smartaquarium.data.datasource.FirestoreDataSource;
import com.example.smartaquarium.data.model.Aquarium;
import com.example.smartaquarium.data.model.AquariumData;
import com.example.smartaquarium.data.model.UserSettings;
import com.example.smartaquarium.service.UserSettingsService;
import com.example.smartaquarium.utils.enums.EnumConnectionStatus;
import com.example.smartaquarium.utils.interfaces.IConnection;
import com.example.smartaquarium.utils.interfaces.IDataListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A shared ViewModel that acts as the single source of truth for all raw aquarium data.
 * It manages multiple aquariums for a user and tracks the currently selected one.
 * It also monitors incoming data against user settings to trigger instant local notifications.
 */
public class AquariumDataViewModel extends AndroidViewModel implements IDataListener {

    private static final String NO_USER_ID = "UserNotLoggedIn";
    private static final String CHANNEL_ID = "aquarium_alerts";
    private static final String TAG = "AquariumDataViewModel";

    private final FirestoreDataSource firestoreDataSource;
    private UserSettingsService settingsService;

    // --- State ---
    private final MutableLiveData<String> authenticatedUserId = new MutableLiveData<>();
    private final MutableLiveData<String> selectedAquariumId = new MutableLiveData<>();
    
    // --- Observables ---
    private final LiveData<List<Aquarium>> availableAquariums;
    private final LiveData<UserSettings> userSettings;
    private final MediatorLiveData<List<AquariumData>> fullHistory = new MediatorLiveData<>();
    private final MutableLiveData<AquariumData> latestDataPoint = new MutableLiveData<>();
    private final MutableLiveData<EnumConnectionStatus> connectionStatus = new MutableLiveData<>();

    private final List<AquariumData> offlineDataCache = new ArrayList<>();

    public AquariumDataViewModel(@NonNull Application application) {
        super(application);
        this.firestoreDataSource = new FirestoreDataSource();
        createNotificationChannel();
        
        // 1. When user changes, fetch their list of aquariums
        availableAquariums = Transformations.switchMap(authenticatedUserId, userId -> {
            if (userId != null && !userId.equals(NO_USER_ID)) {
                return firestoreDataSource.getListOfAquariums(userId);
            }
            return new MutableLiveData<>(new ArrayList<>());
        });

        // 2. Fetch user settings
        userSettings = Transformations.switchMap(authenticatedUserId, userId -> {
            if (isValidUser(userId)) {
                return settingsService.getSettingsForCurrentUser();
            }
            return new MutableLiveData<>(new UserSettings()); // Return defaults
        });
        init();
    }
    private boolean isValidUser(String userId) {
        return userId != null && !userId.equals(NO_USER_ID);
    }
    private void init() {
        fullHistory.addSource(authenticatedUserId, userId -> updateHistorySource());
        fullHistory.addSource(selectedAquariumId, aqId -> updateHistorySource());

        checkUserAuthentication();
    }

    private void updateHistorySource() {
        String userId = authenticatedUserId.getValue();
        String aquariumId = selectedAquariumId.getValue();

        if (userId != null && !userId.equals(NO_USER_ID) && aquariumId != null) {
            // In a real app, you'd want to manage sources more carefully to avoid duplicates
            LiveData<List<AquariumData>> newHistory = firestoreDataSource.getAquariumHistory(userId, aquariumId);
            fullHistory.addSource(newHistory, data -> fullHistory.setValue(data));
        } else {
            fullHistory.setValue(new ArrayList<>());
        }
    }

    // --- Getters & Setters ---

    public LiveData<List<Aquarium>> getAvailableAquariums() {
        return availableAquariums;
    }

    public void setSelectedAquarium(String aquariumId) {
        if (aquariumId != null && !aquariumId.equals(selectedAquariumId.getValue())) {
            selectedAquariumId.setValue(aquariumId);
        }
    }

    public LiveData<String> getSelectedAquariumId() {
        return selectedAquariumId;
    }

    public LiveData<List<AquariumData>> getHistory() {
        return fullHistory;
    }

    public LiveData<AquariumData> getLatestData() {
        return latestDataPoint;
    }

    public LiveData<EnumConnectionStatus> getConnectionStatus() {
        return connectionStatus;
    }

    /**
     * Creates a new aquarium for the current user.
     * @param name The display name of the new aquarium.
     */
    public void addNewAquarium(String name) {
        String userId = authenticatedUserId.getValue();

        // Safety check to ensure we have a valid user logged in
        if (userId != null && !userId.equals(NO_USER_ID) && name != null && !name.trim().isEmpty()) {

            // Call createAquarium which creates the document in the 'aquariums' subcollection
            firestoreDataSource.createAquarium(userId, name.trim())
                    .addOnSuccessListener(aVoid -> {
                        Log.d("AquariumVM", "New aquarium created: " + name);
                        // The availableAquariums LiveData will update automatically
                        // because it has a snapshot listener in the DataSource.
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AquariumVM", "Failed to create aquarium", e);
                    });
        }
    }
    // --- IDataListener ---

    @Override
    public void onNewData(AquariumData newData) {
        latestDataPoint.postValue(newData);
        checkLimitsAndNotify(newData);

        String userId = authenticatedUserId.getValue();
        String aquariumId = selectedAquariumId.getValue();

        if (userId != null && !userId.equals(NO_USER_ID) && aquariumId != null) {
            firestoreDataSource.saveDataToAquarium(userId, aquariumId, newData);
        } else {
            offlineDataCache.add(newData);
        }
        checkLimitsAndNotify(newData);
    }

    private void checkLimitsAndNotify(AquariumData incomingData) {
        // Fetch current settings from the observed LiveData
        UserSettings currentSettings = userSettings.getValue();

        if (currentSettings == null) {
            Log.w(TAG, "Cannot check limits: User settings not yet loaded.");
            return;
        }

        StringBuilder alertBuilder = new StringBuilder();

        if (incomingData.getTemperature() < currentSettings.getMinTemperature())
            alertBuilder.append("Temp too low (").append(incomingData.getTemperature()).append("°C). ");

        if (incomingData.getTemperature() > currentSettings.getMaxTemperature())
            alertBuilder.append("Temp too high (").append(incomingData.getTemperature()).append("°C). ");

        if (incomingData.getPh() < currentSettings.getMinPh())
            alertBuilder.append("pH level critically low. ");

        if (incomingData.getOxygen() < currentSettings.getMinOxygen())
            alertBuilder.append("Oxygen level dropped! ");

        if (alertBuilder.length() > 0) {
            sendLocalNotification("Aquarium Alert", alertBuilder.toString());
        }
    }
    private void sendLocalNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplication(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Aquarium Alerts";
            String description = "Notifications for when aquarium levels are out of range";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getApplication().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onConnectionStatusChanged(EnumConnectionStatus newStatus) {
        connectionStatus.postValue(newStatus);
    }

    public void setAsListenerTo(IConnection connection) {
        connection.addListener(this);
    }

    public void checkUserAuthentication() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentId = (currentUser != null) ? currentUser.getUid() : NO_USER_ID;

        if (!currentId.equals(authenticatedUserId.getValue())) {
            authenticatedUserId.setValue(currentId);
        }
    }
}
