package com.example.wildattend;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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

public class StudentScheduleTimeout extends Fragment {

    private AppCompatButton timeoutButtonStudent;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final String ARG_PARAM5 = "param5";
    private static final String ARG_PARAM6 = "param6";
    private static final String TAG = "StudentScheduleTimeout";

    private String mParam1;
    private String mParam2;
    private String mParam3;
    private String mParam4;
    private String mParam5;
    private String mParam6;

    private TextView studentNameTextView;
    private TextView idNumberTextView;
    private ImageView profile_image;

    public StudentScheduleTimeout() {
        // Required empty public constructor
    }

    public static StudentScheduleTimeout newInstance(String param1, String param2, String param3, String param4, String param5, String param6) {
        StudentScheduleTimeout fragment = new StudentScheduleTimeout();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        args.putString(ARG_PARAM4, param4);
        args.putString(ARG_PARAM5, param5);
        args.putString(ARG_PARAM6, param6);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mParam3 = getArguments().getString(ARG_PARAM3);
            mParam4 = getArguments().getString(ARG_PARAM4);
            mParam5 = getArguments().getString(ARG_PARAM5);
            mParam6 = getArguments().getString(ARG_PARAM6);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_student_schedule_timeout, container, false);

        // Initialize views
        profile_image = rootView.findViewById(R.id.profile_image_student);
        studentNameTextView = rootView.findViewById(R.id.facultyName);
        idNumberTextView = rootView.findViewById(R.id.idNumber);
        timeoutButtonStudent = rootView.findViewById(R.id.studentTimeOutButton);

        // Set class name and time in the TextViews
        TextView classCodeTextView = rootView.findViewById(R.id.classCode2);
        TextView timeDisplayTextView = rootView.findViewById(R.id.timeDisplay);
        TextView classNameTextView = rootView.findViewById(R.id.className);
        TextView roomLocationTextView = rootView.findViewById(R.id.roomLocation);
        View classColorCircle = rootView.findViewById(R.id.classColorCircle2);

        if (mParam1 != null) {
            classCodeTextView.setText(mParam1);
        }
        if (mParam2 != null && mParam3 != null) {
            String timeDisplay = mParam2 + " - " + mParam3;
            timeDisplayTextView.setText(timeDisplay);
        }
        if (mParam4 != null) {
            classNameTextView.setText(mParam4);
        }
        if (mParam5 != null) {
            roomLocationTextView.setText(mParam5);
        }
        if (mParam6 != null) {
            int classColor = Color.parseColor(mParam6);
            classColorCircle.setBackgroundColor(classColor);
            classColorCircle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(mParam6)));
            classColorCircle.setBackground(getResources().getDrawable(R.drawable.circle_background));

        }

        timeoutButtonStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeOut();
            }
        });

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
                    .whereEqualTo("email", userEmail)
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
                            new StudentLoadImageTask(profile_image).execute(imageUrl);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching user document", e);
                    });
        } else {
            Log.e(TAG, "User is not authenticated");
        }
    }

    private void timeOut() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String classCode = mParam1; // Assuming you have the class code available
            Date timestamp = new Date(); // Get current timestamp

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Fetch the class document to get the class ID
            db.collection("classes")
                    .whereEqualTo("classCode", classCode)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot classDocument = queryDocumentSnapshots.getDocuments().get(0);
                            String classId = classDocument.getId(); // Get classId here
                            String teacherId = classDocument.getString("teacherId"); // Assuming teacherId is stored here

                            // Check if the teacher has timed out
                            db.collection("attendRecord")
                                    .document(teacherId + "_" + classId)
                                    .get()
                                    .addOnSuccessListener(teacherDocument -> {
                                        if (teacherDocument.exists() && teacherDocument.contains("timeOut")) {
                                            // Teacher has timed out, now proceed with timing out the student
                                            recordTimeOut(userId, classId, timestamp); // Use classId here
                                        } else {
                                            // Notify user that the teacher has not timed out yet
                                            Toast.makeText(getContext(), "You cannot time out until the teacher has timed out.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Class document does not exist for class: " + classCode);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching class document", e);
                    });
        } else {
            Log.e(TAG, "User not authenticated");
        }
    }

    private void recordTimeOut(String userId, String classId, Date timestamp) { // Accept classId instead of classCode
        Map<String, Object> timeoutData = new HashMap<>();
        timeoutData.put("timeOut", timestamp);
        timeoutData.put("message", "I'm here on time"); // Set the default message to a predefined value
        timeoutData.put("status", "On-Time"); // Set the default status

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Update the timeOut field in the attendance record using classId
        db.collection("attendRecord")
                .document(userId + "_" + classId)
                .set(timeoutData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Time out recorded successfully with message: " + timeoutData.get("message"));
                    showTimeoutPopup();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error recording time out", e);
                });
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
            // For example, navigate to StudentSchedule
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.popBackStack(); // Clear the back stack
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, new StudentSchedule());
            fragmentTransaction.commit();
            popupWindow.dismiss(); // Dismiss the popup window after navigation
        });

        timeOutNo.setOnClickListener(v -> {
            // Perform actions when "No" button is clicked
            // For example, dismiss the popup window
            popupWindow.dismiss();
        });
    }

    private static class StudentLoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewWeakReference;

        public StudentLoadImageTask(ImageView imageView) {
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
