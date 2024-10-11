package com.example.wildattend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;

public class MainActivity extends AppCompatActivity {

    private LoginUI loginUI;
    private LoginControl loginControl;
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private CheckBox checkBox;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration roleListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_ui); // Replace with your layout name

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find the views in your layout
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.button);
        checkBox = findViewById(R.id.checkBox);

        loginUI = new LoginUI();
        loginUI.setViews(emailEditText, passwordEditText, loginButton, checkBox);

        loginControl = new LoginControl(new LoginControl.LoginListener() {
            @Override
            public void onLoginSuccess(FirebaseUser user) {
                // This method is not used here as user data should be handled in onUserRoleReceived
            }

            @Override
            public void onLoginFailure(String message) {
                // Handle login failure
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUserRoleReceived(String role) {
                handleUserRole(role);
            }
        });

        // Check if a user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchUserRole(currentUser); // If logged in, fetch the user's role and redirect
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        loginUI.setLoginClickListener(loginControl);
    }

    // Method to fetch the user role from Firestore
    private void fetchUserRole(FirebaseUser user) {
        DocumentReference userRef = db.collection("users").document(user.getUid());

        // Fetch the user's role from Firestore and redirect accordingly
        roleListener = userRef.addSnapshotListener(MetadataChanges.INCLUDE, (snapshot, error) -> {
            if (error != null) {
                Toast.makeText(MainActivity.this, "Failed to fetch user role", Toast.LENGTH_SHORT).show();
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                String role = snapshot.getString("role");
                handleUserRole(role); // Handle redirection based on the role
            } else {
                Toast.makeText(MainActivity.this, "Failed to retrieve user role", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to handle user redirection based on their role
    private void handleUserRole(String role) {
        if (role != null) {
            if ("Student".equals(role)) {
                // Allow login for students
                Intent intent = new Intent(MainActivity.this, StudentDashboard.class);
                startActivity(intent);
                finish(); // Finish MainActivity to prevent going back when pressing back button
            } else if ("Faculty".equals(role)) {
                // Allow login for faculty
                Intent intent = new Intent(MainActivity.this, FacultyDashboard.class);
                startActivity(intent);
                finish(); // Finish MainActivity to prevent going back when pressing back button
            } else {
                // Disallow login for other roles
                Toast.makeText(MainActivity.this, "You are not authorized to log in.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle scenario where role is not retrieved
            Toast.makeText(MainActivity.this, "Failed to fetch user role", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (roleListener != null) {
            roleListener.remove(); // Remove Firestore listener to prevent memory leaks
        }
    }
}
