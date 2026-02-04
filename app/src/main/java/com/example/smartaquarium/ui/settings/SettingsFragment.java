package com.example.smartaquarium.ui.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartaquarium.R;
import com.example.smartaquarium.data.model.UserSettings;
import com.example.smartaquarium.service.UserSettingsService; // <-- Import the new service
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Fragment for managing user settings for all aquarium parameters.
 * It uses UserSettingsService to load and save data, keeping the UI logic clean.
 */
public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    // --- UI Components ---
    private TextInputEditText minTemperatureEditText;
    private TextInputEditText maxTemperatureEditText;
    private TextInputEditText minPhEditText;
    private TextInputEditText maxPhEditText;
    private TextInputEditText minOxygenEditText;
    private TextInputEditText maxOxygenEditText;
    private TextInputEditText minWaterLevelEditText;
    private TextInputEditText maxWaterLevelEditText;
    private TimePicker doNotDisturbStartTimePicker;
    private TimePicker doNotDisturbEndTimePicker;
    private Button saveSettingsButton;
    private Button logoutButton;

    // --- Logic Service ---
    private UserSettingsService userSettingsService; // <-- The only dependency we need
    private FirebaseAuth firebaseAuth; // <-- Declare FirebaseAuth

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_settings, container, false);
        initializeFragment(fragmentView);
        return fragmentView;
    }

    /**
     * Main initialization function for the fragment.
     */
    private void initializeFragment(View view) {
        // Init the service
        this.userSettingsService = new UserSettingsService();
        this.firebaseAuth = FirebaseAuth.getInstance(); // <-- Initialize FirebaseAuth
        // Init UI components and listeners
        initializeUiComponents(view);
        setupOnClickListener();

        // Trigger the data load
        loadAndObserveUserSettings();
    }

    /**
     * Binds all the UI component variables to their views in the layout.
     */
    private void initializeUiComponents(View view) {
        minTemperatureEditText = view.findViewById(R.id.edit_text_min_temperature);
        maxTemperatureEditText = view.findViewById(R.id.edit_text_max_temperature);
        minPhEditText = view.findViewById(R.id.edit_text_min_ph);
        maxPhEditText = view.findViewById(R.id.edit_text_max_ph);
        minOxygenEditText = view.findViewById(R.id.edit_text_min_oxygen);
        maxOxygenEditText = view.findViewById(R.id.edit_text_max_oxygen);
        minWaterLevelEditText = view.findViewById(R.id.edit_text_min_water_level);
        maxWaterLevelEditText = view.findViewById(R.id.edit_text_max_water_level);

        doNotDisturbStartTimePicker = view.findViewById(R.id.time_picker_dnd_start);
        doNotDisturbEndTimePicker = view.findViewById(R.id.time_picker_dnd_end);
        saveSettingsButton = view.findViewById(R.id.button_save_settings);
        logoutButton = view.findViewById(R.id.button_logout);

        doNotDisturbStartTimePicker.setIs24HourView(true);
        doNotDisturbEndTimePicker.setIs24HourView(true);
    }


    /**
     * Signs the current user out of Firebase and returns to the Login screen.
     */
    private void executeLogout() {
        Log.d(TAG, "Logging out user.");

        // 1. Sign out from Firebase
        firebaseAuth.signOut();

        // 2. Hide the Bottom Navigation Bar (since we are going back to Login)
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }

        // 3. Move the user back to LoginFragment
        // We replace the current fragment in the host container with a new LoginFragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new com.example.smartaquarium.ui.login.LoginFragment())
                .commit();

        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void setupOnClickListener() {
        saveSettingsButton.setOnClickListener(v -> showSaveConfirmationDialog());
        logoutButton.setOnClickListener(v -> showLogoutConfirmationDialog()); // <-- 3. Set the listener
    }

    /**
     * Subscribes to settings data from the service and updates the UI upon receiving it.
     */
    private void loadAndObserveUserSettings() {
        Log.d(TAG, "Asking UserSettingsService for current user's settings.");
        userSettingsService.getSettingsForCurrentUser().observe(getViewLifecycleOwner(), settings -> {
            if (settings != null) {
                Log.d(TAG, "Settings data received. Updating UI.");
                updateUiWithSettings(settings);
            } else {
                Log.w(TAG, "Received null settings, UI will be disabled.");
                disableUiComponents();
            }
        });
    }

    /**
     * Gathers data from all UI fields, populates a UserSettings object,
     * and asks the service to save it.
     */
    private void executeSaveSettings() {
        UserSettings settingsToSave = new UserSettings();
        try {
            // Gather all values from the UI
            settingsToSave.setMinTemperature(Double.parseDouble(minTemperatureEditText.getText().toString()));
            settingsToSave.setMaxTemperature(Double.parseDouble(maxTemperatureEditText.getText().toString()));
            settingsToSave.setMinPh(Double.parseDouble(minPhEditText.getText().toString()));
            settingsToSave.setMaxPh(Double.parseDouble(maxPhEditText.getText().toString()));
            settingsToSave.setMinOxygen(Double.parseDouble(minOxygenEditText.getText().toString()));
            settingsToSave.setMaxOxygen(Double.parseDouble(maxOxygenEditText.getText().toString()));
            settingsToSave.setMinWaterLevel(Double.parseDouble(minWaterLevelEditText.getText().toString()));
            settingsToSave.setMaxWaterLevel(Double.parseDouble(maxWaterLevelEditText.getText().toString()));

            // Gather DND hours
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                settingsToSave.setDoNotDisturbStartHour(doNotDisturbStartTimePicker.getHour());
                settingsToSave.setDoNotDisturbEndHour(doNotDisturbEndTimePicker.getHour());
            } else {
                settingsToSave.setDoNotDisturbStartHour(doNotDisturbStartTimePicker.getCurrentHour());
                settingsToSave.setDoNotDisturbEndHour(doNotDisturbEndTimePicker.getCurrentHour());
            }

            // UI feedback and service call
            Toast.makeText(getContext(), "Saving...", Toast.LENGTH_SHORT).show();
            saveSettingsButton.setEnabled(false);

            // Use the service to save the settings
            userSettingsService.saveSettingsForCurrentUser(settingsToSave)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Settings saved successfully!", Toast.LENGTH_SHORT).show();
                        saveSettingsButton.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save settings", e);
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        saveSettingsButton.setEnabled(true);
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter a valid number in all fields.", Toast.LENGTH_LONG).show();
            saveSettingsButton.setEnabled(true);
        }
    }

    /**
     * Populates all UI fields with the data from the loaded UserSettings object.
     */
    private void updateUiWithSettings(UserSettings settings) {
        minTemperatureEditText.setText(String.valueOf(settings.getMinTemperature()));
        maxTemperatureEditText.setText(String.valueOf(settings.getMaxTemperature()));
        minPhEditText.setText(String.valueOf(settings.getMinPh()));
        maxPhEditText.setText(String.valueOf(settings.getMaxPh()));
        minOxygenEditText.setText(String.valueOf(settings.getMinOxygen()));
        maxOxygenEditText.setText(String.valueOf(settings.getMaxOxygen()));
        minWaterLevelEditText.setText(String.valueOf(settings.getMinWaterLevel()));
        maxWaterLevelEditText.setText(String.valueOf(settings.getMaxWaterLevel()));

        saveSettingsButton.setEnabled(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            doNotDisturbStartTimePicker.setHour(settings.getDoNotDisturbStartHour());
            doNotDisturbStartTimePicker.setMinute(0);
            doNotDisturbEndTimePicker.setHour(settings.getDoNotDisturbEndHour());
            doNotDisturbEndTimePicker.setMinute(0);
        } else {
            doNotDisturbStartTimePicker.setCurrentHour(settings.getDoNotDisturbStartHour());
            doNotDisturbStartTimePicker.setCurrentMinute(0);
            doNotDisturbEndTimePicker.setCurrentHour(settings.getDoNotDisturbEndHour());
            doNotDisturbEndTimePicker.setCurrentMinute(0);
        }
    }

    // --- Helper methods for Dialog and UI state ---

    private void showSaveConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Save")
                .setMessage("Are you sure you want to save these changes?")
                .setPositiveButton("Save", (dialog, which) -> executeSaveSettings())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * Shows a confirmation dialog before logging the user out.
     */
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (dialog, which) -> executeLogout()) // Call logout on confirm
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.ic_logout)
                .create()
                .show();
    }

    private void disableUiComponents() {
        minTemperatureEditText.setEnabled(false);
        maxTemperatureEditText.setEnabled(false);
        minPhEditText.setEnabled(false);
        maxPhEditText.setEnabled(false);
        minOxygenEditText.setEnabled(false);
        maxOxygenEditText.setEnabled(false);
        minWaterLevelEditText.setEnabled(false);
        maxWaterLevelEditText.setEnabled(false);
        doNotDisturbStartTimePicker.setEnabled(false);
        doNotDisturbEndTimePicker.setEnabled(false);
        saveSettingsButton.setEnabled(false);
        logoutButton.setEnabled(false);
        saveSettingsButton.setText("Log in to change settings");
    }
}
