package com.example.smartaquarium;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private SwitchCompat switchNotifications;
    private EditText etLightSchedule;
    private EditText etRefreshInterval;
    private Button btnSaveSettings;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        switchNotifications = view.findViewById(R.id.switch_notifications);
        etLightSchedule = view.findViewById(R.id.et_light_schedule);
        etRefreshInterval = view.findViewById(R.id.et_refresh_interval);
        btnSaveSettings = view.findViewById(R.id.btn_save_settings);
        btnLogout = view.findViewById(R.id.btn_Logout);

        btnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            // Hide bottom navigation
            requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.GONE);

            // Replace with login fragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new LoginFragment())
                    .commit();
        });



        return view;
    }

    private void saveSettings() {
        boolean notificationsEnabled = switchNotifications.isChecked();
        String lightSchedule = etLightSchedule.getText().toString();
        String refreshInterval = etRefreshInterval.getText().toString();

        // Here you would normally save to SharedPreferences or Firebase
        Toast.makeText(getContext(),
                "Settings saved:\nNotifications: " + notificationsEnabled +
                        "\nLight Schedule: " + lightSchedule +
                        "\nRefresh Interval: " + refreshInterval,
                Toast.LENGTH_LONG).show();
    }
}
