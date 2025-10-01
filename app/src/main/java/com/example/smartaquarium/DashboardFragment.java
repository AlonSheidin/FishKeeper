package com.example.smartaquarium;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DashboardFragment extends Fragment implements IDataListener {

    private TextView tvPhOverview;
    private TextView tvOxygenOverview;
    private TextView tvWaterOverview;
    private TextView tvAlerts;
    private TextView tvTempOverview;

    private View root;

    public DashboardFragment() {

    }

    void InitViews(View root) {
        tvTempOverview = root.findViewById(R.id.tv_temp_overview);
        tvPhOverview = root.findViewById(R.id.tv_ph_overview);
        tvOxygenOverview = root.findViewById(R.id.tv_oxygen_overview);
        tvWaterOverview = root.findViewById(R.id.tv_water_overview);
        tvAlerts = root.findViewById(R.id.tv_alerts);
    }

    private void addListenerToConnection() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getConnection().addListener(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        InitViews(root);

        addListenerToConnection();

        tvTempOverview.setText("Temp: Loading...");
        tvPhOverview.setText("pH: Loading...");
        tvOxygenOverview.setText("Oxygen: Loading...");
        tvWaterOverview.setText("Water: Loading...");
        tvAlerts.setText("Alerts: Loading...");

        return root;
    }

    @Override
    public void onNewData(AquariumData data) {
        tvTempOverview.setText("Temp: "+data.temperature+" °C");
        tvPhOverview.setText("pH: "+data.ph);
        tvOxygenOverview.setText("Oxygen: "+data.oxygen+" mg/L");

    }
}
