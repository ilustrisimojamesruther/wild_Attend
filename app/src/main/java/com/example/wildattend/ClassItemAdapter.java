package com.example.wildattend;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
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
    private int layoutResource;
    private boolean shouldChangeColor;

    public ClassItemAdapter(Context context, List<ClassItem> scheduleItems, int layoutResource, boolean shouldChangeColor) {
        super(context, 0, scheduleItems);
        this.context = context;
        this.scheduleItems = scheduleItems;
        this.layoutResource = layoutResource;
        this.shouldChangeColor = shouldChangeColor;
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

            if (shouldChangeColor) {
                String classColor = scheduleItem.getClassColor();
                if (classColor != null && !classColor.isEmpty()) {
                    courseCode.setBackground(getCircleBackground(classColor));
                } else {
                    courseCode.setBackground(getCircleBackground("#FF4081")); // Default color
                }
            }
        }

        return convertView;
    }

    private ShapeDrawable getCircleBackground(String classColor) {
        int color;
        try {
            color = Color.parseColor(classColor);
        } catch (IllegalArgumentException e) {
            color = Color.parseColor("#FF4081"); // Default color
        }

        ShapeDrawable circle = new ShapeDrawable(new OvalShape());
        circle.getPaint().setColor(color);

        return circle;
    }
}
