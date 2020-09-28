package com.fantasma.netflow.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fantasma.netflow.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TimeFrameSpinnerAdapter extends ArrayAdapter<String> {

    private String[] items;

    public TimeFrameSpinnerAdapter(Context context, String[] timeFrames) {
        super(context, 0, timeFrames);
        items = timeFrames;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.time_frame_open, parent, false
            );
            TextView timeFrame = convertView.findViewById(R.id.time_frame);
            timeFrame.setText(items[position]);
        }
        View extraSpace = convertView.findViewById(R.id.last_item_spinner);
        if(position == items.length-1){
            extraSpace.setVisibility(View.VISIBLE);
        } else {
            extraSpace.setVisibility(View.GONE);
        }
        return convertView;
    }
}