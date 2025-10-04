package com.example.smartaquarium.data.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.smartaquarium.data.model.AquariumData;
import com.example.smartaquarium.utils.enums.EnumConnectionStatus;
import com.example.smartaquarium.utils.interfaces.IConnection;
import com.example.smartaquarium.utils.interfaces.IDataListener;

import java.util.ArrayList;
import java.util.List;

public class AquariumViewModel extends ViewModel implements IDataListener {
    private final MutableLiveData<AquariumData> latestData = new MutableLiveData<>();
    private final MutableLiveData<List<AquariumData>> history = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<EnumConnectionStatus> connectionStatus = new MutableLiveData<>();

    private IConnection connection;

    public AquariumViewModel() {

    }

    public void setAsListenerTo(IConnection connection) {
        this.connection = connection;
        connection.addListener(this);
    }

    public LiveData<AquariumData> getLatestData() {
        return latestData;
    }

    public LiveData<EnumConnectionStatus> getConnectionStatus() {
        return connectionStatus;
    }

    public LiveData<List<AquariumData>>  getHistory() {
        return history;
    }



    @Override
    public void onNewData(AquariumData data) {
        //TODO: replace with getting data from firebase
        List<AquariumData> current = history.getValue();
        if (current != null) {
            current.add(data);
            history.setValue(current); // triggers observers
        }
        latestData.setValue(data);
        connectionStatus.setValue(connection.getConnectionStatus());


    }

    @Override
    public void onConnectionStatusChanged(EnumConnectionStatus connectionStatus) {
        // Handle connection status changes if needed
    }
}

