package com.pomodoroapp.pomodoroapp;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class BreakTime {
    public long performedBreak;
    public String shortPerformedBreak;
    public long breakTimeSpanMillis;
    public String breakTimeSpan;

    public BreakTime(long timeStart, long timeSpan){
        this.performedBreak = timeStart;
        this.shortPerformedBreak = GetReadableDate(timeStart);
        this.breakTimeSpanMillis = timeSpan;
        this.breakTimeSpan = GetReadableTime(timeSpan);
    }

    public long getPerformedBreak() {
        return performedBreak;
    }

    public String getShortPerformedBreak(){
        return shortPerformedBreak;
    }

    public long getBreakTimeSpanMillis() {
        return breakTimeSpanMillis;
    }

    public String getBreakTimeSpan(){
        return breakTimeSpan;
    }

    private String GetReadableDate(long timeStart){
        LocalDateTime dateTime = Instant.ofEpochMilli(timeStart)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    private String GetReadableTime(long timeSpan){
        Duration duration = Duration.ofMillis(timeSpan);
        long seconds = duration.getSeconds();

        long HH = seconds / 3600;
        long MM = (seconds % 3600) / 60;
        long SS = seconds % 60;
        return String.format("%02d:%02d:%02d", HH, MM, SS);
    }
}
