package com.example.schedular.utils;

import java.util.Calendar;

public class Converter {
    public static String arrayToCommaSeparatedString(Object[] array){
        StringBuilder sb = new StringBuilder();

        for (int i=0; i<array.length; i++){
            sb.append(array[i]);

            if(i < array.length - 1){
                sb.append(',');
            }
        }
        return sb.toString();
    }

    public static String durationToTimeFormat(int hour, int minute){
        hour += minute / 60;
        minute = minute % 60;
        if (hour > 0) {
            return hour + "hr " + minute + "min";
        }
        return minute + "min";
    }

    public static CharSequence timeFormatToAM_PM(int hour, int minute) {
        if (hour == 0) {
            return String.format("12:%02d AM", minute);
        } else if (hour == 12) {
            return String.format("12:%02d PM", minute);
        } else {
            if (hour > 12) {
                return String.format("%02d:%02d PM", hour % 12, minute);
            } else {
                return String.format("%02d:%02d AM", hour, minute);
            }
        }
    }

    public static CharSequence timeFormatRangeToAM_PM(int hour, int minute, int duration) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.add(Calendar.MINUTE, duration);
        int toHour = calendar.get(Calendar.HOUR_OF_DAY);
        int toMinute = calendar.get(Calendar.MINUTE);

        return timeFormatToAM_PM(hour, minute) + " - " + timeFormatToAM_PM(toHour, toMinute);
    }
}
