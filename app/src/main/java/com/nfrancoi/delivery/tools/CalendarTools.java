package com.nfrancoi.delivery.tools;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarTools {

    public static SimpleDateFormat YYYYMMDD = new SimpleDateFormat("yyyyMMdd");
    public static SimpleDateFormat YYYYMMDDHHmmss = new SimpleDateFormat("yyyyMMddHHmmSS");

    public static SimpleDateFormat YYYYMM = new SimpleDateFormat("yyyyMM");
    public static SimpleDateFormat DDMMYYYY = new SimpleDateFormat("dd/MM/YYYY");

    public static SimpleDateFormat HHmm = new SimpleDateFormat("HH:mm");

    public static final Calendar roundByDay(@NonNull Calendar calendar) {
        Calendar calendarReturn = (Calendar) calendar.clone();
        calendarReturn.set(Calendar.HOUR_OF_DAY, 0 );
        calendarReturn.set(Calendar.MINUTE, 0);
        calendarReturn.set(Calendar.SECOND, 0);
        calendarReturn.set(Calendar.MILLISECOND, 0);

        return calendarReturn;
    }
}
