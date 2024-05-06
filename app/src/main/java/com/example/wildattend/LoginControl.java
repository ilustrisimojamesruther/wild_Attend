package com.example.wildattend;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginControl {

    private LoginListener loginListener;

    public LoginControl(LoginListener loginListener) {
        this.loginListener = loginListener;
    }

    public void loginUser(String email, String password) {
        // Perform Firebase Authentication
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login success
                            FirebaseUser user = task.getResult().getUser();
                            loginListener.onLoginSuccess(user);
                        } else {
                            // Login failed
                            loginListener.onLoginFailure("Incorrect credentials. Please try again.");
                        }
                    }
                });
    }

    // Interface for login event callbacks
    public interface LoginListener {
        void onLoginSuccess(FirebaseUser user);
        void onLoginFailure(String message);
    }
}
