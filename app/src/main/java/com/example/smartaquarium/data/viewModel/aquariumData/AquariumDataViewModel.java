package com.example.smartaquarium.data.viewModel.aquariumData;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.Transformations; // Make sure this is imported

import com.example.smartaquarium.data.datasource.FirebaseDataSource;
import com.example.smartaquarium.data.model.AquariumData;
import com.example.smartaquarium.utils.enums.EnumConnectionStatus;
import com.example.smartaquarium.utils.interfaces.IConnection;
import com.example.smartaquarium.utils.interfaces.IDataListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A shared ViewModel that acts as the single source of truth for all raw aquarium data.
 * It manages fetching historical data from Firebase and listening for real-time updates
 * from a connection source (like Bluetooth).
 */
public class AquariumDataViewModel extends ViewModel implements IDataListener {

    private static final String NO_USER_ID = "UserNotLoggedIn";

    // --- Dependencies & State ---
    private final FirebaseDataSource firebaseDataSource;

    // LiveData for single, real-time events
    private final MutableLiveData<AquariumData> latestDataPoint = new MutableLiveData<>();
    private final MutableLiveData<EnumConnectionStatus> connectionStatus = new MutableLiveData<>();

    // The TRIGGER for authentication changes.
    private final MutableLiveData<String> authenticatedUserId = new MutableLiveData<>();

    // The final, combined history list that includes both loaded and real-time data.
    private final MutableLiveData<List<AquariumData>> fullHistory = new MutableLiveData<>();
    private final List<AquariumData> offlineDataCache = new ArrayList<>();

    /**
     * Constructor for dependency injection.
     * The init() method is called to set up reactive data streams.
     * You would use a ViewModelFactory to call this constructor.
     */
    public AquariumDataViewModel() {
        this.firebaseDataSource = new FirebaseDataSource();
        init();
    }

    /**
     * Initializes the reactive data observers.
     */
    private void init() {
        // When the user logs in/out, fetch their historical data from Firestore.
        LiveData<List<AquariumData>> firestoreHistory = Transformations.switchMap(authenticatedUserId, userId -> {
            if (userId != null && !userId.equals(NO_USER_ID)) {
                return firebaseDataSource.getUserAquariumData(userId);
            } else {
                // Return a LiveData with an empty list if logged out.
                MutableLiveData<List<AquariumData>> emptyList = new MutableLiveData<>();
                emptyList.setValue(new ArrayList<>());
                return emptyList;
            }
        });

        // Observe the history from Firestore. When it loads, update our main history list.
        firestoreHistory.observeForever(historyList -> {
            if (historyList != null) {
                fullHistory.setValue(historyList);
            }
        });

        checkUserAuthentication(); // Check the user's status on startup
    }

    // --- Getters for the UI/Other ViewModels to Observe ---

    public LiveData<List<AquariumData>> getHistory() {
        return fullHistory;
    }

    public LiveData<AquariumData> getLatestData() {
        return latestDataPoint;
    }

    public LiveData<EnumConnectionStatus> getConnectionStatus() {
        return connectionStatus;
    }

    // --- IDataListener Implementation ---

    @Override
    public void onNewData(AquariumData newData) {
        Log.d("AquariumDataViewModel", "onNewData received: " + newData);

        // 1. Immediately notify observers of the single latest data point.
        latestDataPoint.postValue(newData);

        // 2. Add the new data point to our full history list.
        List<AquariumData> currentList = fullHistory.getValue();
        List<AquariumData> updatedList = new ArrayList<>();
        if (currentList != null) {
            updatedList.addAll(currentList);
        }
        updatedList.add(newData);
        fullHistory.postValue(updatedList); // Update the history with the new item

        // 3. Save the new data to Firebase if the user is logged in.
        String userId = authenticatedUserId.getValue();
        if (userId != null && !userId.equals(NO_USER_ID)) {
            firebaseDataSource.addNewData(userId, newData);
        } else {
            offlineDataCache.add(newData);
        }
    }

    @Override
    public void onConnectionStatusChanged(EnumConnectionStatus newStatus) {
        connectionStatus.postValue(newStatus);
    }

    /**
     * The object that has the Bluetooth connection will call this method
     * to register this ViewModel as a listener for new data.
     */
    public void setAsListenerTo(IConnection connection) {
        connection.addListener(this);
    }

    /**
     * Checks the current Firebase authentication state and updates the trigger LiveData.
     * This should be called whenever a login/logout event might have occurred.
     */
    public void checkUserAuthentication() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentId = (currentUser != null) ? currentUser.getUid() : NO_USER_ID;

        if (!currentId.equals(authenticatedUserId.getValue())) {
            authenticatedUserId.setValue(currentId);


            // If the user just logged in, upload any cached data.
            if (!currentId.equals(NO_USER_ID) && !offlineDataCache.isEmpty()) {
                Log.d("AquariumDataViewModel", "User logged in. Uploading " + offlineDataCache.size() + " cached items.");
                for (AquariumData data : offlineDataCache) {
                    firebaseDataSource.addNewData(currentId, data);
                }
                offlineDataCache.clear();
            }
        }
    }
}
