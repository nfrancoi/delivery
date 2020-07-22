package com.nfrancoi.delivery.activity.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.nfrancoi.delivery.R;

public class HorizontalNumberPicker extends LinearLayout {
    private EditText et_number;
    private NumberPickerOnFocusChangeListener numberPickerOnFocusChangeListener;

    private int min, max = Integer.MAX_VALUE;


    public HorizontalNumberPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.widget_numberpicker_horizontal, this);
        et_number = findViewById(R.id.et_number);

        //capture event when focus change
        numberPickerOnFocusChangeListener = new NumberPickerOnFocusChangeListener();
        et_number.setOnFocusChangeListener(numberPickerOnFocusChangeListener);

        //capture event from done button
        et_number.setOnKeyListener((et_numberView, keyCode, event) -> {

            if (keyCode == EditorInfo.IME_ACTION_SEARCH
                    || keyCode == EditorInfo.IME_ACTION_DONE
                    || event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (listener != null)
                    listener.onValueChange(getValue());
                return true;
            }
            // Return true if you have consumed the action, else false.
            return false;

        });


        //capture event from click button
        final Button btn_less = findViewById(R.id.btn_less);
        btn_less.setOnClickListener(new AddHandler(-1));

        final Button btn_more = findViewById(R.id.btn_more);
        btn_more.setOnClickListener(new AddHandler(1));
    }

    //
    // Value change
    //
    private OnValueChangeListener listener;

    public void setOnChangeValueListener(OnValueChangeListener listener) {
        this.listener = listener;
    }

    private class NumberPickerOnFocusChangeListener implements OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                if (listener != null)
                    listener.onValueChange(getValue());
            }
        }
    }


    /***
     * HANDLERS
     **/

    private class AddHandler implements OnClickListener {
        final int diff;

        public AddHandler(int diff) {
            this.diff = diff;
        }

        @Override
        public void onClick(View v) {
            int oldValue = getValue();
            int newValue = getValue() + diff;
            if (newValue < min) {
                newValue = min;
            } else if (newValue > max) {
                newValue = max;
            }
            setValue(newValue);
            if (listener != null)
                listener.onValueChange(getValue());

        }
    }

    //
    // Getters Setters
    //
    public int getValue() {

        try {
            final String value = et_number.getText().toString();
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            Log.e("HorizontalNumberPicker", ex.toString());
        }
        return 0;
    }

    public void setValue(final int value) {
        et_number.setText(String.valueOf(value));
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }


}