package com.fantasma.netflow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static String[] stops;
    private static DataAtTimeFrames dataAtTimeFrame;
    private static List<LogModel> logs;
    private static int currentTimeFrame;
    private static double totalDays;

    private TextView total, negative, positive, logNumber, logAverage;

    private AddLog addLogScreen;
    private LogListAdapter logListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(logs == null || dataAtTimeFrame == null) {

            //Switch true to false to see the app filled with a bunch of random logs
            if(true)
                logs = getAllLogs();
            else
                logs = fillUpWithXRandomLogs(1000000); // <--- Just fills with random logs does not fill database; So edit mode does not work because lack of IDs.
                                                               // Makes 3/5th the logs positive

            dataAtTimeFrame = new DataAtTimeFrames();
            updateCalculations(logs);

            currentTimeFrame = 5;
        }

        //Log List
        RecyclerView mainLogsList = findViewById(R.id.mainLogsList);
        logListAdapter = new LogListAdapter(this, logs, isLandscape());
        mainLogsList.setAdapter(logListAdapter);
        mainLogsList.setLayoutManager(new LinearLayoutManager(this));

        if(isLandscape()) return;
        addLogScreen = new AddLog(this);

        logListAdapter.setAddLogScreen(addLogScreen);

        //Assign Views
        total = findViewById(R.id.netFlowTotal);
        negative = findViewById(R.id.netFlowNegative);
        positive = findViewById(R.id.netFlowPositive);
        Spinner timeFrameButton = findViewById(R.id.time);
        logNumber = findViewById(R.id.logCountText);
        logAverage = findViewById(R.id.logAverage);

        setCalculations();

        //Time Frame Drop Down
        final ArrayAdapter<String> timeFrames = new ArrayAdapter<>(MainActivity.this,
                R.layout.time_frame_closed, getResources().getStringArray(R.array.time_frames));
        timeFrames.setDropDownViewResource(R.layout.time_frame_open);

        timeFrameButton.setAdapter(timeFrames);
        timeFrameButton.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(currentTimeFrame!=position) {
                    currentTimeFrame = position;
                    setCalculations();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        timeFrameButton.setSelection(currentTimeFrame);
    }

    @Override
    public void onBackPressed() {
        if(!addLogScreen.closedIfOpened())
            super.onBackPressed();
    }

    public void addLogToAdapter(LogModel logModel) {
        newLogCalculation(logModel);
        setCalculations();
        dataAtTimeFrame.setAveragePerDay(totalDays);
        logListAdapter.addNewLog(logModel);
    }

    public void deleteLogInAdapter(int position, LogModel deletedLog) {
        deleteLogCalculations(deletedLog);
        setCalculations();

        logListAdapter.removeLog(position);
    }

    public void updateLogInAdapter(int position, LogModel oldLog, LogModel updatedLog) {
        updateLogCalculations(oldLog, updatedLog);
        setCalculations();

        logListAdapter.updateLog(position, updatedLog);
    }

    private void newLogCalculation(LogModel newLog) {
        int currentStop = 0; //"Past Week"
        dataAtTimeFrame.updateCalculation(newLog.isPositive(), currentStop, newLog.getAmount());
        dataAtTimeFrame.addOrRemoveOneLogCount(currentStop, true);

        currentStop = 5; //"All Time"
        dataAtTimeFrame.updateCalculation(newLog.isPositive(), currentStop, newLog.getAmount());
        dataAtTimeFrame.addOrRemoveOneLogCount(currentStop, true);
    }

    private void deleteLogCalculations(LogModel deletedLog) {
        int currentStop = 0;
        while(checkIfMoveToNextStop(stops[currentStop], deletedLog.getDate())) {
            //If it is past the date at the stop then update data at and before that stop
            dataAtTimeFrame.updateCalculation(deletedLog.isPositive(), currentStop, -deletedLog.getAmount());
            dataAtTimeFrame.addOrRemoveOneLogCount(currentStop, false);
            currentStop++;
            if(currentStop == stops.length) return;
        }
        dataAtTimeFrame.updateCalculation(deletedLog.isPositive(), currentStop, -deletedLog.getAmount());
        dataAtTimeFrame.addOrRemoveOneLogCount(currentStop, false);
        if(currentStop!=5) {
            currentStop = 5; //"All Time"
            dataAtTimeFrame.updateCalculation(deletedLog.isPositive(), currentStop, -deletedLog.getAmount());
            dataAtTimeFrame.addOrRemoveOneLogCount(currentStop, false);
        }
    }

    private void updateLogCalculations(LogModel oldLog, LogModel updatedLog) {
        int currentStop = 0;
        while(checkIfMoveToNextStop(stops[currentStop], updatedLog.getDate())) {
            //If it is past the date at the stop then update data at that stop
            dataAtTimeFrame.updateCalculation(oldLog.isPositive(), currentStop, -oldLog.getAmount()); //Out with the old
            dataAtTimeFrame.updateCalculation(updatedLog.isPositive(), currentStop, updatedLog.getAmount()); //In with the new
            currentStop++;
            if(currentStop == stops.length) return;
        }
        dataAtTimeFrame.updateCalculation(oldLog.isPositive(), currentStop, -oldLog.getAmount());
        dataAtTimeFrame.updateCalculation(updatedLog.isPositive(), currentStop, updatedLog.getAmount());
        if(currentStop!=5) {
            currentStop = 5; //"All Time"
            dataAtTimeFrame.updateCalculation(oldLog.isPositive(), currentStop, -oldLog.getAmount());
            dataAtTimeFrame.updateCalculation(updatedLog.isPositive(), currentStop, updatedLog.getAmount());
        }
    }

    private List<LogModel> getAllLogs() {
        DatabaseHelper db = new DatabaseHelper(this);
        List<LogModel> logs = db.getLogs();
        db.close();
        return logs;
    }

    private List<LogModel> fillUpWithXRandomLogs(int x) {
        List<LogModel> randomLogs = new ArrayList<>();

        Random r = new Random();

        String today = getTimeStamp();
        for(int i = 0; i<x; i++){
            String purpose = r.nextBoolean() ? "Lorem ipsum dolor sit amet" : "";
            randomLogs.add(new LogModel(today, purpose, r.nextDouble()*1000.00, r.nextInt(5) < 3, ""));

            String[] todayArray = today.split(" ")[0].split("/");
            int year = Integer.parseInt(todayArray[0]);
            int month = Integer.parseInt(todayArray[1]);
            int day = Integer.parseInt(todayArray[2]);

            day -= r.nextInt(2);
            if(day <= 0) {
                month--;
                if(month <= 0) {
                    year--;
                    month = 12;
                }
                day = getAmountOfDaysInMonth(year, month)+day;
            }
            today = year + "/" + month + "/" + day + " " + r.nextInt(24) + ":" + r.nextInt(60);
        }
        return randomLogs;
    }

    private void updateCalculations(List<LogModel> logs) {
        double positiveSum = 0.0;
        double negativeSum = 0.0;

        String dateToday = getTimeStamp().split(" ")[0];

        stops = new String[]{get7DayAgo(dateToday), getXMonthsAgo(dateToday, 1), getXMonthsAgo(dateToday, 3), getXMonthsAgo(dateToday, 6), get1YearAgo(dateToday)};
        int currentStop = 0;

        int logCount = 0;
        for(LogModel logModel: logs) {
            while(currentStop < stops.length && checkIfMoveToNextStop(stops[currentStop], logModel.getDate())) {
                dataAtTimeFrame.setDataAtTimeFrame(currentStop, positiveSum, negativeSum, logCount);
                currentStop++;
            }

            if(logModel.isPositive()) {
                positiveSum += logModel.getAmount();
            } else {
                negativeSum += logModel.getAmount();
            }
            logCount++;
        }

        for(;currentStop <= stops.length; currentStop++) { // is "<=" because we want to set "All Time" aswell
            dataAtTimeFrame.setDataAtTimeFrame(currentStop, positiveSum, negativeSum, logCount);
        }

        String earliestDate = logs.size()==0 ? dateToday : logs.get(logs.size()-1).getTimeStamp().split(" ")[0];
        totalDays = getTotalDays(earliestDate, dateToday);
    }

    private double getTotalDays(String earliestDate, String dateToday) {
        String[] earliest = earliestDate.split("/");
        String[] today = dateToday.split("/");

        int yearEarliest = Integer.parseInt(earliest[0]);
        int yearToday = Integer.parseInt(today[0]);

        int monthEarliest = Integer.parseInt(earliest[1]);
        int monthToday = Integer.parseInt(today[1]);

        int dayEarliest = Integer.parseInt(earliest[2]);
        int dayToday = Integer.parseInt(today[2]);

        double totalDays = 1;
        while(monthToday != monthEarliest || yearToday!=yearEarliest) {
            monthToday--;
            if(monthToday==0){
                monthToday = 12;
                yearToday--;
            }
            totalDays += getAmountOfDaysInMonth(yearToday, monthToday);
        }
        totalDays += dayToday-dayEarliest;
        return totalDays;
    }

    private boolean checkIfMoveToNextStop(String beforeDate, String date) {
        String[] componentsBefore = beforeDate.split("/");
        int yearMin = Integer.parseInt(componentsBefore[0]);
        int monthMin = Integer.parseInt(componentsBefore[1]);
        int dayMin = Integer.parseInt(componentsBefore[2]);

        String[] componentsDate = date.split("/");
        int year = Integer.parseInt(componentsDate[0]);
        int month = Integer.parseInt(componentsDate[1]);
        int day = Integer.parseInt(componentsDate[2]);

        if(year > yearMin) return false;
        if(month > monthMin) return false;
        return day <= dayMin;
    }

    private String get7DayAgo(String date) {
        String[] components = date.split("/");
        int year = Integer.parseInt(components[0]);
        int month = Integer.parseInt(components[1]);
        int day = Integer.parseInt(components[2]);

        day -= 7;
        if(day <= 0) {
            month-=1;
            if(month == 0) {
                month = 12;
                year--;
            }
            day = getAmountOfDaysInMonth(year, month)+day;
        }
        return year + "/" + month + "/" + day;
    }


    private String getXMonthsAgo(String date, int months) {
        String[] components = date.split("/");
        int year = Integer.parseInt(components[0]);
        int month = Integer.parseInt(components[1]);
        String day = components[2];

        month -= months;
        if(month <= 0) {
            month+=12;
            year--;
        }

        return year + "/" + month + "/" + day;
    }

    private String get1YearAgo(String date) {
        String[] components = date.split("/");
        int year = Integer.parseInt(components[0]);
        String month = components[1];
        String day = components[2];

        year -= 1;

        return year + "/" + month + "/" + day;
    }

    private int getAmountOfDaysInMonth(int year, int month) {
        if(month == 4 || month == 6 || month == 9 || month == 11) {
            return 30;
        } else if(month == 2) {
            if(isLeapYear(year)) {
                return 29;
            } else {
                return 28;
            }
        }
        return 31;
    }

    private boolean isLeapYear(int year) {
        if(year % 400 == 0) {
            return true;
        }
        return year % 4 == 0 && year % 100 != 0;
    }

    private void setCalculations() {
        double negativeAtTimeFrame = 0.0;
        double positiveAtTimeFrame = 0.0;
        String averageAtTimeFrame = "";
        int logsWithinTimeFrame = 0;

        dataAtTimeFrame.setAveragePerDay(totalDays);

        switch(currentTimeFrame) {
            case 0:
                positiveAtTimeFrame = dataAtTimeFrame.getPositiveSum1Week();
                negativeAtTimeFrame = dataAtTimeFrame.getNegativeSum1Week();
                logsWithinTimeFrame = dataAtTimeFrame.getLogs1Week();
                averageAtTimeFrame = getString(R.string.average_per_week) + " " +  LogModel.formatAmount(dataAtTimeFrame.getPerDayAverage()*7.0);
                break;
            case 1:
                positiveAtTimeFrame = dataAtTimeFrame.getPositiveSum1Month();
                negativeAtTimeFrame = dataAtTimeFrame.getNegativeSum1Month();
                logsWithinTimeFrame = dataAtTimeFrame.getLogs1Month();
                averageAtTimeFrame = getString(R.string.average_per_month) + " " + LogModel.formatAmount(dataAtTimeFrame.getPerDayAverage()*30.42);
                break;
            case 2:
                positiveAtTimeFrame = dataAtTimeFrame.getPositiveSum3Months();
                negativeAtTimeFrame = dataAtTimeFrame.getNegativeSum3Months();
                logsWithinTimeFrame = dataAtTimeFrame.getLogs3Months();
                averageAtTimeFrame = getString(R.string.average_per_3month) + " " + LogModel.formatAmount(dataAtTimeFrame.getPerDayAverage()*91.25);
                break;
            case 3:
                positiveAtTimeFrame = dataAtTimeFrame.getPositiveSum6Months();
                negativeAtTimeFrame = dataAtTimeFrame.getNegativeSum6Months();
                logsWithinTimeFrame = dataAtTimeFrame.getLogs6Months();
                averageAtTimeFrame = getString(R.string.average_per_6month) + " " + LogModel.formatAmount(dataAtTimeFrame.getPerDayAverage()*182.5);
                break;
            case 4:
                positiveAtTimeFrame = dataAtTimeFrame.getPositiveSum1Year();
                negativeAtTimeFrame = dataAtTimeFrame.getNegativeSum1Year();
                logsWithinTimeFrame = dataAtTimeFrame.getLogs1Year();
                averageAtTimeFrame = getString(R.string.average_per_year) + " " + LogModel.formatAmount(dataAtTimeFrame.getPerDayAverage()*365.0);
                break;
            case 5:
                positiveAtTimeFrame = dataAtTimeFrame.getPositiveSumAllTime();
                negativeAtTimeFrame = dataAtTimeFrame.getNegativeSumAllTime();
                logsWithinTimeFrame = dataAtTimeFrame.getLogsAllTime();
                averageAtTimeFrame = getString(R.string.average_per_day) + " " + LogModel.formatAmount(dataAtTimeFrame.getPerDayAverage());
                break;
        }

        negative.setText(LogModel.formatAmount(negativeAtTimeFrame));
        positive.setText(LogModel.formatAmount(positiveAtTimeFrame));

        double netFlow = positiveAtTimeFrame-negativeAtTimeFrame;
        if((float) netFlow < 0) {
            //Negative
            total.setTextColor(getResources().getColor(R.color.red));
            total.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_trending_down, 0,0,0);
        } else {
            //Positive Sum
            total.setTextColor(getResources().getColor(R.color.colorAccent));
            total.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_trending_up, 0,0,0);
        }
        total.setText(LogModel.formatAmount(Math.abs(netFlow)));

        String logNumberText;
        if(logsWithinTimeFrame==1) {
            logNumberText = formatNumber(logsWithinTimeFrame) + " " + getString(R.string.log);
        } else {
            logNumberText = formatNumber(logsWithinTimeFrame) + " " + getString(R.string.logs);
        }

        logNumber.setText(logNumberText);
        logAverage.setText(averageAtTimeFrame);
    }

    private String formatNumber(int number) {
        String numberString = Integer.toString(number);

        for(int i = numberString.length()-3; i > 0 ; i-=3) {
            numberString = numberString.substring(0, i) + "," + numberString.substring(i);
        }
        return numberString;
    }

    public String getTimeStamp() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.timestamp_format));
        Date date = new Date();
        return dateFormat.format(date);
    }

    public boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
}
