package com.example.wildattend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class FacultySchedule extends Fragment {

    private static final String TAG = "FacultySchedule";
    private TextView facultyNameTextView;
    private TextView idNumberTextView;
    private ListView listView;
    private ImageView profile_image;
    private List<ClassItem> scheduleItems;
    private ClassItemAdapter adapter;
    private int currentPage = 0;
    private final int pageSize = 4; // Display 4 classes per page
    private int totalPages = 0;
    private List<ClassItem> allClasses = new ArrayList<>();
    private Button prevPageButton, nextPageButton;
    private SearchView searchView;
    private List<ClassItem> filteredClasses = new ArrayList<>();
    private Spinner sortSpinner;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_faculty_schedule, container, false);

        facultyNameTextView = rootView.findViewById(R.id.facultyName);
        idNumberTextView = rootView.findViewById(R.id.idNumber);
        listView = rootView.findViewById(R.id.list_view_schedule);
        profile_image = rootView.findViewById(R.id.profile_image_faculty);
        prevPageButton = rootView.findViewById(R.id.prevButton);
        nextPageButton = rootView.findViewById(R.id.nextButton);
        searchView = rootView.findViewById(R.id.searchView);
        sortSpinner = rootView.findViewById(R.id.sortSpinner);


        sortSpinner.setPrompt("SORT BY");
        prevPageButton.setOnClickListener(v -> onPrevPage());
        nextPageButton.setOnClickListener(v -> onNextPage());

        scheduleItems = new ArrayList<>();
        adapter = new ClassItemAdapter(requireContext(), scheduleItems, R.layout.list_class_schedule, true);
        listView.setAdapter(adapter);

        // Set up the ArrayAdapter for the Spinner
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.sort_options, // Array of sorting options
                android.R.layout.simple_spinner_item // Layout for the spinner
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);

        fetchUserInformation();
        setupListView();
        setupSearchView();
        setupSpinner();


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

                            facultyNameTextView.setText(firstName + " " + lastName);
                            idNumberTextView.setText(idNumber);

                            new LoadImageTask(profile_image).execute(imageUrl);

                            fetchUserClasses(currentUser.getUid());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching user document", e);
                    });
        } else {
            Log.e(TAG, "User is not authenticated");
        }
    }

    private void fetchUserClasses(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("userClasses")
                .whereEqualTo("userID", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String classID = documentSnapshot.getString("classID");
                        fetchClassDetails(classID);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user classes", e);
                });
    }

    private Set<String> classIdSet = new HashSet<>(); // Set to track unique class IDs

    private void fetchClassDetails(String classID) {
        // Check if the class has already been added
        if (classIdSet.contains(classID)) {
            return; // Skip if already added
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("classes")
                .document(classID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String classCode = documentSnapshot.getString("classCode");
                        String classDesc = documentSnapshot.getString("classDesc");
                        String startTime = documentSnapshot.getString("startTime");
                        String formattedStartTime = formatTime(startTime);
                        String endTime = documentSnapshot.getString("endTime");
                        String formattedEndTime = formatTime(endTime);
                        String classColor = documentSnapshot.getString("classColor");
                        String classRoom = documentSnapshot.getString("classRoom");

                        // Fetch day booleans with null checks using temporary variables
                        Boolean mondayObj = documentSnapshot.getBoolean("Monday");
                        boolean monday = mondayObj != null && mondayObj;

                        Boolean tuesdayObj = documentSnapshot.getBoolean("Tuesday");
                        boolean tuesday = tuesdayObj != null && tuesdayObj;

                        Boolean wednesdayObj = documentSnapshot.getBoolean("Wednesday");
                        boolean wednesday = wednesdayObj != null && wednesdayObj;

                        Boolean thursdayObj = documentSnapshot.getBoolean("Thursday");
                        boolean thursday = thursdayObj != null && thursdayObj;

                        Boolean fridayObj = documentSnapshot.getBoolean("Friday");
                        boolean friday = fridayObj != null && fridayObj;

                        Boolean saturdayObj = documentSnapshot.getBoolean("Saturday");
                        boolean saturday = saturdayObj != null && saturdayObj;

                        Boolean sundayObj = documentSnapshot.getBoolean("Sunday");
                        boolean sunday = sundayObj != null && sundayObj;

                        ClassItem item = new ClassItem(classCode, classDesc, formattedStartTime, formattedEndTime, classColor, classRoom, monday, tuesday, wednesday, thursday, friday, saturday, sunday);

                        // Add the class ID to the set to track uniqueness
                        classIdSet.add(classID);
                        allClasses.add(item);
                        totalPages = (int) Math.ceil((double) allClasses.size() / pageSize);

                        updateListView();
                    } else {
                        Log.e(TAG, "Class document does not exist");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching class details", e));
    }


    private String formatTime(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date date = sdf.parse(time);
            sdf.applyPattern("h:mm a");
            return sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return time;
        }
    }


    private void setupListView() {
        listView.setAdapter(adapter);

        // Set item click listener to navigate to class schedule
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ClassItem selectedItem = (ClassItem) parent.getItemAtPosition(position);
            if (selectedItem != null) {
                // Navigate to the detailed schedule of the selected class
                navigateToClassSchedule(selectedItem.getClassCode(), selectedItem.getStartTime(), selectedItem.getEndTime(), selectedItem.getClassDesc(), selectedItem.getClassRoom(), selectedItem.getClassColor());
            }
        });
    }

    private void updateListView() {
        scheduleItems.clear();

        List<ClassItem> displayList = filteredClasses.isEmpty() ? allClasses : filteredClasses;

        totalPages = (int) Math.ceil((double) displayList.size() / pageSize);

        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, displayList.size());

        for (int i = start; i < end; i++) {
            scheduleItems.add(displayList.get(i));
        }

        adapter.notifyDataSetChanged();

        prevPageButton.setEnabled(currentPage > 0);
        nextPageButton.setEnabled(currentPage < totalPages - 1);
    }


    private void onPrevPage() {
        if (currentPage > 0) {
            currentPage--;
            updateListView();
        }
    }

    private void onNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateListView();
        }
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterClass(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterClass(newText);
                return false;
            }
        });
    }

    private void filterClass(String query) {
        filteredClasses.clear();
        if (query.isEmpty()) {
            filteredClasses.addAll(allClasses);
        } else {
            for (ClassItem item : allClasses) {
                if (item.getClassCode().toLowerCase().contains(query.toLowerCase()) ||
                        item.getClassDesc().toLowerCase().contains(query.toLowerCase())) {
                    filteredClasses.add(item);
                }
            }
        }
        currentPage = 0; // Reset to first page
        updateListView();
    }

    private void setupSpinner() {
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == AdapterView.INVALID_POSITION) {
                    return;
                }

                switch (position) {
                    case 0: // Ascending
                        sortByAscending();
                        break;
                    case 1: // Time
                        sortByTime();
                        break;
                    case 2: // Day
                        sortByDay();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        sortSpinner.setSelection(AdapterView.INVALID_POSITION);
    }

    private void sortByDay() {
        List<ClassItem> listToSort = filteredClasses.isEmpty() ? allClasses : filteredClasses;

        // Debugging: Log the days before sorting
        for (ClassItem item : listToSort) {
            Log.d(TAG, "Class: " + item.getClassCode() + ", Day: " + item.getDayOfWeek());
        }

        listToSort.sort((c1, c2) -> Integer.compare(c1.getDayOfWeek(), c2.getDayOfWeek()));

        // Debugging: Log the days after sorting
        for (ClassItem item : listToSort) {
            Log.d(TAG, "Sorted Class: " + item.getClassCode() + ", Day: " + item.getDayOfWeek());
        }

        currentPage = 0; // Reset to the first page after sorting
        updateListView();
    }


    private boolean getDayValue(ClassItem item, String day) {
        switch (day) {
            case "Monday": return item.isMonday();
            case "Tuesday": return item.isTuesday();
            case "Wednesday": return item.isWednesday();
            case "Thursday": return item.isThursday();
            case "Friday": return item.isFriday();
            case "Saturday": return item.isSaturday();
            case "Sunday": return item.isSunday();
            default: return false;
        }
    }


    private void sortByAscending() {
        List<ClassItem> listToSort = filteredClasses.isEmpty() ? allClasses : filteredClasses;
        listToSort.sort((c1, c2) -> c1.getClassDesc().compareToIgnoreCase(c2.getClassDesc()));
        updateListView();
    }

    private void sortByTime() {
        List<ClassItem> listToSort = filteredClasses.isEmpty() ? allClasses : filteredClasses;
        listToSort.sort((c1, c2) -> {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
            try {
                Date time1 = sdf.parse(c1.getStartTime());
                Date time2 = sdf.parse(c2.getStartTime());
                return time1.compareTo(time2);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });
        updateListView();
    }








    private void navigateToClassSchedule(String classCode, String startTime, String endTime, String classDesc, String classRoom, String classColor) {
        // Create instance of FacultyScheduleTimeIn fragment and pass class code as argument
        FacultyScheduleTimeIn facultyScheduleTimeInFragment = FacultyScheduleTimeIn.newInstance(classCode, startTime, endTime, classDesc, classRoom, classColor);

        // Navigate to the FacultyScheduleTimeIn fragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.faculty_frame_layout, facultyScheduleTimeInFragment)
                .addToBackStack(null)
                .commit();
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
    @Override
    public void onResume() {
        super.onResume();
        // Show the bottom navigation bar
        ((FacultyDashboard) getActivity()).showBottomNavigation();
    }
}
