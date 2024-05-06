package com.example.wildattend;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LoginUI {

    private EditText emailTxt;
    private EditText passwordTxt;
    private Button loginButton;
    private CheckBox checkBox;

    public LoginUI() {
        // No need to initialize views here
    }

    public void setViews(EditText emailTxt, EditText passwordTxt, Button loginButton, CheckBox checkBox) {
        this.emailTxt = emailTxt;
        this.passwordTxt = passwordTxt;
        this.loginButton = loginButton;
        this.checkBox = checkBox;
    }

    public String getEmailText() {
        return emailTxt.getText().toString();
    }

    public String getPasswordText() {
        return passwordTxt.getText().toString();
    }

    public void setLoginClickListener(final LoginControl loginControl) {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = getEmailText();
                String password = getPasswordText();
                boolean agreedToTerms = checkBox.isChecked();

                // Check if email, password, and checkbox are not empty
                if (!email.isEmpty() && !password.isEmpty() && agreedToTerms) {
                    // Pass user credentials to login control for authentication
                    loginControl.loginUser(email, password);
                } else {
                    // Notify user to fill all fields and check the checkbox
                    Toast.makeText(v.getContext(), "Please fill all fields and agree to the terms", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
