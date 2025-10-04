package com.example.smartaquarium.utils.interfaces;

import com.example.smartaquarium.data.model.AquariumData;
import com.example.smartaquarium.utils.enums.EnumConnectionStatus;

public interface IDataListener {
    void onNewData(AquariumData data);
    void onConnectionStatusChanged(EnumConnectionStatus connectionStatus);
}
