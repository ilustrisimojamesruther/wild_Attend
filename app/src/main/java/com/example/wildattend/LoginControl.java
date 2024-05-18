package com.example.wildattend;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;


public class LoginControl {

    private LoginListener loginListener;

    public LoginControl(LoginListener loginListener) {
        this.loginListener = loginListener;
    }

    public void loginUser(final String email, final String password) {
        // Perform Firebase Authentication
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login success
                            FirebaseUser user = task.getResult().getUser();
                            // Pass user to fetch role
                            fetchUserRole(user);
                        } else {
                            // Login failed
                            loginListener.onLoginFailure("Incorrect credentials. Please try again.");
                        }
                    }
                });
    }

    private void fetchUserRole(FirebaseUser user) {
        // Fetch user role from Firestore
        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            if (role != null && !role.isEmpty()) {
                                // Inform UI about the role
                                loginListener.onUserRoleReceived(role);
                            } else {
                                loginListener.onLoginFailure("User role not found.");
                            }
                        } else {
                            loginListener.onLoginFailure("User document not found.");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loginListener.onLoginFailure("Failed to fetch user role: " + e.getMessage());
                    }
                });
    }

    // Interface for login event callbacks
    public interface LoginListener {
        void onLoginSuccess(FirebaseUser user);
        void onLoginFailure(String message);
        void onUserRoleReceived(String role);
    }
}

