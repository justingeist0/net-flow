package com.fantasmaplasma.netflow.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fantasmaplasma.netflow.R
import com.fantasmaplasma.netflow.adapter.LogListAdapter
import com.fantasmaplasma.netflow.database.DatabaseHelper
import com.fantasmaplasma.netflow.database.LogModel
import com.fantasmaplasma.netflow.util.GetRandomLogs
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
//                logs = getAllLogs(context) ?: logs
                logs = GetRandomLogs.fillList(getTimeStampNow(), 100)
                //If you want to fill up list uncomment above line.
                // editing won't work because it isn't in database.
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