package com.fantasma.netflow.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fantasma.netflow.R
import com.fantasma.netflow.adapter.LogListAdapter
import com.fantasma.netflow.database.DatabaseHelper
import com.fantasma.netflow.database.LogModel
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class ViewModel : ViewModel() {
    private var _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage

    private var _logToEdit = MutableLiveData<LogModel?>()
    val logToEdit: LiveData<LogModel?>
        get() = _logToEdit

    private var logs = mutableListOf<LogModel>()

    var currentTimeFrame = 5

    private val uiScope = CoroutineScope(Dispatchers.Main + Job())

    fun setLogs(adapter: LogListAdapter, context: Context) {
        adapter.setLogs(logs)
        uiScope.launch {
            if (logs.isEmpty()) {
                logs = fillUpWithXRandomLogs(10000) ?: logs
            }
            adapter.setLogs(logs)
        }
    }

    private suspend fun getAllLogs(context: Context): MutableList<LogModel>? {
        return withContext(Dispatchers.IO) {
            val db = DatabaseHelper(context)
            val logs = db.logs
            db.close()
            if (logs.isEmpty()) null else logs
        }
    }

    private fun fillUpWithXRandomLogs(x: Int): MutableList<LogModel>? {
        val randomLogs: MutableList<LogModel> = ArrayList()
        val r = Random()
        var today: String =  getTimeStampNow()
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


    fun addLog(context: Context, amount: Double, note: String, isPositive: Boolean, copy: Boolean) : LogModel {
        val newLog = LogModel(getTimeStampNow(), note, amount, isPositive, "")
        uiScope.launch {
            _toastMessage.postValue(
                if(addLogToDatabase(context, newLog))
                    context.getString(
                        if(newLog.isPositive)
                            R.string.gain
                        else
                            R.string.loss
                    ) + " " +
                    context.getString(
                        if (copy)
                            R.string.copied
                        else
                            R.string.added
                    )
                else
                    context.getString(R.string.addLogError)
            )
        }
        return newLog
    }


    fun editLog(context: Context, idx: Int) {
        uiScope.launch {
            _logToEdit.postValue(
                    getLogOfId(context, idx)
            )
        }
    }

    private suspend fun getLogOfId(context: Context, idx: Int) : LogModel {
        return withContext(Dispatchers.IO) {
            val db = DatabaseHelper(context)
            val log = db.getLogAt(idx)
            db.close()
            log
        }
    }

    fun updateLog(context: Context, selectedLog: LogModel, enteredAmount: Double, enteredDescription: String, isPositive: Boolean) : LogModel {
        val updatedLog = LogModel(
                selectedLog.timeStamp,
                enteredDescription,
                enteredAmount,
                isPositive,
                selectedLog.id
        )
        uiScope.launch {
            if (updateLog(context, updatedLog))
                _toastMessage.postValue(context.getString(R.string.logUpdated))
            else
                _toastMessage.postValue(context.getString(R.string.logFailedUpdate))
        }
        return updatedLog
    }

    private suspend fun updateLog(context: Context, updatedLog: LogModel) : Boolean {
        return withContext(Dispatchers.IO) {
            val db = DatabaseHelper(context)
            val success = db.updateLog(updatedLog)
            db.close()
            success
        }
    }

    fun deleteLog(context: Context, selectedLog: LogModel) {
        uiScope.launch {
            if (deleteLog(context, selectedLog.id))
                _toastMessage.postValue(context.getString(R.string.deleteSuccess))
            else
                _toastMessage.postValue(context.getString(R.string.deleteFailed))
        }
    }

    private suspend fun deleteLog(context: Context, logID: String) : Boolean {
        return withContext(Dispatchers.IO) {
            val db = DatabaseHelper(context)
            val success = db.deleteLog(logID)
            db.close()
            success
        }
    }

    private suspend fun addLogToDatabase(context:Context, newLog: LogModel) : Boolean {
        return withContext(Dispatchers.IO) {
            val db = DatabaseHelper(context)
            val success = db.addOne(newLog)
            db.close()
            success
        }
    }


    @SuppressLint("SimpleDateFormat")
    fun getTimeStampNow(): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")
        val date = Date()
        return dateFormat.format(date)
    }

    fun getDateToday(): Array<Int> {
        val dateStr: List<String> = getTimeStampNow().split(" ")[0].split("/")
        return arrayOf(
                dateStr[0].toInt(),
                dateStr[1].toInt(),
                dateStr[2].toInt()
        )
    }


}