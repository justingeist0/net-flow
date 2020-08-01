package com.fantasma.netflow;

class DataAtTimeFrames {
    private Double positiveSum1Week, positiveSum1Month, positiveSum3Months, positiveSum6Months, positiveSum1Year, positiveSumAllTime;
    private Double negativeSum1Week, negativeSum1Month, negativeSum3Months, negativeSum6Months, negativeSum1Year, negativeSumAllTime;
    private Double averagePerDay;
    private int logs1Week, logs1Month, logs3Months, logs6Months, logs1Year, logsAllTime;

    void setDataAtTimeFrame(int currentStop, double positiveSum, double negativeSum, int logs) {
        switch (currentStop) {
            case 0:
                positiveSum1Week = positiveSum;
                negativeSum1Week = negativeSum;
                logs1Week = logs;
                break;
            case 1:
                positiveSum1Month = positiveSum;
                negativeSum1Month = negativeSum;
                logs1Month = logs;
                break;
            case 2:
                positiveSum3Months = positiveSum;
                negativeSum3Months = negativeSum;
                logs3Months = logs;
                break;
            case 3:
                positiveSum6Months = positiveSum;
                negativeSum6Months = negativeSum;
                logs6Months = logs;
                break;
            case 4:
                positiveSum1Year = positiveSum;
                negativeSum1Year = negativeSum;
                logs1Year = logs;
                break;
            case 5:
                positiveSumAllTime = positiveSum;
                negativeSumAllTime = negativeSum;
                logsAllTime = logs;
                break;
        }
    }

    void setAveragePerDay(double totalDays) {
        averagePerDay = (positiveSumAllTime-negativeSumAllTime)/totalDays;
    }

    double getPerDayAverage(){
        return averagePerDay;
    }

    private void updatePositiveCalculations(int currentStop, double amount) {

        switch (currentStop) {
            case 0:
                positiveSum1Week += amount;
                break;
            case 1:
                positiveSum1Month += amount;
                break;
            case 2:
                positiveSum3Months += amount;
                break;
            case 3:
                positiveSum6Months += amount;
                break;
            case 4:
                positiveSum1Year += amount;
                break;
            case 5:
                positiveSumAllTime += amount;
                break;
        }
    }

    void updateCalculation(boolean isPositive, int currentStop, double amount){
        if(isPositive)
            updatePositiveCalculations(currentStop, amount);
        else
            updateNegativeCalculations(currentStop, amount);
    }

    private void updateNegativeCalculations(int currentStop, double amount) {
        switch (currentStop) {
            case 0:
                negativeSum1Week += amount;
                break;
            case 1:
                negativeSum1Month += amount;
                break;
            case 2:
                negativeSum3Months += amount;
                break;
            case 3:
                negativeSum6Months += amount;
                break;
            case 4:
                negativeSum1Year += amount;
                break;
            case 5:
                negativeSumAllTime += amount;
                break;
        }
    }

    void addOrRemoveOneLogCount(int currentStop, boolean addLog) {
        int newLogs = addLog ? 1 : -1;

        switch (currentStop) {
            case 0:
                logs1Week += newLogs;
                break;
            case 1:
                logs1Month += newLogs;
                break;
            case 2:
                logs3Months += newLogs;
                break;
            case 3:
                logs6Months += newLogs;
                break;
            case 4:
                logs1Year += newLogs;
                break;
            case 5:
                logsAllTime += newLogs;
                break;
        }
    }

    int getLogs1Week() {
        return logs1Week;
    }

    int getLogs1Month() {
        return logs1Month;
    }

    int getLogs3Months() {
        return logs3Months;
    }

    int getLogs6Months() {
        return logs6Months;
    }

    int getLogs1Year() {
        return logs1Year;
    }

    int getLogsAllTime() {
        return logsAllTime;
    }

    Double getNegativeSum1Week() {
        return negativeSum1Week;
    }

    Double getPositiveSum1Week() {
        return positiveSum1Week;
    }

    Double getNegativeSum1Month() {
        return negativeSum1Month;
    }

    Double getPositiveSum1Month() {
        return positiveSum1Month;
    }

    Double getNegativeSum3Months() {
        return negativeSum3Months;
    }

    Double getPositiveSum3Months() {
        return positiveSum3Months;
    }

    Double getNegativeSum6Months() {
        return negativeSum6Months;
    }

    Double getPositiveSum6Months() {
        return positiveSum6Months;
    }

    Double getNegativeSum1Year() {
        return negativeSum1Year;
    }

    Double getPositiveSum1Year() {
        return positiveSum1Year;
    }

    Double getNegativeSumAllTime() {
        return negativeSumAllTime;
    }

    Double getPositiveSumAllTime() {
        return positiveSumAllTime;
    }

}
