package com.example.smartaquarium;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartaquarium.R;

public class DashboardFragment extends Fragment {

    private TextView tvTempOverview, tvPhOverview, tvOxygenOverview, tvWaterOverview, tvAlerts;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvTempOverview = root.findViewById(R.id.tv_temp_overview);
        tvPhOverview = root.findViewById(R.id.tv_ph_overview);
        tvOxygenOverview = root.findViewById(R.id.tv_oxygen_overview);
        tvWaterOverview = root.findViewById(R.id.tv_water_overview);
        tvAlerts = root.findViewById(R.id.tv_alerts);

        // TODO: Load real values from Firebase
        tvTempOverview.setText("Temp: 25 °C");
        tvPhOverview.setText("pH: 7.4");
        tvOxygenOverview.setText("Oxygen: 6.5 mg/L");
        tvWaterOverview.setText("Water: 80%");
        tvAlerts.setText("Alerts: All good ✅");

        return root;
    }
}
