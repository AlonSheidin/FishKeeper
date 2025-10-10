package com.example.smartaquarium.data.viewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.smartaquarium.data.datasource.FirebaseDataSource;
import com.example.smartaquarium.data.model.AquariumData;
import com.example.smartaquarium.utils.enums.EnumConnectionStatus;
import com.example.smartaquarium.utils.interfaces.IConnection;
import com.example.smartaquarium.utils.interfaces.IDataListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AquariumDataViewModel extends ViewModel implements IDataListener {

    private static final String NO_USER_ID = "UserNotLoggedIn";

    private final FirebaseDataSource dataSource = new FirebaseDataSource();
    private final MutableLiveData<AquariumData> latestData = new MutableLiveData<>();
    private final MutableLiveData<List<AquariumData>> history = new MutableLiveData<>();
    private final MutableLiveData<EnumConnectionStatus> connectionStatus = new MutableLiveData<>();

    private String userId;
    private final List<AquariumData> offlineDataCache = new ArrayList<>();

    public AquariumDataViewModel() {
        loadCurrentUser();
    }

    public LiveData<List<AquariumData>> getHistory() {
        return history;
    }

    public LiveData<AquariumData> getLatestData() {
        return latestData;
    }

    public LiveData<EnumConnectionStatus> getConnectionStatus() {
        return connectionStatus;
    }

    // --- IDataListener Implementation ---
    @Override
    public void onNewData(AquariumData data) {
        Log.i("AquariumViewModel", "onNewData: userId=\"" + userId + "\" data=" + data.toString());
        latestData.postValue(data);

        if (!Objects.equals(userId, NO_USER_ID)) {
            dataSource.addNewData(userId, data);
        } else {
            // Cache data locally until the user logs in.
            offlineDataCache.add(data);
        }
    }

    @Override
    public void onConnectionStatusChanged(EnumConnectionStatus newStatus) {
        // Suggestion 4: Implement the logic. Use postValue() for thread safety.
        connectionStatus.postValue(newStatus);
    }

    // --- Public Methods ---
    public void setAsListenerTo(IConnection connection) {
        connection.addListener(this);
    }

    /**
     * Suggestion 6: Renamed to follow Java conventions.
     * Called when the user logs in. This method fetches the user's history
     * and uploads any data that was cached while offline.
     */
    public void onUserLogin() {
        loadCurrentUser();

        // Suggestion 2: Safer handling of cached data.
        if (!offlineDataCache.isEmpty()) {
            for (AquariumData data : offlineDataCache) {
                dataSource.addNewData(userId, data);
            }
            offlineDataCache.clear();
        }
    }

    /**
     * Centralized method to load user data. This avoids duplicate code
     * and ensures history LiveData is always handled.
     */
    private void loadCurrentUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            this.userId = currentUser.getUid();

            LiveData<List<AquariumData>> sourceHistory = dataSource.getUserAquariumData(userId);

            //  Observe the data source's LiveData.
            //  When the data comes back from Firebase, this block will execute.
            sourceHistory.observeForever(new androidx.lifecycle.Observer<>() {
                @Override
                public void onChanged(List<AquariumData> newHistoryData) {
                    // Update the ViewModel's own history LiveData with the fetched data.
                    history.setValue(newHistoryData);

                    // IMPORTANT: Remove the observer to prevent memory leaks and
                    // to stop observing the old user's data if a new user logs in.
                    sourceHistory.removeObserver(this);
                }
            });

        } else {
            // If there is no user, set the user ID and clear the history.
            this.userId = NO_USER_ID;
            history.setValue(new ArrayList<>());
        }
    }

}

