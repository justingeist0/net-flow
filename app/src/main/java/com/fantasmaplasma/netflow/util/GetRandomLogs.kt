package com.fantasmaplasma.netflow.util

import com.fantasmaplasma.netflow.database.LogModel
import java.util.*

object GetRandomLogs {

    /**
     * Function used for testing.
     *
     * Returns list of random logs to demonstrate a filled list.
     *
     * @param startDate The timestamp.
     * @param x Amount of logs to create.
     */
    fun fillList(startDate: String, x: Int): MutableList<LogModel> {
        val randomLogs: MutableList<LogModel> = ArrayList()
        val r = Random()
        var today = startDate
        for (i in 0 until x) {
            val purpose = if (r.nextBoolean()) "Lorem ipsum dolor sit amet" else ""
            randomLogs.add(LogModel(today, purpose, r.nextDouble() * 1000.00, r.nextInt(5) < 3, ""))
            val todayArray = today
                    .split(" ".toRegex())
                    .toTypedArray()[0]
                    .split("/".toRegex())
                    .toTypedArray()
            var year = todayArray[0].toInt()
            var month = todayArray[1].toInt()
            var day = todayArray[2].toInt()
            day -= r.nextInt(2)
            if (day <= 0) {
                month--
                if (month <= 0) {
                    year--
                    month = 12
                }
                day += getAmountOfDaysInMonth(year, month)
            }
            today = year.toString() + "/" + month + "/" + day + " " + r.nextInt(24) + ":" + r.nextInt(60)
        }
        return randomLogs
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