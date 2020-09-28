package com.fantasma.netflow.util

import android.annotation.SuppressLint
import android.util.Log
import com.fantasma.netflow.model.LogModel
import java.text.SimpleDateFormat
import java.util.*

class DataAtTimeFrames {
    private val positiveSumAtTimeFrame = Array(Constant.ALL_TIME+1) {0.0}
    
    private val negativeSumAtTimeFrame = Array(Constant.ALL_TIME+1) {0.0}
    
    private var averagePerDay: Double = 0.0
    
    private val logCountAtTimeFrame = Array(Constant.ALL_TIME+1) {0}

    private val dateToday: Array<Int> = getDateToday()
    private val stops = arrayOf(get7DayAgo(), getXMonthsAgo( 1), getXMonthsAgo(3), getXMonthsAgo(6), get1YearAgo())

    private var totalDays = 1

    /**
    * Set all data at the time frames
    *
    * @param logs Logs from database.
    */
    fun setCalculations(logs: List<LogModel>) {
        var positiveSum = 0.0
        var negativeSum = 0.0
        var currentStop = 0
        var logCount = 0
        for (logModel in logs) {
            while (currentStop < stops.size && checkIfMoveToNextStop(stops[currentStop], logModel.date.toTypedArray())) {
                setDataAtTimeFrame(currentStop, positiveSum, negativeSum, logCount)
                currentStop++
            }
            if (logModel.isPositive) {
                positiveSum += logModel.amount
            } else {
                negativeSum += logModel.amount
            }
            logCount++
        }
        while (currentStop < logCountAtTimeFrame.size) {
            // Set rest of time frames
            setDataAtTimeFrame(currentStop, positiveSum, negativeSum, logCount)
            currentStop++
        }
        val earliestDate =
                if(logs.size > 0)
                    logs[logs.size-1].date.toTypedArray()
                else
                    dateToday

        totalDays = getTotalDays(earliestDate)
    }

    private fun setDataAtTimeFrame(currentStop: Int, positiveSum: Double, negativeSum: Double, logs: Int) {
        positiveSumAtTimeFrame[currentStop] = positiveSum
        negativeSumAtTimeFrame[currentStop] = negativeSum
        logCountAtTimeFrame[currentStop] = logs
    }


    /**
    * Gets projected amount for a time frame
    * */
    fun getPerDayAverage(timeFrame: Int): Double {
        averagePerDay = (positiveSumAtTimeFrame[Constant.ALL_TIME] - negativeSumAtTimeFrame[Constant.ALL_TIME]) / totalDays
        return when (timeFrame) {
            Constant.WEEK -> averagePerDay * 7.0
            Constant.MONTH -> averagePerDay * 30.4375
            Constant.QUARTER -> averagePerDay * 91.3125
            Constant.HALF -> averagePerDay * 182.625
            Constant.YEAR -> averagePerDay * 365.25
            else -> averagePerDay
        }
    }

    fun lossGainAt(currentTimeFrame: Int): Pair<Double, Double> {
        return Pair(negativeSumAtTimeFrame[currentTimeFrame], positiveSumAtTimeFrame[currentTimeFrame])
    }

    fun amountOfLogsAt(currentTimeFrame: Int): Int {
        return logCountAtTimeFrame[currentTimeFrame]
    }

    fun addNewLog(isPositive: Boolean, amount: Double) {
        if (isPositive) {
            for(i in positiveSumAtTimeFrame.indices)
                positiveSumAtTimeFrame[i] += amount
        } else {
            for(i in negativeSumAtTimeFrame.indices)
                negativeSumAtTimeFrame[i] += amount
        }
        for(i in logCountAtTimeFrame.indices)
            logCountAtTimeFrame[i]++
    }

    @SuppressLint("SimpleDateFormat")
    fun getTimeStampNow(): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")
        val date = Date()
        return dateFormat.format(date)
    }

    private fun getDateToday(): Array<Int> {
        val dateStr: List<String> = getTimeStampNow().split(" ")[0].split("/")
        return arrayOf(
                dateStr[0].toInt(),
                dateStr[1].toInt(),
                dateStr[2].toInt()
        )
    }

    private fun getTotalDays(earliestDate: Array<Int>): Int {
        val yearEarliest = earliestDate[0]
        var yearToday = dateToday[0]
        val monthEarliest = earliestDate[1]
        var monthToday = dateToday[1]
        val dayEarliest = earliestDate[2]
        val dayToday = dateToday[2]
        var totalDays = 1
        while (monthToday != monthEarliest || yearToday != yearEarliest) {
            monthToday--
            if (monthToday == 0) {
                monthToday = 12
                yearToday--
            }
            totalDays += getAmountOfDaysInMonth(yearToday, monthToday)
        }
        totalDays += dayToday - dayEarliest
        return totalDays
    }

    fun deleteLogCalculations(deletedLog: LogModel) {
        var currentStop = 0
        while(true) {
            //If it is past the date at the stop then update data at and before that stop
            if(deletedLog.isPositive)
                positiveSumAtTimeFrame[currentStop] -= deletedLog.amount
            else
                negativeSumAtTimeFrame[currentStop] -= deletedLog.amount
            logCountAtTimeFrame[currentStop]--
            currentStop++
            if(currentStop > Constant.ALL_TIME)
                break
            else if(!checkIfMoveToNextStop(stops[currentStop-1], deletedLog.date.toTypedArray()))
                currentStop = Constant.ALL_TIME
        }
    }

    fun updateLogCalculations(oldLog: LogModel, updatedLog: LogModel) {
        var currentStop = 0
        while (true) {
            if(oldLog.isPositive)
                positiveSumAtTimeFrame[currentStop] -= oldLog.amount
            else
                negativeSumAtTimeFrame[currentStop] -= oldLog.amount
            if(updatedLog.isPositive)
                positiveSumAtTimeFrame[currentStop] += updatedLog.amount
            else
                negativeSumAtTimeFrame[currentStop] += updatedLog.amount
            currentStop++
            if(currentStop > Constant.ALL_TIME)
                break
            else (!checkIfMoveToNextStop(stops[currentStop-1], updatedLog.date.toTypedArray()))
                currentStop = Constant.ALL_TIME
        }
    }


    private fun checkIfMoveToNextStop(beforeDate: Array<Int>, date: Array<Int>): Boolean {
        if(beforeDate[0] > date[0]) return true
        if(beforeDate[1] > date[1]) return true
        return beforeDate[2] <= date[1]
    }

    private fun get7DayAgo(): Array<Int> {
        var year = dateToday[0]
        var month = dateToday[1]
        var day = dateToday[2]
        day -= 7
        if (day <= 0) {
            month -= 1
            if (month == 0) {
                month = 12
                year--
            }
            day += getAmountOfDaysInMonth(year, month)
        }
        return arrayOf(year, month, day)
    }


    private fun getXMonthsAgo(months: Int): Array<Int> {
        var year = dateToday[0]
        var month = dateToday[1]
        val day = dateToday[2]
        month -= months
        if (month <= 0) {
            month += 12
            year--
        }
        return arrayOf(year, month, day)
    }

    private fun get1YearAgo(): Array<Int> {
        var year = dateToday[0]
        val month = dateToday[1]
        val day = dateToday[2]
        year -= 1
        return arrayOf(year, month, day)
    }

    private fun getAmountOfDaysInMonth(year: Int, month: Int): Int {
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            return 30
        } else if (month == 2) {
            return if (isLeapYear(year)) {
                29
            } else {
                28
            }
        }
        return 31
    }

    private fun isLeapYear(year: Int): Boolean {
        return year % 400 == 0 || year % 4 == 0 && year % 100 != 0
    }


}