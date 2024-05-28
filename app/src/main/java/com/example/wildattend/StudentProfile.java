package com.example.wildattend;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class StudentProfile extends Fragment {

    private ImageView profileImageView;
    private TextView studentNameTextView;
    private TextView idNumberTextView;
    private TextView departmentTextView;
    private TextView emailTextView;

    public StudentProfile() {
        // Required empty public constructor
    }

    public static StudentProfile newInstance() {
        return new StudentProfile();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_student_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileImageView = view.findViewById(R.id.profilepicture);
        studentNameTextView = view.findViewById(R.id.studentname);
        idNumberTextView = view.findViewById(R.id.id_number);
        departmentTextView = view.findViewById(R.id.department);
        emailTextView = view.findViewById(R.id.email);

        Button logoutButton = view.findViewById(R.id.facultyExport);
        Button changePasswordButton = view.findViewById(R.id.changepassword);
        Button studentOverallAttendanceButton = view.findViewById(R.id.facultyoverallattendance);

        logoutButton.setOnClickListener(v -> showLogoutConfirmationDialog());

        // Set OnClickListener for change password button
        changePasswordButton.setOnClickListener(v -> navigateToChangePassword());

        // Set OnClickListener for student overall attendance button
        studentOverallAttendanceButton.setOnClickListener(v -> navigateToStudentOverallAttendance());

        fetchUserInformation();
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
                            String idNumber = documentSnapshot.getString("idNum");
                            String department = documentSnapshot.getString("department");
                            String imageUrl = documentSnapshot.getString("img");
                            String email = documentSnapshot.getString("email");

                            studentNameTextView.setText(firstName + " " + lastName);
                            idNumberTextView.setText(idNumber);
                            departmentTextView.setText(department);
                            emailTextView.setText(email);

                            new LoadImageTask(profileImageView).execute(imageUrl);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("StudentProfile", "Error fetching user document", e);
                    });
        } else {
            Log.e("StudentProfile", "User is not authenticated");
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

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logoutUser();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }

    private void navigateToChangePassword() {
        // Create instance of StudentChangePassword fragment
        StudentChangePassword changePasswordFragment = new StudentChangePassword();

        // Navigate to the StudentChangePassword fragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, changePasswordFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void navigateToStudentOverallAttendance() {
        // Create instance of StudentOverAllAttendance fragment
        StudentOverAllAttendance overallAttendanceFragment = new StudentOverAllAttendance();

        // Navigate to the StudentOverAllAttendance fragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, overallAttendanceFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
