package com.fantasma.netflow.view;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kotlin.Pair;

import android.content.Context;
import android.content.DialogInterface;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fantasma.netflow.R;
import com.fantasma.netflow.adapter.HeaderDecoration;
import com.fantasma.netflow.adapter.LogListAdapter;
import com.fantasma.netflow.adapter.TimeFrameSpinnerAdapter;
import com.fantasma.netflow.model.LogModel;
import com.fantasma.netflow.model.ViewModel;
import com.fantasma.netflow.util.Constant;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private ViewGroup logLayout, addLogLayout;
    private RecyclerView mainLogsList;
    private View logListBackground;
    private Button addLog, gainBtn, lossBtn, copyBtn, deleteBtn;
    private EditText amountTxt, noteTxt;
    private TextView total, negative, positive, logNumber, currentTimeFrame;
    private ImageView trendingArrowImg;
    private Spinner timeFrameSpinner;
    private ViewModel viewModel;
    private LogListAdapter logListAdapter;
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
    }

    private void setUpLogList() {
        mainLogsList = findViewById(R.id.mainLogsList);
        logListAdapter = new LogListAdapter(this, getEditBtnListener());
        viewModel.setLogs(logListAdapter, this);
        mainLogsList.setAdapter(logListAdapter);
        mainLogsList.addItemDecoration(new HeaderDecoration(this, getMonthChecker()));
        mainLogsList.setLayoutManager(new LinearLayoutManager(this));
    }

    private LogListAdapter.EditBtnClickListener getEditBtnListener() {
        return new LogListAdapter.EditBtnClickListener() {
            @Override
            public void onEditClickedAt(int idx) {
                viewModel.editLog(getContext(), idx);
                toggleAddLogMenu();
                copyBtn.setVisibility(View.VISIBLE);
                deleteBtn.setVisibility(View.VISIBLE);
                addLog.setText(
                        getString(R.string.edit_log_header)
                );
                mainLogsList.scrollToPosition(idx);
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
                return logListAdapter.checkIfFirstLogOfMonth(position);
            }

            @NotNull
            @Override
            public String getHeader(int position) {
                return logListAdapter.getHeaderText(position);
            }
        };
    }

    private void placeViewHolderObservers() {
        total = findViewById(R.id.netFlowTotal);
        negative = findViewById(R.id.netFlowNegative);
        positive = findViewById(R.id.netFlowPositive);
        logNumber = findViewById(R.id.logCountText);
        currentTimeFrame = findViewById(R.id.current_time_frame);
        trendingArrowImg = findViewById(R.id.trending_arrow_img);

        viewModel.getLossAndGain().observe(this, new Observer<Pair<Double, Double>>() {
            @Override
            public void onChanged(Pair<Double, Double> amount) {
                //First is negative, second is positive
                negative.setText(logListAdapter.formatNumber(-amount.getFirst()));
                positive.setText(logListAdapter.formatNumber(amount.getSecond()));
                double sum = amount.getSecond() - amount.getFirst();
                total.setText(logListAdapter.formatNumber(sum));
                updateTrendingImage(sum);
                currentTimeFrame.setText(
                        getResources().getStringArray(R.array.time_frames)
                        [viewModel.getCurrentTimeFrame()]
                );
            }
        });

        viewModel.getNumberOfLogs().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                logNumber.setText(
                        getResources().getQuantityString(R.plurals.totalLogsInTimeFrame, integer, integer)
                );
            }
        });

        /*viewModel.getAverage().observe(this, new Observer<Double>() {
            @Override
            public void onChanged(Double averageForTimePeriod) {
                logAverage.setText(
                        getString(R.string.average, logListAdapter.formatNumber(averageForTimePeriod))
                );
            }
        });*/

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
                            selectedLogToEdit.getAmount().toString()
                    );
                    noteTxt.setText(selectedLogToEdit.getNote());
                }
            }
        });
    }

    private void updateTrendingImage(double sum) {
        if(sum != 0) {
            trendingArrowImg.setVisibility(View.VISIBLE);
            int id;
            if(sum > 0)
                id = R.drawable.ic_trending_up;
            else
                id = R.drawable.ic_trending_down;
            trendingArrowImg.setImageDrawable(
                    ContextCompat.getDrawable(this, id)
            );
        } else {
            trendingArrowImg.setVisibility(View.GONE);
        }
    }

    private void showToast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    private void setUpTimeFrameDropDown() {
        timeFrameSpinner = findViewById(R.id.time);
        timeFrameSpinner.setAdapter(new TimeFrameSpinnerAdapter(this,
                getResources().getStringArray(R.array.time_frames))
        );
        timeFrameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.updateCalculations(position);
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
    }
    
    private void setUpAddLog() {
        addLog = findViewById(R.id.add_log_btn);
        addLogLayout = findViewById(R.id.add_log_layout);
        logListBackground = findViewById(R.id.log_list_background);
        logLayout = findViewById(R.id.log_list_layout);
        logLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                logLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                logListLayoutHeight = logLayout.getHeight();
                addLogLayout.setY(logListLayoutHeight-addLog.getHeight()); //Called once after views created
            }
        });
        setUpAddLogAnimation();
        setUpAddLogLayouts();
    }

    private void setUpAddLogAnimation() {
        addLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAddLogMenu();
                hideAddLogScreen();
            }
        });
        logListBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAddLogMenu();
            }
        });
    }

    private void toggleAddLogMenu() {
        ObjectAnimator btnSlide = ObjectAnimator.ofFloat(
                addLogLayout,
                View.Y,
                addLogLayout.getY(),
                logListLayoutHeight-addLogLayout.getY()-addLog.getHeight()
        );
        ObjectAnimator logLayoutFade = ObjectAnimator.ofFloat(
                logLayout,
                View.ALPHA,
                logLayout.getAlpha(),
                1f - logLayout.getAlpha() + 0.5f
        );
        btnSlide.start();
        logLayoutFade.start();
    }

    private void setUpAddLogLayouts() {
        amountTxt = findViewById(R.id.enter_amount_edit_txt);
        noteTxt = findViewById(R.id.addPurposeInput);

        gainBtn = findViewById(R.id.buttonGain);
        lossBtn = findViewById(R.id.buttonLoss);
        deleteBtn = findViewById(R.id.deleteButton);
        copyBtn = findViewById(R.id.duplicateButton);

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
        copyBtn.setVisibility(View.GONE);
        deleteBtn.setVisibility(View.GONE);
        addLog.setText(
                getString(R.string.add_log_header)
        );
        hideAddLogScreen();
    }

    private void confirmDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete_confirmation_header, logListAdapter.getHeaderText(selectedLogToEdit)));
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
        hideKeyboard(); //TODO bathroom
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
        addLog.setText(getString(R.string.add_log_header));
        int color = ContextCompat.getColor(this, R.color.white);
        lossBtn.setTextColor(color);
        gainBtn.setTextColor(color);
        selectedLogToEdit = null;
    }

    @Override
    public void onBackPressed() {
        if(addLog.getY() == 0)
            toggleAddLogMenu();
        else
            super.onBackPressed();
    }
}
