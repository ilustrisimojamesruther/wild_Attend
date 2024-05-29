package com.example.wildattend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StudentScheduleTimeIn extends Fragment {

    private AppCompatButton timeinButton;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "StudentScheduleTimeIn";

    private String mParam1;
    private String mParam2;

    private TextView studentNameTextView;
    private TextView idNumberTextView;
    private ShapeableImageView profile_image;

    public StudentScheduleTimeIn() {
        // Required empty public constructor
    }

    public static StudentScheduleTimeIn newInstance(String param1, String param2) {
        StudentScheduleTimeIn fragment = new StudentScheduleTimeIn();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_schedule_timein, container, false);

        // Set class name and time in the TextViews
        TextView classNameTextView = view.findViewById(R.id.className);
        TextView timeDisplayTextView = view.findViewById(R.id.timeDisplay);

        if (mParam1 != null) {
            classNameTextView.setText(mParam1);
        }
        if (mParam2 != null) {
            timeDisplayTextView.setText(mParam2);
        }

        Button timeInButton = view.findViewById(R.id.timeInButton);
        timeInButton.setOnClickListener(v -> timeIn());

        ImageButton backButton = view.findViewById(R.id.backButtonClass);
        backButton.setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });

        // Initialize views
        profile_image = view.findViewById(R.id.profile_image_student);
        studentNameTextView = view.findViewById(R.id.studentName);
        idNumberTextView = view.findViewById(R.id.idNumber);

        // Fetch and display user information
        fetchUserInformation();

        return view;
    }

    private void fetchUserInformation() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .whereEqualTo("email", userEmail) // Assuming the field in Firestore is "email"
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            String idNumber = documentSnapshot.getString("idNum");
                            String imageUrl = documentSnapshot.getString("img");

                            // Update UI with fetched information
                            studentNameTextView.setText(firstName + " " + lastName);
                            idNumberTextView.setText(idNumber);

                            // Load the image from URL
                            new LoadImageTask(profile_image).execute(imageUrl);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching user document", e);
                    });
        } else {
            Log.e(TAG, "User is not authenticated");
            // Handle the case where the user is not authenticated or has signed out
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

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Create a map to hold the attendance data
            Map<String, Object> attendanceRecord = new HashMap<>();
            attendanceRecord.put("userId", userId);
            attendanceRecord.put("message", message);
            attendanceRecord.put("status", "On-Time");
            attendanceRecord.put("timeIn", timestamp);
            attendanceRecord.put("className", className);

            // Use set with SetOptions.merge() to update the document if it exists or create it if it doesn't
            db.collection("attendRecord")
                    .document(userId + "_" + className)
                    .set(attendanceRecord, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Time in recorded successfully!");
                        // Show the popup when time in is recorded successfully
                        showPopup();
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

    private void showPopup() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_timein, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        View rootView = getView();

        View overlay = new View(requireContext());
        overlay.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        ((ViewGroup) rootView).addView(overlay);

        overlay.setClickable(true);
        overlay.setFocusable(true);

        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);

        AppCompatButton continueButton = popupView.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToStudentScheduleTimeout();

                ((ViewGroup) rootView).removeView(overlay);

                popupWindow.dismiss();
            }
        });
    }

    private void navigateToStudentScheduleTimeout() {
        // Create instance of StudentScheduleTimeout fragment
        StudentScheduleTimeout studentScheduleTimeoutFragment = StudentScheduleTimeout.newInstance(mParam1, mParam2);

        // Navigate to the StudentScheduleTimeout fragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, studentScheduleTimeoutFragment); // Use the correct container ID
        transaction.addToBackStack(null);
        transaction.commit();
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
                Log.e(TAG, "Error loading image from URL", e);
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
}
