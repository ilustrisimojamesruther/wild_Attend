package com.example.wildattend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private LoginUI loginUI;
    private LoginControl loginControl;
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_ui); // Replace with your layout name

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
                // Check if the user's role is allowed to log in
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
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loginUI.setLoginClickListener(loginControl);
    }
}