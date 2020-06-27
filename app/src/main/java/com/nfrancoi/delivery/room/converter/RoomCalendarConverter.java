package com.nfrancoi.delivery.room.converter;

import androidx.room.TypeConverter;

import java.util.Calendar;

public class RoomCalendarConverter {

    @TypeConverter
    public static Calendar fromTimestamp(Long value) {
        if(value == null){
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(value);

        return c;
    }

    @TypeConverter
    public static Long calendarToTimestamp(Calendar c) {
        if(c == null){
            return null;
        }
        else{
            return c.getTimeInMillis();
        }
    }
}
