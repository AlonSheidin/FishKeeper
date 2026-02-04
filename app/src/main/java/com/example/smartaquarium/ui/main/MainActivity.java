package com.example.smartaquarium.ui.main;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartaquarium.data.viewModel.aquariumData.AquariumDataViewModel;
import com.example.smartaquarium.service.AquariumAlertJobService;
import com.example.smartaquarium.service.DummyConnection;
import com.example.smartaquarium.utils.interfaces.IConnection;
import com.example.smartaquarium.ui.login.LoginFragment;
import com.example.smartaquarium.R;
import com.example.smartaquarium.ui.settings.SettingsFragment;
import com.example.smartaquarium.ui.analyics.AnalyticsFragment;
import com.example.smartaquarium.ui.aquarium.AquariumFragment;
import com.example.smartaquarium.ui.dashboard.DashboardFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private FirebaseAuth auth;
    public IConnection connection;
    private static AquariumDataViewModel aquariumDataViewModel;

    void init()
    {
        bottomNav = findViewById(R.id.bottom_navigation);
        auth = FirebaseAuth.getInstance();
        // TODO switch to real connection
        connection = new DummyConnection();

        aquariumDataViewModel = new ViewModelProvider(this).get(AquariumDataViewModel.class);
        aquariumDataViewModel.setAsListenerTo(connection);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
        initJobScheduler();
    }
    public void initJobScheduler() {
        // Use a clear, unique ID for your aquarium job
        int AQUARIUM_JOB_ID = 888;

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        // Clear any old stuck jobs with the same ID
        jobScheduler.cancel(AQUARIUM_JOB_ID);

        ComponentName componentName = new ComponentName(this, AquariumAlertJobService.class);

        JobInfo info = new JobInfo.Builder(AQUARIUM_JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // Works on Wifi/Data
                .setPersisted(true) // Keeps job after phone restart
                .setPeriodic(60000) // 15 mins (Android minimum)
                .build();

        int result = jobScheduler.schedule(info);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.d("AquariumJob", "Job Scheduled Successfully with ID: " + AQUARIUM_JOB_ID);
        } else {
            Log.e("AquariumJob", "Job Scheduling Failed!");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        init();
        FirebaseUser currentUser = auth.getCurrentUser();

        setContentView(R.layout.activity_main);
        setupBottomNavigation();


        if (currentUser == null) { //if user not logged in
            findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
            // Show login first
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new LoginFragment())
                    .commit();


        } else {
            findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
            Log.i("Uid1", "onCreate: uid="+currentUser.getUid());
            aquariumDataViewModel.checkUserAuthentication();
            // Show main app
            loadFragment(new DashboardFragment());

        }

    }

    private void setupBottomNavigation() {
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;

            int id = item.getItemId();
            if (id == R.id.navigation_dashboard) {
                selected = new DashboardFragment();
            } else if (id == R.id.navigation_aquarium) {
                selected = new AquariumFragment();
            } else if (id == R.id.navigation_analytics) {
                selected = new AnalyticsFragment();
            } else if (id == R.id.navigation_settings) {
                selected = new SettingsFragment();
            }

            if (selected != null) {
                loadFragment(selected);
            }
            return true;
        });
    }

    private void loadFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }


}
