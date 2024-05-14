package com.example.wildattend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StudentChangePassword extends Fragment {

    private EditText currentPasswordEditText, newPasswordEditText, reEnterNewPasswordEditText;
    private Button updatePasswordButton;

    public StudentChangePassword() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_changepassword, container, false);

        currentPasswordEditText = view.findViewById(R.id.CurrentPassword);
        newPasswordEditText = view.findViewById(R.id.NewPassword);
        reEnterNewPasswordEditText = view.findViewById(R.id.ReEnterNewPassword);
        updatePasswordButton = view.findViewById(R.id.updatePassword);

        updatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword();
            }
        });

        return view;
    }

    private void updatePassword() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String reEnteredPassword = reEnterNewPasswordEditText.getText().toString().trim();

        // Check if new password and re-entered password match
        if (!newPassword.equals(reEnteredPassword)) {
            Toast.makeText(getActivity(), "New password and re-entered password do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Re-authenticate the user with their current password
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // User re-authenticated successfully, proceed with password update
                            user.updatePassword(newPassword)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Password updated successfully
                                            Toast.makeText(getActivity(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Failed to update password
                                            Toast.makeText(getActivity(), "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to re-authenticate user
                            Toast.makeText(getActivity(), "Failed to re-authenticate user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // User is not authenticated
            Toast.makeText(getActivity(), "User is not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}
