package com.example.wildattend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class FacultyChangePassword extends Fragment {

    private ImageView profileImageView;
    private TextView facultyNameTextView;

    private EditText fcurrentPasswordEditText, fnewPasswordEditText, freEnterNewPasswordEditText;
    private Button fupdatePasswordButton;

    public FacultyChangePassword() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faculty_change_password, container, false);

        fcurrentPasswordEditText = view.findViewById(R.id.CurrentPasswordf);
        fnewPasswordEditText = view.findViewById(R.id.fctyNewPassword);
        freEnterNewPasswordEditText = view.findViewById(R.id.ReEnterNewPasswordf);
        fupdatePasswordButton = view.findViewById(R.id.updatePasswordf);

        fupdatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileImageView = view.findViewById(R.id.profilepicture);
        facultyNameTextView = view.findViewById(R.id.facultyName);
        ImageButton backButton = view.findViewById(R.id.backButtonChangePassword);

        fetchUserInformation();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous screen (assuming it's an activity)
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });
    }


    private void fetchUserInformation() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            String imageUrl = documentSnapshot.getString("img");

                            facultyNameTextView.setText(firstName + " " + lastName);

                            new FacultyChangePassword.LoadImageTask(profileImageView).execute(imageUrl);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FacultyProfile", "Error fetching user document", e);
                    });
        } else {
            Log.e("FacultyProfile", "User is not authenticated");
        }
    }

    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewWeakReference;

        public LoadImageTask(ImageView imageView) {
            this.imageViewWeakReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String imageUrl = strings[0];
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                Log.e("LoadImageTask", "Error loading image from URL", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewWeakReference != null && bitmap != null) {
                ImageView imageView = imageViewWeakReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    private void updatePassword() {
        String currentPassword = fcurrentPasswordEditText.getText().toString().trim();
        String newPassword = fnewPasswordEditText.getText().toString().trim();
        String reEnteredPassword = freEnterNewPasswordEditText.getText().toString().trim();

        // Check if any field is empty
        if (currentPassword.isEmpty() || newPassword.isEmpty() || reEnteredPassword.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

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
