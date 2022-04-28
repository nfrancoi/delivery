package com.nfrancoi.delivery.tools;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class StringTools {

    public static DecimalFormat PriceFormat = (new DecimalFormat("##0.00"));
    public static NumberFormat PercentageFormat = (new DecimalFormat("#0"));

    public static boolean Equals(String a, String b) {
        if (a == null && b == null) return true;

        if (a == null || b == null) return false;

        return a.equals(b);


    }

    public static boolean IsEmpty(String a) {
        if (a == null) return true;
        if ("".equals(a)) return true;

        return false;


    }
}
