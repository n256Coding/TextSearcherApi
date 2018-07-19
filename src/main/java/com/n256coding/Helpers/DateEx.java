package com.n256coding.Helpers;

import java.util.Calendar;
import java.util.Date;

public class DateEx extends Date {
    Calendar calendar = Calendar.getInstance();

    public Date AddMonths(Date date, int count) {
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, count);
        return calendar.getTime();
    }

    public int compareTime(Date date1, Date date2) {
        if (date1.getTime() == date2.getTime()) {
            return 0;
        } else if (date1.getTime() > date2.getTime()) {
            return 1;
        } else {
            return -1;
        }
    }

    public boolean isPastDate(Date date){
        return date.getTime() < new Date().getTime();
    }

    public boolean isOlderThanMonths(Date date, int month){
        calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -month);
        return date.getTime() < calendar.getTime().getTime();
    }

    public boolean isFutureDate(Date date){
        return date.getTime() > new Date().getTime();
    }
}
