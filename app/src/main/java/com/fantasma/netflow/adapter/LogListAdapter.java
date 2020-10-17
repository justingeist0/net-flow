package com.fantasma.netflow.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fantasma.netflow.R;
import com.fantasma.netflow.database.LogModel;
import com.fantasma.netflow.util.Constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LogListAdapter extends RecyclerView.Adapter<LogListAdapter.LogViewHolder> {
    private int mSelected;
    private View editBtn;
    private View.OnClickListener mEditBtnClick;
    private EditBtnClickListener mEditBtnClickInterface;

    private Context context;
    private List<LogModel> logs;
    private int mCurrentTimeFrame, steps;
    private Integer[] today;
    private HashMap<Integer, String> mCache;
    private float translationX;

    public LogListAdapter(Context context, Integer[] today, final EditBtnClickListener editBtnClickListener) {
        this.context = context;
        this.today = today;
        mEditBtnClickInterface = editBtnClickListener;
        mSelected = -1;
        mEditBtnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getAlpha() == 1)
                    mEditBtnClickInterface.onEditClicked();
            }
        };
        mCurrentTimeFrame = 0;
        steps = 0;
        mCache = new HashMap<>();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_log_row, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        boolean positive = logs.get(position).isPositive();
        if(mCurrentTimeFrame == Constant.ALL_TIME) {
            holder.timeStamp.setText(
                    formatToTime(
                            logs.get(position).getTimeStamp()
                    )
            );
        } else {
            holder.timeStamp.setText(
                    formatToDate(
                            logs.get(position)
                    )
            );
        }
        holder.amount.setText(
                formatAmount(
                        logs.get(position)
                )
        );
        holder.amount.setTextColor(
                positive ? context.getResources().getColor(R.color.textGreen) :
                context.getResources().getColor(R.color.textRed)
        );
        int padding = holder.amount.getPaddingTop();
        if(logs.get(position).hasDesc()) {
            holder.description.setText(logs.get(position).getNote());
            holder.description.setVisibility(View.VISIBLE);
            holder.amount.setPadding(0, padding, 0, 0);
            holder.timeStamp.setPadding(padding, padding, padding, 0);
        } else{
            holder.description.setVisibility(View.GONE);
            holder.amount.setPadding(0, padding, 0, padding);
            holder.timeStamp.setPadding(padding, padding, padding, padding);
        }
        if (position == mSelected)
            holder.animateEditBtn();
        else
            holder.editButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public void updateTimeFrame(int currentTimeFrame) {
        mCurrentTimeFrame = currentTimeFrame;
        mCache.clear();
        notifyDataSetChanged();
    }

    public class LogViewHolder extends RecyclerView.ViewHolder {
        TextView timeStamp, amount, description;
        Button editButton;

        LogViewHolder(@NonNull final View itemView) {
            super(itemView);
            timeStamp = itemView.findViewById(R.id.time);
            amount = itemView.findViewById(R.id.amount);
            description = itemView.findViewById(R.id.description);
            editButton = itemView.findViewById(R.id.buttonEdit);
            editButton.setOnClickListener(mEditBtnClick);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(editBtn != null) {
                        editBtn.animate()
                                .translationX(-editBtn.getWidth())
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        editBtn.setVisibility(View.INVISIBLE);
                                        if(mSelected == getAdapterPosition()) {
                                            mSelected = -1;
                                            editBtn = null;
                                        } else {
                                            animateSelected();
                                        }
                                    }
                                })
                                .start();
                    } else {
                        animateSelected();
                    }
                }
            });
        }

        private void animateSelected() {
            mSelected = getAdapterPosition();
            translationX = timeStamp.getWidth()/2f-editButton.getWidth()/2f;
            animateEditBtn();
        }

        public void animateEditBtn() {
            editBtn = editButton;
            editButton.setVisibility(View.VISIBLE);
            editButton.setTranslationX(-editButton.getWidth());
            editButton.animate()
                    .translationX(translationX)
                    .start();
        }
    }

    public void setLogs(List<LogModel> updatedLogs) {
        logs = updatedLogs;
        resetLocalData();
        notifyDataSetChanged();
    }

    public int getSelectedDatabaseIdx() {
        return logs.size() - 1 - mSelected;
    }

    public int getSelected() {
        return mSelected;
    }

    public void addNewLog(LogModel logModel) {
        logs.add(0,logModel);
        resetLocalData();
        notifyItemInserted(0);
    }

    public void removeLog() {
        logs.remove(mSelected);
        resetLocalData();
        notifyItemRemoved(mSelected);
    }

    public void updateLog(LogModel newLog) {
        logs.set(mSelected, newLog);
        resetLocalData();
        notifyItemChanged(mSelected);
    }

    private void resetLocalData() {
        mCache.clear();
        if(editBtn != null) {
            editBtn.setVisibility(View.INVISIBLE);
            editBtn = null;
        }
        mSelected = -1;
    }

    public boolean checkIfHeaderNeeded(int position) {
        if(position == 0) return true;
        switch(mCurrentTimeFrame) {
            case Constant.WEEK:
                return firstOfWeekRelativeToToday(position);
            case Constant.MONTH:
                return firstOfMonth(position);
            case Constant.QUARTER:
                return firstOfQuarter(position);
            case Constant.HALF:
                return firstOfHalf(position);
            case Constant.YEAR:
                return firstOfYear(position);
            default:
                return firstOfDifferentDay(position);
        }
    }

    private boolean firstOfWeekRelativeToToday(int position) {
        if(position-1 < 0) return true;
        LogModel log = logs.get(position);
        Integer[] start = getWeekStart(log.getDate(), new Integer[]{today[0], today[1], today[2]});
        LogModel prevLog = logs.get(position-1);
        return dayWithinWeek(log.getDate(), start) != dayWithinWeek(prevLog.getDate(), start);
    }

    private Integer[] getWeekStart(Integer[] date, Integer[] start) {
        if(!dayWithinWeek(date, start)) {
            start[2] -= 7;
            if(start[2] <= 0) {
                start[1]--;
                if(start[1] < 1) {
                    start[0]--;
                    start[1] = 12;
                }
                start[2] = getAmountOfDaysInMonth(start[0], start[1]) + start[2];
            }
            steps++;
            getWeekStart(date, start);
        }
        return start;
    }

    private boolean dayWithinWeek(Integer[] date, Integer[] start) {
        int[] temp = {start[0], start[1], start[2]};
        temp[2] -= 6;
        if(temp[2] < 1) {
            temp[1]--;
            if(temp[1] < 1) {
                temp[0]--;
                temp[1] = 12;
            }
            temp[2] = getAmountOfDaysInMonth(temp[0], temp[1]) + temp[2];
            if(date[0].equals(start[0]) && date[1].equals(start[1]) && date[2] <= start[2]) return true;
            return date[0].equals(temp[0]) && date[1].equals(temp[1]) && date[2] >= temp[2];
        }
        if(temp[0] > date[0]) return false;
        if(temp[1] != date[1]) return false;
        return date[2] >= temp[2] && date[2] <= start[2];
    }

    private boolean firstOfMonth(int position) {
        if(position-1 < 0) return true;
        LogModel log = logs.get(position);
        return log.getMonth() != logs.get(position - 1).getMonth();
    }

    private boolean firstOfQuarter(int position) {
        if(position-1 < 0) return true;
        LogModel log = logs.get(position);
        Integer[] start = getQuarterStart(log.getDate(), new Integer[]{today[0], today[1]});
        LogModel prevLog = logs.get(position - 1);
        return monthWithinQuarter(log.getDate(), start) != monthWithinQuarter(prevLog.getDate(), start);
    }

    private Integer[] getQuarterStart(Integer[] date, Integer[] start) {
        if(!monthWithinQuarter(date, start)) {
            start[1] -= 3;
            if(start[1] < 1) {
                start[0]--;
                start[1] = 12 + start[1];
            }
            steps = 0;
            getQuarterStart(date, start);
        }
        return start;
    }

    private boolean monthWithinQuarter(Integer[] date, Integer[] start) {
        if(date[1] >= start[1]-2 && date[1] <= start[1] && date[0].equals(start[0])) return true;
        return start[1] <= 2 &&
                (date[1] >= 12 + start[1]-2 && date[0] == start[0]-1) ||
                (date[1] <= start[1] && date[0].equals(start[0]));
    }

    private boolean firstOfHalf(int position) {
        if(position-1 < 0) return true;
        LogModel log = logs.get(position);
        Integer[] start = getHalfStart(log.getDate(), new Integer[]{today[0], today[1]});
        LogModel prevLog = logs.get(position - 1);
        return monthWithinHalf(log.getDate(), start) != monthWithinHalf(prevLog.getDate(), start);
    }

    private Integer[] getHalfStart(Integer[] date, Integer[] start) {
        if(!monthWithinHalf(date, start)) {
            start[1] -= 6;
            if(start[1] < 1) {
                start[1] = 12 + start[1];
                start[0]--;
            }
            steps++;
            getHalfStart(date, start);
        }
        return start;
    }

    private boolean monthWithinHalf(Integer[] month, Integer[] start) {
        if(start[1] - 5 > 0)
            return month[1] >= start[1] - 5 && month[1] <= start[1] && month[0].equals(start[0]);
        return (month[1] >= 12 + start[1]-5 && month[0] == start[0] - 1) ||
                (month[1] <= start[1] && month[0].equals(start[0]));

    }

    private boolean firstOfYear(int position) {
        if(position-1 < 0) return true;
        return logs.get(position).getYear() != logs.get(position - 1).getYear();
    }

    private boolean firstOfDifferentDay(int position) {
        if(position-1 < 0) return true;
        return !Arrays.equals(logs.get(position).getDate(), logs.get(position - 1).getDate());
    }

    public String getHeaderText(int position) {
        switch(mCurrentTimeFrame) {
            case Constant.WEEK:
                return getWeekHeader(position);
            case Constant.MONTH:
                return getMonthHeader(position);
            case Constant.QUARTER:
                return getQuarterHeader(position);
            case Constant.HALF:
                return getHalfHeader(position);
            case Constant.YEAR:
                return getYearHeader(position);
            default:
                return getDailyHeader(position);
        }
    }

    private String getWeekHeader(int position) {
        LogModel log = logs.get(position);
        steps = 1;
        Integer[] start = getWeekStart(log.getDate(), new Integer[]{today[0], today[1], today[2]});
        if(mCache.containsKey(Arrays.hashCode(start)))
            return Objects.requireNonNull(mCache.get(Arrays.hashCode(start)));
        while(position-1 >= 0) {
            if(dayWithinWeek(logs.get(position-1).getDate(), start)) {
                position--;
            } else {
                break;
            }
        }
        double total = 0.0;
        while(position+1 < logs.size()) {
            position++;
            LogModel temp = logs.get(position);
            if(dayWithinWeek(temp.getDate(), start)) {
                total += temp.getValue();
            }
        }

        String header;
        if(steps == 1)
            header = context.getResources().getQuantityString(R.plurals.week, steps, formatNumber(total));
        else
            header = context.getResources().getQuantityString(R.plurals.week, steps, steps, formatNumber(total));
        mCache.put(Arrays.hashCode(start), header);
        return header;
    }

    private String getMonthHeader(int position) {
        LogModel log = logs.get(position);
        Integer[] start = new Integer[] {log.getYear(), log.getMonth()};
        if(mCache.containsKey(Arrays.hashCode(start)))
            return Objects.requireNonNull(mCache.get(Arrays.hashCode(start)));
        while(position - 1 >= 0) {
            LogModel temp = logs.get(position-1);
            if(temp.getMonth() == log.getMonth() &&
                temp.getYear() == log.getYear()) {
                position--;
            } else {
                break;
            }
        }
        double total = 0.0;
        while(position+1 < logs.size()) {
            position++;
            LogModel temp = logs.get(position);
            if(temp.getMonth() == log.getMonth() &&
            temp.getYear() == log.getYear())
                total+= temp.getValue();
            else
                break;
        }
        String header = getMonthFromNumber(log.getMonth()) + " " + formatNumber(total);
        mCache.put(Arrays.hashCode(start), header);
        return header;
    }

    private String getQuarterHeader(int position) {
        LogModel log = logs.get(position);
        steps = 1;
        Integer[] start = getQuarterStart(log.getDate(), new Integer[]{today[0], today[1]});
        if(mCache.containsKey(Arrays.hashCode(start)))
            return Objects.requireNonNull(mCache.get(Arrays.hashCode(start)));
        while(position - 1 >= 0) {
            if(monthWithinQuarter(logs.get(position-1).getDate(), start)) {
                position--;
            } else {
                break;
            }
        }
        double total = 0.0;
        while(position < logs.size()) {
            LogModel temp = logs.get(position);
            if(monthWithinQuarter(temp.getDate(), start))
                total += temp.getValue();
            else
                break;
            position++;
        }
        String header;
        if(steps == 1)
            header = context.getResources().getQuantityString(R.plurals.quarter, steps, formatNumber(total));
        else
            header = context.getResources().getQuantityString(R.plurals.quarter, steps, steps, formatNumber(total));
        mCache.put(Arrays.hashCode(start), header);
        return header;
    }

    private String getHalfHeader(int position) {
        LogModel log = logs.get(position);
        steps = 1;
        Integer[] start = getHalfStart(log.getDate(), new Integer[]{today[0], today[1]});
        if(mCache.containsKey(Arrays.hashCode(start)))
            return Objects.requireNonNull(mCache.get(Arrays.hashCode(start)));
        while(position - 1 >= 0) {
            if(monthWithinHalf(logs.get(position-1).getDate(), start))
                position--;
            else
                break;
        }
        double total = 0.0;
        while(position < logs.size()) {
            LogModel temp = logs.get(position);
            if(monthWithinHalf(temp.getDate(), start))
                total += temp.getValue();
            else
                break;
            position++;
        }
        String header;
        if(steps == 1)
            header = context.getResources().getQuantityString(R.plurals.half, steps, formatNumber(total));
        else
            header = context.getResources().getQuantityString(R.plurals.half, steps, steps, formatNumber(total));
        mCache.put(Arrays.hashCode(start), header);
        return header;
    }

    private String getYearHeader(int position) {
        LogModel log = logs.get(position);
        Integer[] start = new Integer[]{log.getYear()};
        if(mCache.containsKey(Arrays.hashCode(start)))
            return Objects.requireNonNull(mCache.get(Arrays.hashCode(start)));
        while(position - 1 >= 0) {
            if(logs.get(position-1).getYear() == log.getYear())
                position--;
            else
                break;
        }
        double total = 0.0;
        while(position < logs.size()) {
            LogModel temp = logs.get(position);
            if(log.getYear() == temp.getYear())
                total += temp.getValue();
            else
                break;
            position++;
        }
        String header = log.getYear() + ": " + formatNumber(total);
        mCache.put(Arrays.hashCode(start), header);
        return header;
    }

    private String getDailyHeader(int position) {
        LogModel log = logs.get(position);
        if(mCache.containsKey(Arrays.hashCode(log.getDate())))
            return Objects.requireNonNull(mCache.get(Arrays.hashCode(log.getDate())));
        while(position - 1 >= 0) {
            if(Arrays.equals(logs.get(position - 1).getDate(), log.getDate()))
                position--;
            else
                break;
        }
        double total = 0.0;
        while(position < logs.size()) {
            LogModel temp = logs.get(position);
            if(Arrays.equals(temp.getDate(), log.getDate()))
                total += temp.getValue();
            else
                break;
            position++;
        }
        String header = getMonthFromNumber(log.getMonth()) + " " + log.getDay() + ": " + formatNumber(total);
        mCache.put(Arrays.hashCode(log.getDate()), header);
        return header;
    }

    public String getDeleteHeader(LogModel log) {
        return formatToDate(log);
    }

    private String formatToDate(LogModel log) {
        return getMonthFromNumber(log.getMonth()) + " " + getNumberWithExtension(log.getDay()) + ":";
    }

    public String formatAmount(LogModel log) {
        return formatNumber(log.getValue());
    }

    public String formatNumber(double number) {
        return formatNumber(number, "");
    }

    public String formatNumber(double number, String prefix) {
        if(prefix.isEmpty())
            if(number > 0)
                prefix = "+";
            else if(number < 0) {
                prefix = "-";
                number = -number;
            }
        String numberString = String.format(context.getResources().getConfiguration().locale, "%.2f", number);
        if(numberString.contains("-"))
            numberString = numberString.replace("-", "");
        int i = numberString.length() - 6;
        int minI = number >= 0 ? 0 : 1;
        while (i > minI) {
            numberString = numberString.substring(0, i) + "," + numberString.substring(i);
            i -= 3;
        }
        return prefix + "$" + numberString;
    }

    private String formatToTime(String timeStamp) {
        String[] timeSplit = timeStamp.split(" ")[1].split(":");
        int hour = Integer.parseInt(timeSplit[0]);
        if(hour >= 12){
            if(hour != 12) hour-=12;
            return hour + ":" + timeSplit[1] + "PM:";
        } else {
            if(hour == 0) hour = 12;
            return hour + ":" + timeSplit[1] + "AM:";
        }
    }

    private String getMonthFromNumber(int month) {
        return context.getResources().getStringArray(R.array.months)[month-1];
    }

    private String getNumberWithExtension(int day) {
        int lastDigit = day - day/10*10 -1;
        if(day > 9 && day < 20)
            lastDigit = -1;
        return context.getResources().getQuantityString(R.plurals.day_extension, lastDigit, day);
    }

    private int getAmountOfDaysInMonth(int year, int month) {
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            return 30;
        } else if (month == 2) {
            if (isLeapYear(year)) {
                return 29;
            } else {
               return 28;
            }
        }
        return 31;
    }

    private Boolean isLeapYear(int year) {
        return year % 400 == 0 || year % 4 == 0 && year % 100 != 0;
    }

    public interface EditBtnClickListener {
        void onEditClicked();
    }
}
