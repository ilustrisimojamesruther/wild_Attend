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
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import java.util.Date;

public class FacultyScheduleTimeout extends Fragment {

    private AppCompatButton timeoutButtonFaculty;
    private static final String ARG_PARAM1 = "className";
    private static final String ARG_PARAM2 = "time";
    private static final String TAG = "FacultyScheduleTimeout";

    private String mParam1;
    private String mParam2;

    private TextView facultyNameTextView;
    private TextView idNumberTextView;
    private ImageView profile_image;

    public FacultyScheduleTimeout() {
        // Required empty public constructor
    }

    public static FacultyScheduleTimeout newInstance(String className, String time) {
        FacultyScheduleTimeout fragment = new FacultyScheduleTimeout();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_faculty_schedule_timeout, container, false);
        timeoutButtonFaculty = rootView.findViewById(R.id.facultyTimeOutButton);
        timeoutButtonFaculty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeOut();
            }
        });

        // Initialize views
        profile_image = rootView.findViewById(R.id.profile_image_faculty);
        facultyNameTextView = rootView.findViewById(R.id.facultyName);
        idNumberTextView = rootView.findViewById(R.id.idNumber);

        // Fetch and display user information
        fetchUserInformation();

        return rootView;
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
                            facultyNameTextView.setText(firstName + " " + lastName);
                            idNumberTextView.setText(idNumber);

                            // Load the image from URL
                            new FacultyLoadImageTask(profile_image).execute(imageUrl);
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

    private void timeOut() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String className = mParam1; // Assuming you have the class name available
            String time = mParam2; // Assuming you have the class time available
            Date timestamp = new Date(); // Get current timestamp

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("attendRecord")
                    .document(userId)
                    .update("timeOut", timestamp)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Time out recorded successfully!");
                        showTimeoutPopup();
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error recording time out", e));
        } else {
            Log.e(TAG, "User not authenticated");
        }
    }

    private void showTimeoutPopup() {
        // Inflate the layout for the popup
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_timeout_confirm, null);

        // Create a PopupWindow with the inflated layout
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        // Set the location of the popup window
        popupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);

        // Set click listeners for the Yes and No buttons in the popup
        AppCompatButton timeOutYes = popupView.findViewById(R.id.timeOutYes);
        AppCompatButton timeOutNo = popupView.findViewById(R.id.timeOutNo);

        timeOutYes.setOnClickListener(v -> {
            // Perform actions when "Yes" button is clicked
            // For example, navigate to FacultySchedule
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.popBackStack(); // Clear the back stack
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new FacultySchedule());
            fragmentTransaction.commit();
            popupWindow.dismiss(); // Dismiss the popup window after navigation
        });

        timeOutNo.setOnClickListener(v -> {
            // Perform actions when "No" button is clicked
            // For example, dismiss the popup window
            popupWindow.dismiss();
        });
    }


    private static class FacultyLoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewWeakReference;

        public FacultyLoadImageTask(ImageView imageView) {
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
