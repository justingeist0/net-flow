package com.fantasma.netflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class LogListAdapter extends RecyclerView.Adapter<LogListAdapter.MyViewHolder> {
    private View.OnClickListener logClicked, editButtonClicked;
    private View lastView;

    private AddLog addLogScreen;

    private Context context;
    private List<LogModel> logs;
    private boolean isLandscape;

    LogListAdapter(Context context, List<LogModel> logs, boolean isLandscape) {
        this.context = context;
        this.logs = logs;
        this.isLandscape = isLandscape;
        if(isLandscape) return;

        logClicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lastView != null && lastView != v) {
                    lastView.findViewById(R.id.buttonEdit).setVisibility(View.GONE);
                    lastView.findViewById(R.id.amount).setVisibility(View.VISIBLE);
                }

                View editButton = v.findViewById(R.id.buttonEdit);
                View amount = v.findViewById(R.id.amount);
                if(editButton.getVisibility() == View.GONE) {
                    editButton.setVisibility(View.VISIBLE);
                    amount.setVisibility(View.INVISIBLE);
                } else {
                    editButton.setVisibility(View.GONE);
                    amount.setVisibility(View.VISIBLE);
                }
                lastView = v;
            }
        };

        editButtonClicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEdit(v);
            }
        };
    }

    void setAddLogScreen(AddLog addLogScreen) {
        this.addLogScreen = addLogScreen;
    }

    void addNewLog(LogModel logModel) {
        logs.add(0,logModel);
        notifyDataSetChanged();
    }

    void removeLog(int position) {
        logs.remove(position);
        notifyDataSetChanged();
    }

    void updateLog(int position, LogModel newLog) {
        logs.set(position, newLog);
        notifyDataSetChanged();
    }

    private void startEdit(View v) {
        int position = (int) v.getTag();
        lastView.findViewById(R.id.buttonEdit).setVisibility(View.GONE);
        lastView.findViewById(R.id.amount).setVisibility(View.VISIBLE);
        addLogScreen.setEditMode(position, logs.size());
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.main_menu_log_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        boolean positive = logs.get(position).isPositive();

        holder.baseLayer.setBackgroundColor(position % 2 == 0 ? context.getResources().getColor(R.color.white) :
                context.getResources().getColor(R.color.lightGreen));
        holder.baseLayer.setOnClickListener(logClicked);

        if(isLandscape) {
            holder.timeStamp.setText(logs.get(position).getDayAndTime());
        } else {
            holder.timeStamp.setText(logs.get(position).getDay());
        }

        holder.amount.setVisibility(View.VISIBLE);
        holder.amount.setText(logs.get(position).getFormattedAmount());
        holder.amount.setTextColor(positive ? context.getResources().getColor(R.color.colorAccent) :
                context.getResources().getColor(R.color.red));

        holder.editButton.setVisibility(View.GONE);
        holder.editButton.setTag(position);
        holder.editButton.setOnClickListener(editButtonClicked);

        if(logs.get(position).hasDesc()) {
            holder.description.setText(logs.get(position).getPurpose());
            holder.description.setVisibility(View.VISIBLE);
            int padding = holder.amount.getPaddingTop();
            holder.amount.setPadding(padding, padding, padding, 0);
            holder.timeStamp.setPadding(padding, padding, padding, 0);
        } else{
            holder.description.setVisibility(View.GONE);

            int padding = holder.amount.getPaddingTop();
            holder.amount.setPadding(padding, padding, padding, padding);
            holder.timeStamp.setPadding(padding, padding, padding, padding);
        }
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout baseLayer;
        TextView timeStamp, amount, description;
        Button editButton;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            baseLayer = itemView.findViewById(R.id.mainLogBackground);
            timeStamp = itemView.findViewById(R.id.time);
            amount = itemView.findViewById(R.id.amount);
            description = itemView.findViewById(R.id.description);
            editButton = itemView.findViewById(R.id.buttonEdit);
        }
    }
}
