package com.example.wildattend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

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
        updatePasswordButton = view.findViewById(R.id.logout);

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

        // Placeholder: Verify the current password
        if (!isCurrentPasswordValid(currentPassword)) {
            Toast.makeText(getActivity(), "Incorrect current password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Password change logic goes here
        // You can implement database operations or call authentication manager methods to update the password
    }

    // Placeholder method to verify the current password
    private boolean isCurrentPasswordValid(String currentPassword) {
        // Implement your logic to verify the current password
        // For example, compare it with the actual current password stored in the database
        // Replace this with your actual logic to validate the current password
        String actualCurrentPassword = "123456"; // Assuming this is your actual current password
        return currentPassword.equals(actualCurrentPassword);
    }
}