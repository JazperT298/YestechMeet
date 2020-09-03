package com.theyestech.yestechmeet.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeHandler {

    public static String getMessageDateDisplay(Date date) {
        DateFormat yearFormat = new SimpleDateFormat("MMM dd yy | hh:mm aa");
        DateFormat dayFormat = new SimpleDateFormat("MMM dd | hh:mm aa");
        DateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
        Calendar calendar = Calendar.getInstance();
        int day = calendar.getTime().getDay();
        int year = calendar.getTime().getYear();

        String stringDate = "";

        Date messageCreated = date;
        if (messageCreated.getYear() == year) {
            if (messageCreated.getDay() == day) {
                stringDate = timeFormat.format(messageCreated);
            } else {
                stringDate = dayFormat.format(messageCreated);
            }
        } else {
            stringDate = yearFormat.format(messageCreated);
        }

        return stringDate;
    }
}
