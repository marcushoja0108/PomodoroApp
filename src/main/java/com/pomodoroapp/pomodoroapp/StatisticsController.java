package com.pomodoroapp.pomodoroapp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javafx.scene.control.Button;
import javafx.event.ActionEvent;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class StatisticsController {
    @FXML Button mainViewSwitch;
    @FXML Label avgBreakTimeLabel;
    @FXML Label avgBreakCountLabel;
    @FXML Label avgWorkTimeLabel;

    @FXML Label totalBreakTimeLabel;
    @FXML Label totalBreakCountLabel;
    @FXML Label totalWorkTimeLabel;


    private Settings settings;
    private ArrayList<BreakTime> breakTimes;
    private AtomicInteger totalBreakTime;
    private int totalBreakCount;
    private long totalWorkTime;
    private double avgBreakTime;
    private double avgBreakCount;
    private long avgWorkTime;
    private ArrayList<String> breakDates;

    public void initialize(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try{
            FileReader reader = new FileReader("settings.json");
            settings = gson.fromJson(reader, Settings.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        if(settings == null){
            settings = new Settings(LocalTime.of(9, 0), LocalTime.of(15, 0));
        }
        loadData();
    }

    public void loadData(){
        Gson gson = new Gson();
        try{
            FileReader reader = new FileReader("breaks.json");
            Type breakTimeType = new TypeToken<ArrayList<BreakTime>>(){}.getType();
            breakTimes = gson.fromJson(reader, breakTimeType);
            if(breakTimes == null){
                breakTimes = new ArrayList<>();
            }
        }
        catch(FileNotFoundException e){
            throw new RuntimeException(e);
        }
        calcTotalBreakTime();
        calcTotalBreakCount();
        calcAverageBreakTime();
        calcAverageBreakCount();
        calcTotalWorkTime();
        calcAverageWorkTime();
    }

    //getters
    public ArrayList<BreakTime> getBreakTimes() {
        return breakTimes;
    }

    public double getAvgBreakTime() {
        return avgBreakTime;
    }

    public double getAvgBreakCount() {
        return avgBreakCount;
    }
    public int getTotalBreakCount() {
        return totalBreakCount;
    }
    public ArrayList<String> getBreakDates() {
        return breakDates;
    }
    public long getTotalWorkTime() {
        return totalWorkTime;
    }

    public long getAvgWorkTime() {
        return avgWorkTime;
    }

    //calculations
    private void calcTotalBreakTime() {
        totalBreakTime = new AtomicInteger();
        if(breakTimes.isEmpty()){
            return;
        }
        breakTimes.forEach(breakTime -> {
            totalBreakTime.addAndGet((int) breakTime.breakTimeSpanMillis);
        });

        totalBreakTimeLabel.setText(getTimeStringFromMillis(totalBreakTime.longValue()));
    }
    private void calcTotalBreakCount() {
        totalBreakCount = breakTimes.size();
        totalBreakCountLabel.setText(String.valueOf(totalBreakCount));
    }
    private void calcTotalWorkTime(){
        long potentialWorkTime = (breakDates.size() * settings.workTimeSpan);
        totalWorkTime = potentialWorkTime - totalBreakTime.longValue();
        totalWorkTimeLabel.setText(getTimeStringFromMillis(totalWorkTime));
    }
    private void calcAverageBreakTime() {
        int breaks = breakTimes.size();
        avgBreakTime = 0;
        double totalTime = totalBreakTime.doubleValue();
        if(breakTimes.isEmpty()){
            return;
        }else{
        avgBreakTime = (double) (int) totalTime / totalBreakCount;
        }
        avgBreakTimeLabel.setText(getTimeStringFromMillis((long)avgBreakTime));
    }
    private void calcAverageBreakCount() {
        breakDates = new ArrayList<>();
        breakTimes.forEach(breakTime -> {
            String breakDate = breakTime.shortPerformedBreak.substring(0, 10);
            if(breakDates.isEmpty() || !breakDates.contains(breakDate)){
                breakDates.add(breakDate);
            };
        });
        avgBreakCount = (double) totalBreakCount / (long) breakDates.size();
        avgBreakCountLabel.setText(String.format("%.3f", avgBreakCount));
        System.out.println(breakDates);
    }
    private void calcAverageWorkTime(){
        avgWorkTime = (long) (settings.workTimeSpan - avgBreakTime);
        avgWorkTimeLabel.setText(getTimeStringFromMillis(avgWorkTime));
    }

    private String getTimeStringFromMillis(long timeMillis){
        Duration duration = Duration.ofMillis(timeMillis);
        long seconds = duration.getSeconds();

        long HH = seconds / 3600;
        long MM = (seconds % 3600) / 60;
        long SS = seconds % 60;
        return String.format("%02d:%02d:%02d", HH, MM, SS);
    }

    public void switchToMainScene(ActionEvent event)  throws IOException {
        Stage stage = (Stage)mainViewSwitch.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Pomodoro Timer");
        stage.setScene(scene);
    }
}
