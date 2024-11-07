package com.example.wildattend;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
import android.widget.Toast;;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatButton;

import androidx.fragment.app.Fragment;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FacultyScheduleTimeIn extends Fragment {

    private AppCompatButton timeinButton;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final String ARG_PARAM5 = "param5";
    private static final String ARG_PARAM6 = "param6";
    private static final String TAG = "FacultyScheduleTimeIn";

    private String mParam1;
    private String mParam2;
    private String mParam3;
    private String mParam4;
    private String mParam5;
    private String mParam6;

    private TextView facultyNameTextView;
    private TextView idNumberTextView;
    private ImageView profile_image;

    public FacultyScheduleTimeIn() {
        // Required empty public constructor
    }

    public static FacultyScheduleTimeIn newInstance(String param1, String param2, String param3, String param4, String param5, String param6) {
        FacultyScheduleTimeIn fragment = new FacultyScheduleTimeIn();
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
        View view = inflater.inflate(R.layout.fragment_faculty_schedule_timein, container, false);

        // Set class name and time in the TextViews
        TextView classCodeTextView = view.findViewById(R.id.course_code);
        TextView timeDisplayTextView = view.findViewById(R.id.timeDisplay);
        TextView classDescTextView = view.findViewById(R.id.className);
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
            classDescTextView.setText(mParam4);
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
        profile_image = view.findViewById(R.id.profile_image_faculty);
        facultyNameTextView = view.findViewById(R.id.facultyName);
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
                            facultyNameTextView.setText(firstName + " " + lastName);
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
        // Check for location permissions before accessing Wi-Fi details
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            Log.e(TAG, "Location permission not granted. Requesting permission.");
            Toast.makeText(getContext(), "Location permission required to access Wi-Fi information.", Toast.LENGTH_SHORT).show();
            return; // Exit the method if permission is not granted
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            final String userId = currentUser.getUid();
            final String className = mParam1;
            final String startTime = mParam2;
            final String endTime = mParam3;
            final Date timestamp = new Date();

            // Define today’s date as final
            final String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // Define the full date format used for parsing date and time strings as final
            final SimpleDateFormat fullDateFormat12Hour = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
            final SimpleDateFormat timeFormat12Hour = new SimpleDateFormat("hh:mm a", Locale.getDefault());

            // Determine the current day of the week and set currentDay as final
            Calendar calendar = Calendar.getInstance();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            final String currentDay;
            switch (dayOfWeek) {
                case Calendar.MONDAY: currentDay = "Monday"; break;
                case Calendar.TUESDAY: currentDay = "Tuesday"; break;
                case Calendar.WEDNESDAY: currentDay = "Wednesday"; break;
                case Calendar.THURSDAY: currentDay = "Thursday"; break;
                case Calendar.FRIDAY: currentDay = "Friday"; break;
                case Calendar.SATURDAY: currentDay = "Saturday"; break;
                case Calendar.SUNDAY: currentDay = "Sunday"; break;
                default: currentDay = ""; // Default case
            }

            // Retrieve the device's connected Wi-Fi SSID
            WifiManager wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String connectedSSIDRaw = wifiInfo.getSSID();

            // Remove quotes if present and make it final for lambda usage
            final String connectedSSID = connectedSSIDRaw.replaceAll("\"", "");

            Log.d(TAG, "Connected Wi-Fi SSID: " + connectedSSID);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("classes")
                    .whereEqualTo("classCode", className)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot classDocument = queryDocumentSnapshots.getDocuments().get(0);

                            // Retrieve the expected SSID from Firebase
                            String expectedSSID = classDocument.getString("ssid");
                            Log.d(TAG, "Expected Wi-Fi SSID from Firebase: " + expectedSSID);

                            // Verify the user is connected to the correct Wi-Fi network
                            if (connectedSSID != null && connectedSSID.equalsIgnoreCase(expectedSSID)) {
                                // Proceed with the rest of the time-in logic

                                Boolean isScheduledToday = classDocument.getBoolean("days." + currentDay);
                                if (isScheduledToday != null && isScheduledToday) {
                                    try {
                                        // Combine today’s date with start and end times for parsing
                                        Date startDate = fullDateFormat12Hour.parse(today + " " + startTime);
                                        Date endDate = fullDateFormat12Hour.parse(today + " " + endTime);

                                        // Check if endDate is before startDate
                                        if (endDate.before(startDate)) {
                                            Calendar calendarEnd = Calendar.getInstance();
                                            calendarEnd.setTime(endDate);
                                            calendarEnd.add(Calendar.DATE, 1);
                                            endDate = calendarEnd.getTime();
                                        }

                                        // Current time
                                        final Date currentTime = new Date();

                                        // Calculate 15 minutes before, 15 minutes late, and 30 minutes late from start time
                                        Date fifteenMinutesEarly = new Date(startDate.getTime() - 15 * 60 * 1000);
                                        Date fifteenMinutesLate = new Date(startDate.getTime() + 15 * 60 * 1000);
                                        Date thirtyMinutesLate = new Date(startDate.getTime() + 30 * 60 * 1000);

                                        // Log the times in 12-hour format with AM/PM
                                        Log.d("FacultyScheduleTimeIn", "Start time (12-hour): " + timeFormat12Hour.format(startDate));
                                        Log.d("FacultyScheduleTimeIn", "End time (12-hour): " + timeFormat12Hour.format(endDate));
                                        Log.d("FacultyScheduleTimeIn", "Fifteen minutes early: " + timeFormat12Hour.format(fifteenMinutesEarly));
                                        Log.d("FacultyScheduleTimeIn", "Fifteen minutes late: " + timeFormat12Hour.format(fifteenMinutesLate));
                                        Log.d("FacultyScheduleTimeIn", "Thirty minutes late: " + timeFormat12Hour.format(thirtyMinutesLate));
                                        Log.d("FacultyScheduleTimeIn", "Current time: " + timeFormat12Hour.format(currentTime));

                                        // Determine the user's status
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
                                            String classId = classDocument.getId();
                                            Boolean ongoing = classDocument.getBoolean("Ongoing");
                                            if (ongoing == null || !ongoing) {
                                                Map<String, Object> attendanceRecord = new HashMap<>();
                                                attendanceRecord.put("userId", userId);
                                                attendanceRecord.put("className", className);
                                                attendanceRecord.put("status", status);
                                                attendanceRecord.put("timeIn", timestamp);
                                                attendanceRecord.put("classId", classId);

                                                Map<String, Object> classUpdate = new HashMap<>();
                                                classUpdate.put("Ongoing", true);

                                                // Update class document to mark it as ongoing
                                                db.collection("classes")
                                                        .document(classId)
                                                        .set(classUpdate, SetOptions.merge())
                                                        .addOnSuccessListener(aVoid -> {
                                                            // Use a unique document ID for the attendance record
                                                            String attendanceId = userId + "_" + classId + "_" + System.currentTimeMillis();
                                                            db.collection("attendRecord")
                                                                    .document(attendanceId)
                                                                    .set(attendanceRecord)
                                                                    .addOnSuccessListener(aVoid2 -> {
                                                                        Log.d(TAG, "Time in recorded successfully!");
                                                                        showPopup();
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Log.e(TAG, "Error recording time in", e);
                                                                    });
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e(TAG, "Error updating class document", e);
                                                        });
                                            } else {
                                                Log.e(TAG, "Class is already ongoing");
                                                Toast.makeText(getContext(), "Class is already ongoing", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Log.e(TAG, "Class has already ended.");
                                            Toast.makeText(getContext(), "Class has already ended", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (ParseException e) {
                                        Log.e(TAG, "Error parsing time", e);
                                    }
                                } else {
                                    Log.e(TAG, "Class is not scheduled for today.");
                                    Toast.makeText(getContext(), "Class is not scheduled for today.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Log both SSIDs if they do not match
                                Log.e(TAG, "User is not connected to the required Wi-Fi network.");
                                Log.e(TAG, "Connected SSID: " + connectedSSID);
                                Log.e(TAG, "Expected SSID: " + expectedSSID);
                                Toast.makeText(getContext(), "Please connect to the specified Wi-Fi network to time in.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Class document not found");
                            Toast.makeText(getContext(), "Class document not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching class document", e);
                    });
        } else {
            Log.e(TAG, "User not authenticated");
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
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
                navigateToFacultyScheduleTimeout();

                ((ViewGroup) rootView).removeView(overlay);

                popupWindow.dismiss();
            }
        });
    }

//    private void autoMarkAbsent(String classId, String className) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        // Retrieve all users who should have attended the class
//        db.collection("users")
//                .whereEqualTo("role", "Student") // Assuming you have a role field to filter students
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                        String userId = documentSnapshot.getId(); // Assuming userId is the document ID
//
//                        // Check if an attendance record exists for this user in the current class
//                        db.collection("attendRecord")
//                                .document(userId + "_" + classId)
//                                .get()
//                                .addOnSuccessListener(attendRecordSnapshot -> {
//                                    if (!attendRecordSnapshot.exists()) {
//                                        // If no attendance record, mark the user as Absent
//                                        Map<String, Object> absentRecord = new HashMap<>();
//                                        absentRecord.put("userId", userId);
//                                        absentRecord.put("className", className);
//                                        absentRecord.put("status", "Absent");
//                                        absentRecord.put("timeIn", null); // No time-in
//                                        absentRecord.put("classId", classId);
//
//                                        db.collection("attendRecord")
//                                                .document(userId + "_" + classId)
//                                                .set(absentRecord, SetOptions.merge())
//                                                .addOnSuccessListener(aVoid -> {
//                                                    Log.d(TAG, "User marked as Absent successfully: " + userId);
//                                                })
//                                                .addOnFailureListener(e -> {
//                                                    Log.e(TAG, "Error marking user as Absent", e);
//                                                });
//                                    }
//                                });
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Error fetching users", e);
//                });
//    }


    private void navigateToFacultyScheduleTimeout() {
        ((FacultyDashboard) getActivity()).hideBottomNavigation();
        // Create instance of FacultyScheduleTimeout fragment
        FacultyScheduleTimeout facultyScheduleTimeoutFragment = FacultyScheduleTimeout.newInstance(mParam1, mParam2, mParam3, mParam4, mParam5, mParam6);

        // Navigate to the FacultyScheduleTimeout fragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.faculty_frame_layout, facultyScheduleTimeoutFragment); // Use the correct container ID
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