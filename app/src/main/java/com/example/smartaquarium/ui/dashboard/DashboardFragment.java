package com.example.smartaquarium.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartaquarium.data.viewModel.AquariumViewModel;
import com.example.smartaquarium.utils.enums.EnumConnectionStatus;
import com.example.smartaquarium.utils.interfaces.IDataListener;
import com.example.smartaquarium.ui.main.MainActivity;
import com.example.smartaquarium.R;
import com.example.smartaquarium.data.model.AquariumData;

public class DashboardFragment extends Fragment  {

    private TextView tvPhOverview;
    private TextView tvOxygenOverview;
    private TextView tvWaterOverview;
    private TextView tvAlerts;
    private TextView tvTempOverview;
    private TextView tvConnectionStatus;

    private View root;

    public DashboardFragment() {}

    void InitViews(View root) {
        tvTempOverview = root.findViewById(R.id.tv_temp_overview);
        tvPhOverview = root.findViewById(R.id.tv_ph_overview);
        tvOxygenOverview = root.findViewById(R.id.tv_oxygen_overview);
        tvWaterOverview = root.findViewById(R.id.tv_water_overview);
        tvAlerts = root.findViewById(R.id.tv_alerts);
        tvConnectionStatus = root.findViewById(R.id.tv_connection_status);
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        InitViews(root);


        tvTempOverview.setText("Temp: Loading...");
        tvPhOverview.setText("pH: Loading...");
        tvOxygenOverview.setText("Oxygen: Loading...");
        tvWaterOverview.setText("Water: Loading...");
        tvAlerts.setText("Alerts: Loading...");
        tvConnectionStatus.setText("Connection: Loading...");

        AquariumViewModel  viewModel = new ViewModelProvider(requireActivity()).get(AquariumViewModel.class);
        viewModel.getLatestData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                tvTempOverview.setText("Temp: "+data.temperature+" °C");
                tvPhOverview.setText("pH: "+data.ph);
                tvOxygenOverview.setText("Oxygen: "+data.oxygen+" mg/L");
                tvWaterOverview.setText("Water: "+data.waterLevel+"%");

            }
        });
        viewModel.getConnectionStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                tvConnectionStatus.setText("Connection: "+ status);
            }
        });

        return root;



    }




}
