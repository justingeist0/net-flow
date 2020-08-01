package com.fantasma.netflow;

import android.annotation.SuppressLint;

class LogModel {
    private Double amount;
    private boolean positive;
    private String purpose, timeStamp;
    private String ID;

    LogModel(String timeStamp, String purpose, Double amount, boolean positive, String ID) {

        //Format is XX/XX/XXXX XX:XX
        this.timeStamp = timeStamp;
        this.purpose = purpose;
        this.amount = amount;
        this.positive = positive;
        this.ID = ID;

        if(purpose == null) {
            this.purpose = "";
        }
    }

    Double getAmount() {
        return amount;
    }

    boolean isPositive() {
        return positive;
    }

    String getPurpose() {
        return purpose;
    }

    String getDay() {
        String[] date = timeStamp.split(" ")[0].split("/");
        int month = Integer.parseInt(date[1]);
        int day = Integer.parseInt(date[2]);
        return getMonthFromNumber(month) + " " + getNumberWithExtension(day);
    }

    String getDayAndTime() {
        String[] timeStampSplit = timeStamp.split(" ");
        String[] date = timeStampSplit[0].split("/");
        int month = Integer.parseInt(date[1]);
        int day = Integer.parseInt(date[2]);

        String time = "";
        if(timeStampSplit.length >= 2) {
            time = getTimeInPeriods(timeStampSplit[1]);
        }

        return getMonthFromNumber(month) + " " + getNumberWithExtension(day) + " " + time;
    }

    private String getTimeInPeriods(String time) {
        String[] timeSplit = time.split(":");
        int hour = Integer.parseInt(timeSplit[0]);
        if(hour >= 12){
            if(hour != 12) hour-=12;
            return hour + ":" + timeSplit[1] + "PM";
        } else {
            if(hour == 0) hour = 12;
            return hour + ":" + timeSplit[1] + "AM";
        }
    }

/*   public String getMonthYear() {
        String[] date = timeStamp.split(" ")[0].split("/");
        String year = date[0];
        int month = Integer.parseInt(date[1]);
        return getMonthFromNumber(month) + " " + year;
    }*/

    String getTimeStamp(){
        return timeStamp;
    }

    String getDate() {
        return timeStamp.split(" ")[0];
    }

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

    String getFormattedAmount() {
        return formatAmount(amount);
    }

    boolean hasDesc() {
        return !purpose.isEmpty();
    }

    static String formatAmount(Double amount) {
        boolean negative = false;
        if(amount < 0) {
            negative = true;
            amount = -amount;
        }

        @SuppressLint("DefaultLocale") String returnString = String.format("%.2f", amount);
        if(returnString.charAt(returnString.length()-3) != '.')
            returnString += "0";

        int count = 0;
        for(int i = returnString.indexOf("."); i > 0; i--) {
            if(count==3){
                returnString = returnString.substring(0, i) + "," + returnString.substring(i);
                count = 0;
            }
            count++;
        }

        return (negative?"-":"") + "$" + returnString;
    }

    String getID() {
        return ID;
    }
}
