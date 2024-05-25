package com.example.wildattend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.graphics.Color;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class StudentScheduleTimeIn extends Fragment {

    private AppCompatButton presentButton; // Renamed from timeInButton
    private static final String ARG_PARAM1 = "className";
    private static final String ARG_PARAM2 = "time";
    private static final String TAG = "StudentScheduleClass";

    private String mParam1;
    private String mParam2;

    private TextView studentNameTextView;
    private TextView idNumberTextView;
    private TextView classNameTextView;
    private TextView timeDisplay;
    private ImageView profile_image;

    public StudentScheduleTimeIn() {
        // Required empty public constructor
    }

    public static StudentScheduleTimeIn newInstance(String className, String time) {
        StudentScheduleTimeIn fragment = new StudentScheduleTimeIn();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, className);
        args.putString(ARG_PARAM2, time);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_student_schedule_timein, container, false);
        presentButton = rootView.findViewById(R.id.timeInButton); // Changed from timeInButton

        profile_image = rootView.findViewById(R.id.profile_image_faculty);
        studentNameTextView = rootView.findViewById(R.id.studentName);
        idNumberTextView = rootView.findViewById(R.id.idNumber);
        classNameTextView = rootView.findViewById(R.id.className);
        timeDisplay = rootView.findViewById(R.id.timeDisplay);

        fetchUserInformation();

        if (getArguments() != null) {
            String className = getArguments().getString(ARG_PARAM1);
            String time = getArguments().getString(ARG_PARAM2);
            classNameTextView.setText(className);
            timeDisplay.setText(time);
        }

        // Back Button
        ImageButton backButton = rootView.findViewById(R.id.backButtonClass);
        backButton.setOnClickListener(v -> getActivity().onBackPressed()); // Go back to the previous fragment/activity

        // Time In Button Click Listener
        presentButton.setOnClickListener(v -> timeIn());

        return rootView;
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
                            String imageUrl = documentSnapshot.getString("img");

                            studentNameTextView.setText(firstName + " " + lastName);
                            idNumberTextView.setText(idNumber);

                            new LoadImageTask(profile_image).execute(imageUrl);
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching user document", e));
        } else {
            Log.e(TAG, "User is not authenticated");
        }
    }

    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private WeakReference<ImageView> imageViewReference;

        public LoadImageTask(ImageView imageView) {
            imageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String urlString = params[0];
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    private void timeIn() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String className = mParam1; // Assuming you have the class name available
            String time = mParam2; // Assuming you have the class time available
            String message = "I'm here on time"; // Default message, you can customize this
            Date timestamp = new Date(); // Get current timestamp

            AttendanceRecord attendanceRecord = new AttendanceRecord(userId, message, "On-Time", timestamp, null, className);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("attendRecord")
                    .document(userId)
                    .set(attendanceRecord)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Time in recorded successfully!");
                        // Display a message or perform any additional action upon successful time-in
                        showTimeInPopup(); // Display the popup
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error recording time in", e);
                        // Handle error
                    });
        } else {
            Log.e(TAG, "User not authenticated");
            // Handle authentication error
        }
    }

    private void showTimeInPopup() {
        // Inflate the popup_timein.xml layout
        View popupView = getLayoutInflater().inflate(R.layout.popup_timein, null);

        // Create the popup window
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(20);
        }

        // Set a background drawable for the popup window
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set a focusable property to make the popup window focusable
        popupWindow.setFocusable(true);

        // Show the popup window
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // Handle the "Continue" button click
        AppCompatButton continueButton = popupView.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(v -> {
            popupWindow.dismiss(); // Dismiss the popup

            // Navigate to the StudentScheduleTimeout fragment
            Fragment timeoutFragment = StudentScheduleTimeout.newInstance(mParam1, mParam2);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, timeoutFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}
