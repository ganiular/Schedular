package com.example.schedular.utils;

import java.util.List;

public class Converter {
    public static String arrayToCommaSeparatedString(Object[] array){
        StringBuilder sb = new StringBuilder();

        for (int i=0; i<array.length; i++){
            sb.append(array[i]);

            if(i < array.length -1){
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
        if (hour == 0){
            return "12:" + minute + " AM";
        }else if (hour == 12){
            return "12:" + minute + " PM";
        }else {
            if (hour > 12){
                return (hour % 12) + ":" + minute + " PM";
            }else {
                return hour + ":" + minute + " AM";
            }
        }
    }
}
