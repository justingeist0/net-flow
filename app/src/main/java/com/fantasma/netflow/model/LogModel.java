package com.fantasma.netflow.model;

import android.annotation.SuppressLint;

public class LogModel {
    private Double amount;
    private boolean positive;
    private String note, timeStamp;
    private int[] date;
    private String ID;

    public LogModel(String timeStamp, String purpose, Double amount, boolean positive, String ID) {
        this.timeStamp = timeStamp;
        this.note = purpose;
        this.amount = amount;
        this.positive = positive;
        this.ID = ID;
        if(purpose == null) {
            this.note = "";
        }
        String[] dateStr = timeStamp.split(" ")[0].split("/");
        date = new int[] {
                Integer.parseInt(dateStr[0]), //Year
                Integer.parseInt(dateStr[1]), //Month
                Integer.parseInt(dateStr[2])  //Day
        };
    }

    public boolean hasDesc() {
        return !note.isEmpty();
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getID() {
        return ID;
    }

    public Double getAmount() {
        return amount;
    }

    public boolean isPositive() {
        return positive;
    }

    public String getNote() {
        return note;
    }

    public int[] getDate() { return date; }

    public int getYear() {return date[0];}

    public int getMonth() {return date[1];}

    public int getDay() { return date[2]; }

}
