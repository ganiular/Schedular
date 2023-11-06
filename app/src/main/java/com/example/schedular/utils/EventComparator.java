package com.example.schedular.utils;

import com.example.schedular.provider.EventProvider;

import java.util.Comparator;

public class EventComparator implements Comparator<EventProvider.EventModel> {
    @Override
    public int compare(EventProvider.EventModel event1, EventProvider.EventModel event2) {
        // Compare by day
        int dayComparison = Integer.compare(event1.getDayOffset(), event2.getDayOffset());
        if (dayComparison != 0) {
            return dayComparison;
        }

        // If the days are the same, compare by hour
        int hourComparison = Integer.compare(event1.hour, event2.hour);
        if (hourComparison != 0) {
            return hourComparison;
        }

        // If the hours are the same, compare by minute
        return Integer.compare(event1.minute, event2.minute);
    }
}
