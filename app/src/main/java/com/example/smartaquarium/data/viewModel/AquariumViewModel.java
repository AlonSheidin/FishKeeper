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

public class AquariumViewModel extends ViewModel implements IDataListener {

    private final FirebaseDataSource dataSource = new FirebaseDataSource();
    private final MutableLiveData<AquariumData> latestData = new MutableLiveData<>();
    private LiveData<List<AquariumData>> history;
    private final MutableLiveData<EnumConnectionStatus> connectionStatus = new MutableLiveData<>();

    private String userId;

    private final List<AquariumData> dataList = new ArrayList<>();

    public AquariumViewModel() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = (currentUser != null) ? currentUser.getUid() : "UserNotLoggedIn";

        if(currentUser != null)
            history =  dataSource.getUserAquariumData(userId);

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

    @Override
    public void onNewData(AquariumData data) {
        Log.i("AquariumViewModel", "onNewData: userId=\""+userId+"\" data="+data.toString());
        latestData.setValue(data);
        if(!Objects.equals(userId, "UserNotLoggedIn"))
            dataSource.addNewData(userId, data);

        else{
            dataList.add(data);
        }
    }

    @Override
    public void onConnectionStatusChanged(EnumConnectionStatus connectionStatus) {
        // Update connection status
    }

    public void setAsListenerTo(IConnection connection) {
        connection.addListener(this);
    }

    public void OnUserLogin() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if( currentUser != null)
            userId = currentUser.getUid();

        history = dataSource.getUserAquariumData(userId);
        if(!dataList.isEmpty() && history.getValue() != null)
            history.getValue().addAll(dataList);
        dataList.clear();

    }
}

