package com.example.wildattend;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StudentScheduleClass#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StudentScheduleClass extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private AppCompatButton presentButton;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StudentScheduleClass() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StudentScheduleClass.
     */
    // TODO: Rename and change types and number of parameters
    public static StudentScheduleClass newInstance(String param1, String param2) {
        StudentScheduleClass fragment = new StudentScheduleClass();
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
        View rootView = inflater.inflate(R.layout.fragment_student_schedule_class, container, false);
        presentButton = rootView.findViewById(R.id.presentButton);
        presentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup();
            }
        });
        return rootView;
    }

    private void showPopup() {
        // Inflate the layout for the popup window
        View popupView = getLayoutInflater().inflate(R.layout.popup_late, null);

        // Create a PopupWindow object
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // Allows touch events outside of the PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // Find the root view of your fragment
        View rootView = getView();

        // Create the overlay view
        View overlay = new View(requireContext());
        overlay.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setBackgroundColor(getResources().getColor(android.R.color.transparent)); // Semi-transparent color
        ((ViewGroup) rootView).addView(overlay);

        // Make the overlay clickable and focusable to intercept touch events
        overlay.setClickable(true);
        overlay.setFocusable(true);

        // Set up the popup window with custom layout
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);

        // Find the "Continue" button inside the popup layout
        Button continueButton = popupView.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the layout for the StudentScheduleLate fragment
                View studentScheduleLateView = getLayoutInflater().inflate(R.layout.fragment_student_schedule_late, null);

                // Replace the current fragment layout with the StudentScheduleLate layout
                ViewGroup parent = (ViewGroup) rootView.getParent();
                int index = parent.indexOfChild(rootView);
                parent.removeView(rootView);
                parent.addView(studentScheduleLateView, index);

                // Remove the overlay
                ((ViewGroup) rootView).removeView(overlay);

                // Dismiss the popup window
                popupWindow.dismiss();
            }
        });
    }


}