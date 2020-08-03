package com.nfrancoi.delivery.activity.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;


public class TextPreference extends EditTextPreference {
    public TextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextPreference(Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
        //just avoid to display the dialog
    }


}
