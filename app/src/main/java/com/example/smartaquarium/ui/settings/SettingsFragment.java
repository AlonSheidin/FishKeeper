package com.example.smartaquarium.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.smartaquarium.ui.login.LoginFragment;
import com.example.smartaquarium.R;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private SwitchCompat switchNotifications;
    private EditText etLightSchedule;
    private EditText etRefreshInterval;
    private Button btnSaveSettings;
    private Button btnLogout;


    /**
     * Initializes the view components of the fragment. This method is intended to be called
     * after the fragment's view has been created. It finds and assigns the UI elements
     * (switches, text fields, buttons) from the layout file to their corresponding
     * class variables and sets up their initial state and listeners.
     *
     * @param view The root view of the fragment's layout, used to find the child views.
     */
    private void init(View view) {
        switchNotifications = view.findViewById(R.id.switch_notifications);
        etLightSchedule = view.findViewById(R.id.et_light_schedule);
        etRefreshInterval = view.findViewById(R.id.et_refresh_interval);
        btnSaveSettings = view.findViewById(R.id.btn_save_settings);
        btnLogout = view.findViewById(R.id.btn_Logout);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        init(view);

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

    //TODO: Implement saving settings to SharedPreferences or Firebase
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
