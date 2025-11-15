package com.example.smartaquarium.data.datasource;

import android.util.Log;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smartaquarium.data.model.AquariumData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreDataSource {

    private final FirebaseFirestore db;


    public FirestoreDataSource() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Retrieves aquarium data for a specific user from Firestore.
     */
    public LiveData<List<AquariumData>> getUserAquariumData(String userId) {
        MutableLiveData<List<AquariumData>> liveData = new MutableLiveData<>();

        Log.i("FirebaseDataSource", "getUserAquariumData: userId=\""+userId+"\"");
        db.collection("users")
                .document(userId)
                .collection("aquariumData")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e("FirebaseDataSource", "Error listening to data", e);
                        return;
                    }


                    if (snapshot != null && !snapshot.isEmpty()) {
                        List<AquariumData> dataList = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            AquariumData data = doc.toObject(AquariumData.class);
                            dataList.add(data);
                        }
                        liveData.setValue(dataList);
                    } else {
                        liveData.setValue(Collections.emptyList());
                    }
                });

        return liveData;
    }

    /**
     * Adds new data to Firestore.
     */
    public void addNewData(String userId, AquariumData data) {
        if (userId == null || userId.isEmpty()) {
            Log.e("FirebaseDataSource", "userId is null or empty");
            return;
        }
        
        Log.i("FirebaseDataSource", "addNewData: userId=\""+userId+"\" data="+data.toString());

        Map<String, Object> dataMap = new HashMap<>();

        dataMap.put("temperature", data.getTemperature());
        dataMap.put("ph", data.getPh());
        dataMap.put("date", data.getDate());
        dataMap.put("oxygen", data.getOxygen());
        dataMap.put("waterLevel", data.getWaterLevel());
        // Use server timestamp to ensure consistency
        dataMap.put("timestamp", FieldValue.serverTimestamp());

        db.collection("users")
                .document(userId)
                .collection("aquariumData")
                .add(dataMap)
                .addOnSuccessListener(documentRef ->
                        Log.d("FirebaseDataSource", "Data added successfully: " + documentRef.getId()))
                .addOnFailureListener(e ->
                        Log.e("FirebaseDataSource", "Error adding data", e));
    }


}

