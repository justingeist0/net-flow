package com.fantasma.netflow;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Guideline;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class AddLog {
    private MainActivity mainActivity;
    private ConstraintLayout mainLayout;
    private Guideline belowAddLog;
    private EditText amountText, descriptionText;
    private Button loss, gain, addLogButton, delete, duplicate;

    private String prevInput;
    private String headerLog;
    private LogModel selectedLogToEdit;
    private ConstraintSet snapToGuideline, snapToTop;

    private float percentage, percentageMovement, percentageStart;
    private int position;

    public AddLog() {
    }

    @SuppressLint("ClickableViewAccessibility")
    AddLog(final MainActivity main) {
        this.mainActivity = main;

        mainLayout = mainActivity.findViewById(R.id.mainActivity);
        addLogButton = mainActivity.findViewById(R.id.addLogButton);
        amountText = mainActivity.findViewById(R.id.addLogInput);
        descriptionText = mainActivity.findViewById(R.id.addPurposeInput);
        delete = mainActivity.findViewById(R.id.deleteButton);
        duplicate = mainActivity.findViewById(R.id.duplicateButton);
        loss = mainActivity.findViewById(R.id.buttonLoss);
        gain = mainActivity.findViewById(R.id.buttonGain);
        belowAddLog = mainActivity.findViewById(R.id.guideline_below_add_log);

        snapToGuideline = new ConstraintSet();
        snapToGuideline.clone(mainLayout);
        snapToGuideline.connect(R.id.addLogButton,ConstraintSet.BOTTOM, R.id.guideline_below_add_log, ConstraintSet.TOP,0);

        snapToTop = new ConstraintSet();
        snapToTop.clone(mainLayout);
        snapToTop.connect(R.id.addLogButton,ConstraintSet.TOP, R.id.header, ConstraintSet.TOP,0);

        snapToGuideline.applyTo(mainLayout);

        headerLog = getString(R.string.add_log_header);

        addLogButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float posRelativeToButton = event.getY(0)-v.getHeight()/2f;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    //Release on Add Log
                    float percentageDifference = Math.abs(percentageStart-percentage);
                    float heightAddLogButtonInPercent = addLogButton.getHeight()/getHeight();

                    if((percentageDifference <= heightAddLogButtonInPercent && percentage == heightAddLogButtonInPercent)
                            || addLogButton.getText().equals(getString(R.string.add_log)) && !(percentage==1 && percentageDifference <= heightAddLogButtonInPercent)) {
                        defaultAddLogText();

                        if(selectedLogToEdit == null)
                            moveAddLogToBottom();
                        else
                            reset();
                    } else {
                        setHeaderAddLog(headerLog);
                        moveAddLogToTop();
                        openKeyboard();
                    }
                    return true;
                } else if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    hideKeyboard();
                    percentageStart = percentage;
                }

                //Move Add Log button to Finger Position
                if(posRelativeToButton != 0 && event.getAction() != MotionEvent.ACTION_DOWN) {
                    percentageMovement = posRelativeToButton/getHeight();
                    percentage += percentageMovement;
                    if(posRelativeToButton < -1) {
                        setHeaderAddLog(headerLog);
                    } else if(posRelativeToButton > 1) {
                        defaultAddLogText();
                    }

                    if(percentage - addLogButton.getHeight()/getHeight() <= 0f) {
                        moveAddLogToTop();
                    } else {
                        snapToGuideline.applyTo(mainLayout);
                    }
                    if(percentage > 1) {
                        if(selectedLogToEdit == null)
                            moveAddLogToBottom();
                        else
                            reset();
                    }
                    belowAddLog.setGuidelinePercent(percentage);
                }
                return false;
            }
        });

        percentageMovement = 0;
        percentage = 1;
        belowAddLog.setGuidelinePercent(percentage);

        //Enter Amount TextView Editing Listener
        prevInput = "";
        amountText.addTextChangedListener(new TextWatcher() {
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

        //Loss and Gain Buttons
        View.OnClickListener buttonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateInputAndStore(v.getId());
            }
        };

        loss.setOnClickListener(buttonListener);
        gain.setOnClickListener(buttonListener);
        delete.setOnClickListener(buttonListener);
        duplicate.setOnClickListener(buttonListener);
    }

    private void setHeaderAddLog(String text) {
        if(text.contentEquals(addLogButton.getText())) return;

        addLogButton.setText(text);
        addLogButton.setBackgroundResource(R.color.primaryText);
        addLogButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_downward, 0, R.drawable.ic_arrow_downward, 0);
    }

    private void defaultAddLogText() {
        String defaultAddLogText = getString(R.string.add_log);

        if(defaultAddLogText.contentEquals(addLogButton.getText())) return;

        addLogButton.setBackgroundResource(R.drawable.btn_add_log);
        addLogButton.setText(defaultAddLogText);
        addLogButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_upward, 0, R.drawable.ic_arrow_upward, 0);
    }


    private void moveAddLogToTop() {
        snapToTop.applyTo(mainLayout);
        percentage = addLogButton.getHeight()/getHeight();
        belowAddLog.setGuidelinePercent(percentage);
        percentageMovement = 1;
    }

    private void moveAddLogToBottom() {
        snapToGuideline.applyTo(mainLayout);
        percentage = 1;
        belowAddLog.setGuidelinePercent(percentage);
        percentageMovement = 1;
        delete.setVisibility(View.INVISIBLE);
        duplicate.setVisibility(View.INVISIBLE);
    }

    public float getHeight() {
        return mainLayout.getHeight();
    }

    void setEditMode(int logPosition, int size) {
        position = logPosition;
        headerLog = getString(R.string.edit_log_header);
        setHeaderAddLog(headerLog);

        DatabaseHelper db = new DatabaseHelper(mainActivity);
        selectedLogToEdit = db.getLogAt(size - 1 - position);
        db.close();

        if(selectedLogToEdit.isPositive()) {
            gain.setTextColor(getColor(R.color.colorAccent));
        } else {
            loss.setTextColor(getColor(R.color.red));
        }

        delete.setVisibility(View.VISIBLE);
        duplicate.setVisibility(View.VISIBLE);
        amountText.setText(selectedLogToEdit.getFormattedAmount().substring(1));
        descriptionText.setText(selectedLogToEdit.getPurpose());
        moveAddLogToTop();
        percentageMovement = 0;
    }

    boolean closedIfOpened() {
        if(percentage == addLogButton.getHeight()/getHeight()) {
            reset();
            return true;
        }
        return false;
    }

    private void openKeyboard() {
        amountText.requestFocus();
        InputMethodManager imm = (InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void hideKeyboard() {
        EditText focused = amountText.isFocused() ? amountText : descriptionText.isFocused() ? descriptionText : null;
        if(focused==null) return;
        InputMethodManager imm = (InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert null != imm;
        imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);

        snapToGuideline.applyTo(mainLayout);
    }

    private void formatText(String input) {
        if(input.isEmpty()) return;

        boolean registerInput;
        String formatText = input;
        int select = amountText.getSelectionStart();

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

            //Goon Proof ex: Deleting '1' in 1,000,000 sets to 0
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
            amountText.setText(formatText);
            amountText.setSelection(select);
        }
    }

    private void storePrevInput(String input) {
        if(prevInput == null){
            prevInput = "ignore";
            return;
        }
        prevInput = input;
    }

    private void validateInputAndStore(int id) {
        String amountString = amountText.getText().toString().trim();
        if(amountString.isEmpty()) {
            amountText.setError(getString(R.string.empty_error));
            return;
        } else if(amountString.replaceAll("[0.]", "").isEmpty()) {
            amountText.setError(getString(R.string.zero_error));
            return;
        }
        hideKeyboard();

        String description = descriptionText.getText().toString();
        double amountNumber = Double.parseDouble(amountString.replace(",", ""));

        if(selectedLogToEdit==null) {
            LogModel log = new LogModel(getTimeStamp(), description, amountNumber, id == R.id.buttonGain, "");
            mainActivity.addLogToAdapter(log);
            addLogToDataBase(log);
        } else {
            if(id == R.id.deleteButton) {
                confirmDelete();
                return;
            } else if(id == R.id.duplicateButton) {
                LogModel log = new LogModel(getTimeStamp(), description, amountNumber, selectedLogToEdit.isPositive(), selectedLogToEdit.getID());
                mainActivity.addLogToAdapter(log);
                addLogToDataBase(log);
            } else {
                LogModel log = new LogModel(selectedLogToEdit.getTimeStamp(), description, amountNumber, id == R.id.buttonGain, selectedLogToEdit.getID());
                mainActivity.updateLogInAdapter(position, selectedLogToEdit, log);
                updateLogInDataBase(log);
            }
        }

        reset();
    }

    private void reset() {
        moveAddLogToBottom();
        amountText.setText("");
        descriptionText.setText("");
        hideKeyboard();
        defaultAddLogText();

        int color = getColor(R.color.white);
        loss.setTextColor(color);
        gain.setTextColor(color);

        headerLog = getString(R.string.add_log_header);
        if(selectedLogToEdit != null) {
            selectedLogToEdit = null;
        }
    }

    private void confirmDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle(getString(R.string.delete_confirmation_header) + " " + selectedLogToEdit.getDay());
        builder.setMessage(getString(R.string.delete_confirmation_body));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainActivity.deleteLogInAdapter(position, selectedLogToEdit);
                deleteFromDataBase();
                reset();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    private void deleteFromDataBase() {
        DatabaseHelper db = new DatabaseHelper(mainActivity);
        db.deleteLog(selectedLogToEdit.getID());
        db.close();
    }

    private void addLogToDataBase(LogModel newLog) {
        DatabaseHelper db = new DatabaseHelper(mainActivity);
        db.addOne(newLog);
        db.close();
    }

    private void updateLogInDataBase(LogModel updatedLog) {
        DatabaseHelper db = new DatabaseHelper(mainActivity);
        db.updateLog(updatedLog);
        db.close();
    }

    private String getTimeStamp() {
        return mainActivity.getTimeStamp();
    }

    private String getString(int ID) {
        return mainActivity.getString(ID);
    }

    private int getColor(int ID) {
        return mainActivity.getResources().getColor(ID);
    }

}
