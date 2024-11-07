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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.imageview.ShapeableImageView;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StudentScheduleTimeIn extends Fragment {

    private AppCompatButton timeinButton;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final String ARG_PARAM5 = "param5";
    private static final String ARG_PARAM6 = "param6";
    private static final String TAG = "StudentScheduleTimeIn";

    private String mParam1;
    private String mParam2;
    private String mParam3;
    private String mParam4;
    private String mParam5;
    private String mParam6;

    private TextView studentNameTextView;
    private TextView idNumberTextView;
    private ShapeableImageView profile_image;

    public StudentScheduleTimeIn() {
        // Required empty public constructor
    }

    public static StudentScheduleTimeIn newInstance(String param1, String param2, String param3, String param4, String param5, String param6) {
        StudentScheduleTimeIn fragment = new StudentScheduleTimeIn();
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
        View view = inflater.inflate(R.layout.fragment_student_schedule_timein, container, false);

        // Set class name and time in the TextViews
        TextView classCodeTextView = view.findViewById(R.id.classCode);
        TextView timeDisplayTextView = view.findViewById(R.id.timeDisplay);
        TextView classNameTextView = view.findViewById(R.id.className);
        TextView roomLocationTextView = view.findViewById(R.id.roomLocation);
        View classColorCircle = view.findViewById(R.id.classColorCircle);

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
            String className = mParam1; // Retrieve class code from arguments
            String roomLocation = mParam5;
            String classDesc = mParam4;
            String startTime = mParam2; // Retrieve class start time from arguments
            String endTime = mParam3; // Retrieve class end time from arguments
            String message = "I'm here on time"; // Default message
            Date timestamp = new Date(); // Get current timestamp

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Log.d(TAG, "Class name: " + className); // Add this debug log
//            Log.d(TAG ,"Current Time: " + currentTime);

            // Step 1: Fetch the class ID using the class code
            db.collection("classes")
                    .whereEqualTo("classCode", className)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Assuming classCode is unique and we get only one document
                            DocumentSnapshot classDocument = queryDocumentSnapshots.getDocuments().get(0);
                            String classId = classDocument.getId(); // Retrieve class ID
                            Log.d(TAG, "Class ID: " + classId);

                            Boolean ongoing = classDocument.getBoolean("Ongoing");
                            if (ongoing != null && ongoing) {
                                try {
                                    // Time comparison similar to FacultyScheduleTimeIn
                                    SimpleDateFormat fullDateFormat12Hour = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
                                    SimpleDateFormat timeFormat12Hour = new SimpleDateFormat("hh:mm a", Locale.getDefault());

                                    // Get today's date for timestamp comparison
                                    final String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                                    // Parse the start and end times
                                    Date startDate = fullDateFormat12Hour.parse(today + " " + startTime);
                                    Date endDate = fullDateFormat12Hour.parse(today + " " + endTime);

                                    // Check if endDate is before startDate (handle overnight classes)
                                    if (endDate.before(startDate)) {
                                        Calendar calendarEnd = Calendar.getInstance();
                                        calendarEnd.setTime(endDate);
                                        calendarEnd.add(Calendar.DATE, 1);
                                        endDate = calendarEnd.getTime();
                                    }

                                    // Current time for comparison
                                    final Date currentTime = new Date();

                                    // Calculate 15 minutes before start time, 15 minutes late, and 30 minutes late
                                    Date fifteenMinutesEarly = new Date(startDate.getTime() - 15 * 60 * 1000);
                                    Date fifteenMinutesLate = new Date(startDate.getTime() + 15 * 60 * 1000);
                                    Date thirtyMinutesLate = new Date(startDate.getTime() + 30 * 60 * 1000);

                                    // Log the times in 12-hour format for debugging
                                    Log.d(TAG, "Start time (12-hour): " + timeFormat12Hour.format(startDate));
                                    Log.d(TAG, "End time (12-hour): " + timeFormat12Hour.format(endDate));
                                    Log.d(TAG, "Fifteen minutes early: " + timeFormat12Hour.format(fifteenMinutesEarly));
                                    Log.d(TAG, "Fifteen minutes late: " + timeFormat12Hour.format(fifteenMinutesLate));
                                    Log.d(TAG, "Thirty minutes late: " + timeFormat12Hour.format(thirtyMinutesLate));
                                    Log.d(TAG, "Current time: " + timeFormat12Hour.format(currentTime));

                                    // Determine student's status (On-Time, Late, Absent)
                                    String status;
                                    if (currentTime.after(thirtyMinutesLate)) {
                                        status = "Absent";
                                    } else if (currentTime.after(fifteenMinutesLate)) {
                                        status = "Late";
                                    } else if (currentTime.after(fifteenMinutesEarly) && currentTime.before(fifteenMinutesLate)) {
                                        status = "On-Time";
                                    } else {
                                        Log.e(TAG, "Time in is not allowed. Outside the allowed window.");
                                        Toast.makeText(getContext(), "Time in is not allowed. You can time in 15 minutes early until the class end time.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    // Ensure time-in is within the allowed window (15 minutes early to end time)
                                    if (currentTime.before(endDate)) {
                                        // Proceed with recording the time-in
                                        Map<String, Object> attendanceRecord = new HashMap<>();
                                        attendanceRecord.put("userId", userId);
                                        attendanceRecord.put("className", className);
                                        attendanceRecord.put("message", message);
                                        attendanceRecord.put("status", status); // Use calculated status
                                        attendanceRecord.put("timeIn", timestamp);
                                        attendanceRecord.put("classId", classId); // Use class ID

                                        // Use set with SetOptions.merge() to update the document if it exists or create it if it doesn't
                                        String attendanceId = userId + "_" + classId + "_" + System.currentTimeMillis(); // Unique ID based on userId and timestamp

                                        db.collection("attendRecord")
//                                                .document(userId + "_" + classId) // Use class ID in document naming
//                                                .set(attendanceRecord, SetOptions.merge())
                                                .document(attendanceId) // Create a new document with the unique ID
                                                .set(attendanceRecord)
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
                                        Log.e(TAG, "Class has already ended.");
                                        Toast.makeText(getContext(), "Class has already ended", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (ParseException e) {
                                    Log.e(TAG, "Error parsing time", e);
                                }
                            } else {
                                // Class is not ongoing, inform the student
                                Log.d(TAG, "Class is not ongoing. Cannot time in.");
                                Toast.makeText(getContext(), "Class is not ongoing. Cannot time in.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.e(TAG, "Class document does not exist for class: " + className);
                            Toast.makeText(getContext(), "Class does not exist.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error checking if class is ongoing", e);
                        Toast.makeText(getContext(), "Error checking class status.", Toast.LENGTH_LONG).show();
                    });
        } else {
            Log.e(TAG, "User not authenticated");
            Toast.makeText(getContext(), "User not authenticated.", Toast.LENGTH_LONG).show();
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

        ((StudentDashboard) getActivity()).hideBottomNavigation();
        // Create instance of StudentScheduleTimeout fragment
        StudentScheduleTimeout studentScheduleTimeoutFragment = StudentScheduleTimeout.newInstance(mParam1, mParam2, mParam3, mParam4, mParam5, mParam6);

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
