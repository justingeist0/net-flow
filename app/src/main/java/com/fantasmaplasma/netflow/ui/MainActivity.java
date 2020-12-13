package com.fantasmaplasma.netflow.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fantasmaplasma.netflow.R;
import com.fantasmaplasma.netflow.adapter.HeaderDecoration;
import com.fantasmaplasma.netflow.adapter.LogListAdapter;
import com.fantasmaplasma.netflow.adapter.TimeFrameSpinnerAdapter;
import com.fantasmaplasma.netflow.database.LogModel;
import com.fantasmaplasma.netflow.notification.RemindReceiver;
import com.fantasmaplasma.netflow.util.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String KEY_PREFERENCES = "KEY_PREFERENCES";
    private static final String KEY_TIME_FRAME = "KEY_TIME_FRAME";
    private ViewGroup logLayout, addLogLayout;
    private RecyclerView mainLogsList;
    private Button addLogBtn, gainBtn, lossBtn, copyBtn, deleteBtn;
    private EditText amountTxt, noteTxt;
    private TextView currentTimeFrame;
    private Spinner timeFrameSpinner;
    private ViewModel viewModel;
    private LogListAdapter logListAdapter;
    private boolean addLogScreenOpened;
    private String prevInput;
    private float logListLayoutHeight;
    private LogModel selectedLogToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(ViewModel.class);
        setUpLogList();
        placeViewHolderObservers();
        setUpTimeFrameDropDown();
        setUpAddLog();
        setUpNotification();
    }

    private void setUpNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    getString(R.string.channel_id),
                    getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            channel.setDescription(getString(R.string.channel_description));
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        //Set notification to go off a day from now.
        Intent remind = new Intent(this, RemindReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, remind, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long oneDay = 86_400_000;
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + oneDay,
                pendingIntent);
    }


    private void setUpLogList() {
        mainLogsList = findViewById(R.id.mainLogsList);
        logListAdapter = new LogListAdapter(this, viewModel.getDateToday(), getEditBtnListener());
        viewModel.setLogs(logListAdapter, this);
        mainLogsList.setAdapter(logListAdapter);
        mainLogsList.addItemDecoration(new HeaderDecoration(this, getMonthChecker()));
        mainLogsList.setLayoutManager(new LinearLayoutManager(this));
    }

    private LogListAdapter.EditBtnClickListener getEditBtnListener() {
        return new LogListAdapter.EditBtnClickListener() {
            @Override
            public void onEditClicked() {
                viewModel.editLog(
                    getContext(),
                    logListAdapter.getSelectedDatabaseIdx()
                );
                toggleAddLogMenu();
                copyBtn.setVisibility(View.VISIBLE);
                deleteBtn.setVisibility(View.VISIBLE);
                addLogBtn.setText(
                        getString(R.string.edit_log_header)
                );
                mainLogsList.scrollToPosition(
                        logListAdapter.getSelected()
                );
            }
        };
    }

    private Context getContext() {
        return this;
    }

    private HeaderDecoration.MonthChecker getMonthChecker() {
        return new HeaderDecoration.MonthChecker() {
            @Override
            public boolean requiresHeader(int position) {
                return logListAdapter.saveHeaderAtPosition(position);
            }
            @NotNull
            @Override
            public String getHeader(int position) {
                return logListAdapter.getHeaderText(position);
            }
        };
    }

    private void placeViewHolderObservers() {
        currentTimeFrame = findViewById(R.id.current_time_frame);

        viewModel.getToastMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(!s.isEmpty())
                    showToast(s);
            }
        });
        viewModel.getLogToEdit().observe(this, new Observer<LogModel>() {
            @Override
            public void onChanged(LogModel logModel) {
                selectedLogToEdit = logModel;
                if(selectedLogToEdit != null) {
                    prevInput ="";
                    amountTxt.setText(
                            String.format(Locale.US,"%2f", selectedLogToEdit.getAmount())
                    );
                    noteTxt.setText(selectedLogToEdit.getNote());
                }
            }
        });
    }

    private void showToast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    private void setUpTimeFrameDropDown() {
        timeFrameSpinner = findViewById(R.id.time);
        timeFrameSpinner.setAdapter(new TimeFrameSpinnerAdapter(this,
                getResources().getStringArray(R.array.time_frames))
        );
        timeFrameSpinner.setSelection(getLastTimeFrame());
        timeFrameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setCurrentTimeFrame(position);
                setTimeFrameText();
                parent.setEnabled(false);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.setEnabled(false);
            }
        });
        View currentTimeFrameDropDown = findViewById(R.id.btn_time_frame);
        currentTimeFrameDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeFrameSpinner.setEnabled(true);
                timeFrameSpinner.performClick();
            }
        });
        View gradient = findViewById(R.id.gradient_top);
        AnimationDrawable animationDrawable = (AnimationDrawable) gradient.getBackground();
        animationDrawable.setExitFadeDuration(8000);
        animationDrawable.setEnterFadeDuration(10000);
        animationDrawable.start();
    }

    private void setTimeFrameText() {
        currentTimeFrame.setText(
                getResources().getStringArray(R.array.time_frames)
                        [viewModel.getCurrentTimeFrame()]
        );
        logListAdapter.updateTimeFrame(viewModel.getCurrentTimeFrame());
        mainLogsList.invalidateItemDecorations();
    }

    private void toggleAddLogMenu() {
        addLogScreenOpened = !addLogScreenOpened;
        if(addLogScreenOpened) {
            openAddLog();
        } else {
            hideKeyboard();
            closeAddLog();
        }
    }

    private void closeAddLog() {
        ObjectAnimator btnSlide = ObjectAnimator.ofFloat(
                addLogLayout,
                View.Y,
                addLogLayout.getY(),
                logListLayoutHeight - addLogBtn.getHeight()
        );
        ObjectAnimator logLayoutFade = ObjectAnimator.ofFloat(
                logLayout,
                View.ALPHA,
                logLayout.getAlpha(),
                1f
        );
        btnSlide.start();
        logLayoutFade.start();
    }

    private void openAddLog() {
        ObjectAnimator btnSlide = ObjectAnimator.ofFloat(
                addLogLayout,
                View.Y,
                addLogLayout.getY(),
                0f
        );
        ObjectAnimator logLayoutFade = ObjectAnimator.ofFloat(
                logLayout,
                View.ALPHA,
                logLayout.getAlpha(),
                0.5f
        );
        btnSlide.start();
        logLayoutFade.start();
    }

    private void setUpAddLog() {
        addLogScreenOpened = false;
        addLogBtn = findViewById(R.id.add_log_btn);
        AnimationDrawable buttonDrawable = (AnimationDrawable)
                ((LayerDrawable) addLogBtn.getBackground()).getDrawable(0);
        buttonDrawable.setExitFadeDuration(4000);
        buttonDrawable.setEnterFadeDuration(6000);
        buttonDrawable.start();
        logLayout = findViewById(R.id.log_list_layout);
        logLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                logLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                logListLayoutHeight = logLayout.getHeight();
                addLogLayout.setY(logListLayoutHeight- addLogBtn.getHeight()); //Called once after views created
            }
        });

        amountTxt = findViewById(R.id.enter_amount_edit_txt);
        addLogLayout = findViewById(R.id.add_log_layout);
        noteTxt = findViewById(R.id.addPurposeInput);
        gainBtn = findViewById(R.id.buttonGain);
        lossBtn = findViewById(R.id.buttonLoss);
        deleteBtn = findViewById(R.id.deleteButton);
        copyBtn = findViewById(R.id.duplicateButton);

        View addLogBackground = findViewById(R.id.gradient_add_log_group);
        AnimationDrawable background = (AnimationDrawable)
                ((LayerDrawable) addLogBackground.getBackground()).getDrawable(1);
        background.setExitFadeDuration(10000);
        background.setEnterFadeDuration(8000);
        background.start();
        addLogBackground.setOnTouchListener(new View.OnTouchListener() {
            float startY = 0f;
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                    startY = event.getY();
                else if(event.getAction() == MotionEvent.ACTION_UP)
                    if(event.getY()-startY > 5)
                        toggleAddLogMenu();
                return true; // Always intercept touch event
            }
        });

        addLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                toggleAddLogMenu();
                if(selectedLogToEdit == null) {
                    if (addLogScreenOpened) {
                        amountTxt.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(amountTxt, InputMethodManager.SHOW_IMPLICIT);
                    }
                } else if(!addLogScreenOpened) {
                    addLogBtn.setText(getString(R.string.add_log_header));
                    resetAddLogScreen();
                }
            }
        });

        amountTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                storePrevInput(s.toString());
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                formatText(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        gainBtn.setTag(Constant.GAIN_BTN);
        lossBtn.setTag(Constant.LOSS_BTN);
        deleteBtn.setTag(Constant.DELETE_BTN);
        copyBtn.setTag(Constant.COPY_BTN);
        View.OnClickListener addLogBtnListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateInputAndStore((Integer)v.getTag());
            }
        };
        gainBtn.setOnClickListener(addLogBtnListener);
        lossBtn.setOnClickListener(addLogBtnListener);
        deleteBtn.setOnClickListener(addLogBtnListener);
        copyBtn.setOnClickListener(addLogBtnListener);
    }

    private void storePrevInput(String input) {
        if(prevInput == null){
            prevInput = "ignore";
            return;
        }
        prevInput = input;
    }

    /**
     * Format incoming text from amount edit text.
     *
     * @param input Amount String
     */
    private void formatText(String input) {
        if(input.isEmpty()) return;
        boolean registerInput;
        String formatText = input;
        int select = amountTxt.getSelectionStart();
        int MAX_AMOUNT = 14; //(Billion)
        if(input.contains(".")) {
            registerInput = input.indexOf(".") < MAX_AMOUNT;
        } else {
            registerInput = input.length() < MAX_AMOUNT;
            if(!registerInput && select==input.length()) {
                if(prevInput.equals("ignore")) {
                    prevInput = input.substring(0,input.length()-1);
                }
                prevInput = prevInput.substring(0, prevInput.length()-1) + input.charAt(input.length()-1);
            }
        }
        if(input.length() < prevInput.length()) {
            if(prevInput.contains(".") && !input.contains(".")) {
                prevInput = prevInput.substring(0, prevInput.indexOf("."));
                registerInput = false;
            } else {
                registerInput = true;
            }
        } else if(!registerInput && ((input.contains(".") && select > input.indexOf(".")) || !prevInput.contains(".") && input.contains(".")))
            registerInput = true;
        if(registerInput) {
            //Prepare Formatting
            formatText = formatText.replaceAll(",", "");
            //Deleting '1' in 1,000,000 sets text to 0
            if (formatText.length() >= 2 && formatText.charAt(0) == '0' && formatText.charAt(1) != '.') {
                formatText = formatText.substring(1);
            }
            //Add Commas
            int start = (formatText.contains(".") ? formatText.indexOf('.') : formatText.length());
            int count = 0;
            for (int i = start; i > 0; i--) {
                if (count == 3) {
                    if (formatText.charAt(i - 1) != ',') {
                        formatText = formatText.substring(0, i) + "," + formatText.substring(i);
                    } else {
                        i--;
                    }
                    count = 0;
                }
                count++;
            }
            select += formatText.length() - input.length();
            if (select < 0) select = 0;
            if (select - 1 > 0 && formatText.charAt(select - 1) == ',')
                select--;
            //Format Change
            if (formatText.contains(".") && formatText.indexOf('.') < formatText.length() - 3) {
                if (select == formatText.length()) {
                    formatText = formatText.substring(0, formatText.indexOf(".") + 2) + formatText.charAt(formatText.length() - 1);
                    select = formatText.length();
                } else {
                    formatText = formatText.substring(0, formatText.indexOf(".") + 3);
                }
            }
        } else {
            //Too Long
            if(!prevInput.equals("ignore")) {
                formatText = prevInput;
                if (formatText.contains(".")) {
                    select = formatText.indexOf(".")+1;
                } else {
                    select = formatText.length();
                }
                prevInput = null;
            }
        }
        if(!input.equals(formatText)) {
            amountTxt.setText(formatText);
            amountTxt.setSelection(select);
        }
    }

    private void validateInputAndStore(int id) {
        String amountString = amountTxt.getText().toString().trim();
        if(amountString.isEmpty()) {
            amountTxt.setError(getString(R.string.empty_error));
            return;
        } else if(amountString.replaceAll("[0.]", "").isEmpty()) {
            amountTxt.setError(getString(R.string.zero_error));
            return;
        }
        double amountNumber = Double.parseDouble(amountString.replace(",", ""));
        String description = noteTxt.getText().toString();
        if(selectedLogToEdit==null || id == Constant.COPY_BTN) {
            logListAdapter.addNewLog(
                    viewModel.addLog(
                            this,
                            amountNumber,
                            description,
                            selectedLogToEdit == null ? id == Constant.GAIN_BTN : selectedLogToEdit.isPositive(),
                            id == Constant.COPY_BTN
                    )
            );
            mainLogsList.scrollToPosition(0);
        } else {
            if(id == Constant.DELETE_BTN) {
                confirmDelete();
                return;
            }  else {
                logListAdapter.updateLog(
                        viewModel.updateLog(this, selectedLogToEdit, amountNumber, description, id == Constant.GAIN_BTN)
                );
            }
        }
        selectedLogToEdit = null;
        copyBtn.setVisibility(View.INVISIBLE);
        deleteBtn.setVisibility(View.INVISIBLE);
        addLogBtn.setText(
                getString(R.string.add_log_header)
        );
        hideAddLogScreen();
    }

    private void confirmDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete_confirmation_header, logListAdapter.formatMonthDay(selectedLogToEdit)));
        builder.setMessage(getString(R.string.delete_confirmation_body));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                yesPressed();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    private void yesPressed() {
        viewModel.deleteLog(this, selectedLogToEdit);
        logListAdapter.removeLog();
        hideAddLogScreen();
    }

    private void hideAddLogScreen() {
        hideKeyboard();
        resetAddLogScreen();
        toggleAddLogMenu();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it.
        if (view == null) {
            view = new View(this);
        }
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void resetAddLogScreen() {
        amountTxt.setText("");
        noteTxt.setText("");
        addLogBtn.setText(getString(R.string.add_log_header));
        deleteBtn.setVisibility(View.INVISIBLE);
        copyBtn.setVisibility(View.INVISIBLE);
        int color = ContextCompat.getColor(this, R.color.whiter);
        lossBtn.setTextColor(color);
        gainBtn.setTextColor(color);
        selectedLogToEdit = null;
    }

    @Override
    public void onBackPressed() {
        if(addLogScreenOpened)
            toggleAddLogMenu();
        else
            super.onBackPressed();
    }

    @Override
    protected void onPause() {
        setTimeFrame(viewModel.getCurrentTimeFrame());
        super.onPause();
    }

    private int getLastTimeFrame() {
        SharedPreferences prefs = getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_TIME_FRAME, 0);
    }

    private void setTimeFrame(int timeFrame) {
        SharedPreferences prefs = getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_TIME_FRAME, timeFrame);
        editor.apply();
    }
}
