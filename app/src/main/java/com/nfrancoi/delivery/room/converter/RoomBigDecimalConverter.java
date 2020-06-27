package com.nfrancoi.delivery.room.converter;

import androidx.room.TypeConverter;

import java.math.BigDecimal;

public class RoomBigDecimalConverter {

    @TypeConverter
    public static Double bigDecimalToDouble(BigDecimal bd) {
        if (bd == null) return 0.0;
        return bd.doubleValue();
    }

    @TypeConverter
    public static BigDecimal doubleToBigDecimal(Double d) {
        if (d == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(d);
    }
}
