package com.nfrancoi.delivery.worker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

class HandlerToast {

    Handler handler = new Handler(Looper.getMainLooper());

    public HandlerToast(Context context, int resId) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, resId,
                        Toast.LENGTH_LONG).show();
            }
        }, 1000);
    }

    public HandlerToast(Context context, String message) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message,
                        Toast.LENGTH_LONG).show();
            }
        }, 1000);
    }

}
