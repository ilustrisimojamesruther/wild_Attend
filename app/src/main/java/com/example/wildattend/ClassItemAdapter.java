package com.example.wildattend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class ClassItemAdapter extends ArrayAdapter<ClassItem> {
    private Context context;
    private List<ClassItem> scheduleItems;
    private int layoutResource; // Store the layout resource ID

    public ClassItemAdapter(Context context, List<ClassItem> scheduleItems, int layoutResource) {
        super(context, 0, scheduleItems);
        this.context = context;
        this.scheduleItems = scheduleItems;
        this.layoutResource = layoutResource; // Assign the layout resource ID
    }

    @NonNull
    @Override
    public View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
        ClassItem scheduleItem = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(layoutResource, parent, false);
        }

        TextView courseCode = convertView.findViewById(R.id.course_code);
        TextView courseName = convertView.findViewById(R.id.course_name);
        TextView timeDisplay = convertView.findViewById(R.id.timeDisplay);

        if (scheduleItem != null) {
            courseName.setText(scheduleItem.getClassDesc());
            courseCode.setText(scheduleItem.getClassCode());
            timeDisplay.setText(scheduleItem.getStartTime());
        }

        return convertView;
    }
}
