package com.example.wildattend;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginUI {

    private EditText emailTxt;
    private EditText passwordTxt;
    private Button loginButton;

    public LoginUI() {
        // No need to initialize views here
    }

    public void setViews(EditText emailTxt, EditText passwordTxt, Button loginButton) {
        this.emailTxt = emailTxt;
        this.passwordTxt = passwordTxt;
        this.loginButton = loginButton;
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
                // Pass user credentials to login control for authentication
                loginControl.loginUser(email, password);
            }
        });
    }
}
