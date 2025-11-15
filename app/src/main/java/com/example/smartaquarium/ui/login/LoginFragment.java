package com.example.smartaquarium.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartaquarium.R;
import com.example.smartaquarium.data.viewModel.aquariumData.AquariumDataViewModel;
import com.example.smartaquarium.ui.dashboard.DashboardFragment;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A fragment that handles the user login and registration screen.
 */
public class LoginFragment extends Fragment {

    // --- UI Components ---
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private Button signUpButton;

    // --- Authentication Service ---
    private FirebaseAuth authenticationService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize all components and set up listeners
        initializeComponents(fragmentView);
        setupClickListeners();

        return fragmentView;
    }

    /**
     * Initializes all the views and services needed for this fragment.
     * This is called once when the view is created.
     *
     * @param view The root view of the fragment.
     */
    private void initializeComponents(View view) {
        // Services
        authenticationService = FirebaseAuth.getInstance();

        // UI Views
        emailEditText = view.findViewById(R.id.etEmail);
        passwordEditText = view.findViewById(R.id.etPassword);
        signInButton = view.findViewById(R.id.btnSignIn);
        signUpButton = view.findViewById(R.id.btnSignUp);
    }

    /**
     * Sets up the click listeners for the buttons on the screen.
     */
    private void setupClickListeners() {
        signInButton.setOnClickListener(v -> performSignIn());
        signUpButton.setOnClickListener(v -> performSignUp());
    }

    /**
     * Handles the logic for creating a new user account.
     */
    private void performSignUp() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate that input fields are not empty
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Email and Password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Backend Call ---
        authenticationService.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Account created!", Toast.LENGTH_SHORT).show();
                        navigateToDashboardScreen();
                    } else {
                        // Display a detailed error message if creation fails
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Registration failed.";
                        Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Handles the logic for signing in an existing user.
     */
    private void performSignIn() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate that input fields are not empty
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Email and Password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Backend Call ---
        authenticationService.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // On successful login, show the main navigation
                        showBottomNavigationBar();
                        Toast.makeText(getContext(), "Welcome!", Toast.LENGTH_SHORT).show();

                        // Navigate to the main screen
                        navigateToDashboardScreen();
                    } else {
                        // Display a detailed error message if sign-in fails
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Sign-in failed.";
                        Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Makes the bottom navigation bar visible.
     */
    private void showBottomNavigationBar() {
        // It's safer to check for null before accessing a view in the parent activity
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Replaces the current fragment with the DashboardFragment.
     */
    private void navigateToDashboardScreen() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new DashboardFragment())
                .commit();
    }
}
