package com.fantasma.netflow

import com.fantasma.netflow.util.DataAtTimeFrames
import com.fantasma.netflow.util.DataAtTimeFrames.Companion.getAmountOfDaysInMonth
import org.junit.Test

class DateCalculationTest {

    @Test
    fun testTimeFrameCalculation() {
        var correct = arrayOf(2020, 9, 25)
        var start = getWeekStart(arrayOf(2020, 9, 19), arrayOf(2020, 10, 16))
        var result = correct[0] == start[0] && correct[1] == start[1] && correct[2] == start[2]
        assert(result)

        correct = arrayOf(2020, 9, 25)
        start = getWeekStart(arrayOf(2020, 9, 19), arrayOf(2020, 10, 16))
        result = correct[0] == start[0] && correct[1] == start[1] && correct[2] == start[2]
        assert(result)

        correct = arrayOf(2020, 9, 18)
        start = getWeekStart(arrayOf(2020, 9, 18), arrayOf(2020, 10, 16))
        result = correct[0] == start[0] && correct[1] == start[1] && correct[2] == start[2]
        assert(result)

        correct = arrayOf(2020, 9, 18)
        start = getWeekStart(arrayOf(2020, 9, 13), arrayOf(2020, 10, 16))
        result = correct[0] == start[0] && correct[1] == start[1] && correct[2] == start[2]
        assert(result)

        correct = arrayOf(2020, 9, 18)
        start = getWeekStart(arrayOf(2020, 9, 11), arrayOf(2020, 10, 16))
        result = correct[0] == start[0] && correct[1] == start[1] && correct[2] == start[2]
        assert(!result)

        correct = arrayOf(2020, 9, 11)
        start = getWeekStart(arrayOf(2020, 9, 10), arrayOf(2020, 10, 16))
        result = correct[0] == start[0] && correct[1] == start[1] && correct[2] == start[2]
        assert(result)

        correct = arrayOf(2020, 9, 4)
        start = getWeekStart(arrayOf(2020, 9, 4), arrayOf(2020, 10, 16))
        result = correct[0] == start[0] && correct[1] == start[1] && correct[2] == start[2]
        assert(result)

        correct = arrayOf(2020, 9, 4)
        start = getWeekStart(arrayOf(2020, 8, 30), arrayOf(2020, 10, 16))
        result = correct[0] == start[0] && correct[1] == start[1] && correct[2] == start[2]
        assert(result)

        correct = arrayOf(2020, 8, 28)
        start = getWeekStart(arrayOf(2020, 8, 28), arrayOf(2020, 10, 16))
        result = correct[0] == start[0] && correct[1] == start[1] && correct[2] == start[2]
        assert(result)
    }

    private fun getWeekStart(date: Array<Int>, start: Array<Int>): Array<Int> {
        if (!dayWithinWeek(date, start)) {
            start[2] -= 7
            if (start[2] <= 0) {
                start[1]--
                if (start[1] < 1) {
                    start[0]--
                    start[1] = 12
                }
                start[2] = getAmountOfDaysInMonth(start[0], start[1]) + start[2]
            }
            getWeekStart(date, start)
        }
        return start
    }

    private fun dayWithinWeek(date: Array<Int>, start: Array<Int>): Boolean {
        val temp = intArrayOf(start[0], start[1], start[2])
        temp[2] -= 6
        if (temp[2] < 1) {
            temp[1]--
            if (temp[1] < 1) {
                temp[0]--
                temp[1] = 12
            }
            temp[2] = getAmountOfDaysInMonth(temp[0], temp[1]) + temp[2]
            return if (date[0] == start[0] && date[1] == start[1] && date[2] <= start[2]) true else date[0] == temp[0] && date[1] == temp[1] && date[2] >= temp[2]
        }
        if (temp[0] > date[0]) return false
        return if (temp[1] != date[1]) false else date[2] >= temp[2] && date[2] <= start[2]
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