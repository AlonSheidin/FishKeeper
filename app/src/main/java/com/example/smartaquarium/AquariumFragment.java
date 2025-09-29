package com.example.smartaquarium;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AquariumFragment extends Fragment {

    private TextView tvTemperature;
    private TextView tvPH;
    private TextView tvWaterLevel;
    private TextView tvLastUpdate;
    private Button btnFeedFish;
    private Button btnToggleLight;

    private DatabaseReference database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_aquarium, container, false);

        // Bind views
        tvTemperature = view.findViewById(R.id.tv_temperature);
        tvPH = view.findViewById(R.id.tv_ph);
        tvWaterLevel = view.findViewById(R.id.tv_water_level);
        tvLastUpdate = view.findViewById(R.id.tv_last_update);
        btnFeedFish = view.findViewById(R.id.btn_feed_fish);
        btnToggleLight = view.findViewById(R.id.btn_toggle_light);

        // Firebase
        // TODO implement firebase

        //database = FirebaseDatabase.getInstance().getReference("aquariumData");
        //loadAquariumData();

        // Button actions
        btnFeedFish.setOnClickListener( v -> {});

        btnToggleLight.setOnClickListener(v -> {});

        //TODO remove set data
        tvTemperature.setText("Temperature: 25 °C");
        tvPH.setText("pH Level: 7.2");
        tvWaterLevel.setText("Water Level: Full");
        tvLastUpdate.setText("Last updated: Just now");

        return view;
    }

    /*private void loadAquariumData() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvTemperature.setText("Temperature: " + snapshot.child("temperature").getValue() + " °C");
                tvPH.setText("pH Level: " + snapshot.child("ph").getValue());
                tvWaterLevel.setText("Water Level: " + snapshot.child("waterLevel").getValue());
                tvLastUpdate.setText("Last updated: " + snapshot.child("lastUpdate").getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvTemperature.setText("Temperature: --");
                tvPH.setText("pH Level: --");
                tvWaterLevel.setText("Water Level: --");
                tvLastUpdate.setText("Last updated: --");
            }
        });
    }*/
}
