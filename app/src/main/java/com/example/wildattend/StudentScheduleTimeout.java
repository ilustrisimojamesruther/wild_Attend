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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

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

public class StudentScheduleTimeout extends Fragment {

    private AppCompatButton timeOutButton;

    private TextView studentNameTextView;
    private TextView idNumberTextView;

    private ImageView profile_image;
    private static final String ARG_PARAM1 = "className";
    private static final String ARG_PARAM2 = "time";
    private static final String TAG = "StudentScheduleTimeout";

    private String mParam1;
    private String mParam2;

    private TextView classNameTextView;
    private TextView timeDisplay;

    public StudentScheduleTimeout() {
        // Required empty public constructor
    }

    public static StudentScheduleTimeout newInstance(String className, String time) {
        StudentScheduleTimeout fragment = new StudentScheduleTimeout();
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
        View rootView = inflater.inflate(R.layout.fragment_student_schedule_timeout, container, false);
        timeOutButton = rootView.findViewById(R.id.timeOutButton);

        profile_image = rootView.findViewById(R.id.profile_image);
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

        // Time Out Button Click Listener
        timeOutButton.setOnClickListener(v -> timeOut());

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
        View popupView = getLayoutInflater().inflate(R.layout.popup_timeout, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0);

        AppCompatButton continueButton = popupView.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(v -> {
            popupWindow.dismiss();
            getActivity().onBackPressed(); // Navigate back to the previous fragment/activity
        });
    }
}
