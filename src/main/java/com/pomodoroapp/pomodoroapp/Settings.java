package com.pomodoroapp.pomodoroapp;

import java.time.LocalTime;
import java.time.temporal.ChronoField;

public class Settings {

    public String fromWorkTime;
    public String toWorkTime;
    public long workTimeSpan;

    public Settings(LocalTime fromworktime, LocalTime toworktime) {
        this.fromWorkTime = fromworktime.toString();
        this.toWorkTime = toworktime.toString();
        updateWorkTimeSpan(fromworktime, toworktime);
    }

    public String getFromWorkTime() {
        return fromWorkTime;
    }
    public String getToWorkTime() {
        return toWorkTime;
    }

    public void updateWorkTimeSpan(LocalTime fromTime, LocalTime toTime) {
        long fromMill = fromTime.get(ChronoField.MILLI_OF_DAY);
        long toMill = toTime.get(ChronoField.MILLI_OF_DAY);
        workTimeSpan = toMill - fromMill;
    }

}
