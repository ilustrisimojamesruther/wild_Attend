package com.example.wildattend;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private LoginUI loginUI;
    private LoginControl loginControl;
    private EditText emailEditText, passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_ui); // Replace with your layout name

        // Find the views in your layout
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.button);

        loginUI = new LoginUI();
        loginUI.setViews(emailEditText, passwordEditText, loginButton);

        loginControl = new LoginControl(new LoginControl.LoginListener() {
            @Override
            public void onLoginSuccess(FirebaseUser user) {
                // Handle successful login
                String email = user.getEmail();
                Toast.makeText(MainActivity.this, "Login successful for: " + email, Toast.LENGTH_SHORT).show();

                // Redirect to StudentDashboard activity upon successful login
                Intent intent = new Intent(MainActivity.this, StudentDashboard.class);
                startActivity(intent);
                finish();// Finish MainActivity to prevent going back when pressing back button

                // You can perform additional operations here if needed
            }

            @Override
            public void onLoginFailure(String message) {
                // Handle login failure
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loginUI.setLoginClickListener(loginControl);
    }
}