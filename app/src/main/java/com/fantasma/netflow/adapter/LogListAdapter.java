package com.fantasma.netflow.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fantasma.netflow.R;
import com.fantasma.netflow.model.LogModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LogListAdapter extends RecyclerView.Adapter<LogListAdapter.MyViewHolder> {
    private int selected;
    private View editBtn;
    private EditBtnClickListener mEditBtnClickListener;

    private Context context;
    private List<LogModel> logs;

    public LogListAdapter(Context context, EditBtnClickListener editBtnClickListener) {
        this.context = context;
        this.mEditBtnClickListener = editBtnClickListener;
        selected = -1;
    }

    public void setLogs(List<LogModel> updatedLogs) {
        logs = updatedLogs;
        notifyDataSetChanged();
    }

    public void addNewLog(LogModel logModel) {
        logs.add(0,logModel);
        notifyItemInserted(0);
        selected = -1;
    }

    public void removeLog() {
        notifyItemRemoved(selected);
        selected = -1;
    }

    public void updateLog(LogModel newLog) {
        logs.set(selected, newLog);
        notifyItemChanged(selected);
        selected = -1;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.main_menu_log_row, parent, false);
        return new MyViewHolder(view, mEditBtnClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        boolean positive = logs.get(position).isPositive();
        holder.timeStamp.setText(
                formatTimeStamp(
                        logs.get(position)
                )
        );
        if(position != selected)
            holder.editButton.setVisibility(View.GONE);
        else
            holder.editButton.setVisibility(View.VISIBLE);
        holder.amount.setVisibility(View.VISIBLE);
        holder.amount.setText(
                formatAmount(
                        logs.get(position)
                )
        );
        holder.amount.setTextColor(
                positive ? context.getResources().getColor(R.color.green) :
                context.getResources().getColor(R.color.red)
        );
        int padding = holder.amount.getPaddingTop();
        if(logs.get(position).hasDesc()) {
            holder.description.setText(logs.get(position).getNote());
            holder.description.setVisibility(View.VISIBLE);
            holder.amount.setPadding(padding, padding, padding, 0);
            holder.timeStamp.setPadding(padding, padding, padding, 0);
        } else{
            holder.description.setVisibility(View.GONE);
            holder.amount.setPadding(padding, padding, padding, padding);
            holder.timeStamp.setPadding(padding, padding, padding, padding);
        }
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView timeStamp, amount, description;
        Button editButton;

        MyViewHolder(@NonNull final View itemView, final EditBtnClickListener editBtnClickListener) {
            super(itemView);
            timeStamp = itemView.findViewById(R.id.time);
            amount = itemView.findViewById(R.id.amount);
            description = itemView.findViewById(R.id.description);
            editButton = itemView.findViewById(R.id.buttonEdit);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editBtnClickListener.onEditClickedAt(logs.size()-1-selected);
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(editBtn != null) {
                        editBtn.animate()
                                .alpha(0f)
                                .translationX(amount.getWidth())
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(selected == getAdapterPosition()) {
                                            selected = -1;
                                        } else {
                                            animateSelected();
                                        }
                                    }
                                })
                                .start();
                        editBtn = null;
                    } else {
                        animateSelected();
                    }

                }
            });
        }

        private void animateSelected() {
            selected = getAdapterPosition();
            editBtn = editButton;
            editBtn.setVisibility(View.VISIBLE);
            editBtn.animate()
                    .alpha(1f)
                    .translationX(-amount.getWidth())
                    .start();
        }
    }

    public boolean checkIfFirstLogOfMonth(int position) {
        return position == 0 || logs.get(position).getDay() != logs.get(position-1).getDay();
    }

    public String getHeaderText(int position) {
        if(logs.size() == 0) return "";
        return getHeaderText(logs.get(position));
    }

    public String getHeaderText(LogModel log) {
        return getMonthFromNumber(log.getMonth()) + " " + log.getYear();
    }

    private String formatTimeStamp(LogModel log) {
        return getMonthFromNumber(log.getMonth()) + " " + getNumberWithExtension(log.getDay());
    }

    public String formatAmount(LogModel log) {
        return formatNumber(log.getAmount() * (log.isPositive() ? 1 : -1));
    }

    public String formatNumber(double number) {
        String prefix = "";
        if(number > 0)
            prefix = "+";
        else if(number < 0) {
            prefix = "-";
            number = -number;
        }
        String numberString = String.format(context.getResources().getConfiguration().locale, "%.2f", number);
        int i = numberString.length() - 6;
        int minI = number >= 0 ? 0 : 1;
        while (i > minI) {
            numberString = numberString.substring(0, i) + "," + numberString.substring(i);
            i -= 3;
        }
        return prefix + "$" + numberString;
    }

  /*  private String getTimeInPeriods(String time) {
        String[] timeSplit = time.split(":");
        int hour = Integer.parseInt(timeSplit[0]);
        if(hour >= 12){
            if(hour != 12) hour-=12;
            return hour + ":" + timeSplit[1] + "PM";
        } else {
            if(hour == 0) hour = 12;
            return hour + ":" + timeSplit[1] + "AM";
        }
    }*/

    private String getMonthFromNumber(int month) {
        String returnString = "";
        switch (month) {
            case 1:
                returnString = "January";
                break;
            case 2:
                returnString = "February";
                break;
            case 3:
                returnString = "March";
                break;
            case 4:
                returnString = "April";
                break;
            case 5:
                returnString = "May";
                break;
            case 6:
                returnString = "June";
                break;
            case 7:
                returnString = "July";
                break;
            case 8:
                returnString = "August";
                break;
            case 9:
                returnString = "September";
                break;
            case 10:
                returnString = "October";
                break;
            case 11:
                returnString = "November";
                break;
            case 12:
                returnString = "December";
                break;
        }
        return returnString;
    }

    private String getNumberWithExtension(int day) {
        int lastDigit = day - day/10*10;

        String extension = "th";
        String number = Integer.toString(day);

        if(lastDigit==1) {
            extension = "st";
        } else if(lastDigit==2) {
            extension = "nd";
        } else if(lastDigit==3) {
            extension = "rd";
        }
        return number+extension;
    }

    public interface EditBtnClickListener {
        void onEditClickedAt(int idx);
    }
}
