package com.n256coding.Helpers;

import java.util.Calendar;
import java.util.Date;

public class DateEx extends Date {
    private DateEx() {
    }

    public static Date AddMonths(Date date, int count) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, count);
        return calendar.getTime();
    }

    public static int compareTime(Date date1, Date date2) {
        if (date1.getTime() == date2.getTime()) {
            return 0;
        } else if (date1.getTime() > date2.getTime()) {
            return 1;
        } else {
            return -1;
        }
    }

    public static boolean isPastDate(Date date){
        return date.getTime() < new Date().getTime();
    }

    public static boolean isOlderThanMonths(Date date, int month){
        Calendar calendar = Calendar.getInstance();
        calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -month);
        return date.getTime() < calendar.getTime().getTime();
    }

    public static boolean isFutureDate(Date date){
        return date.getTime() > new Date().getTime();
    }
}
