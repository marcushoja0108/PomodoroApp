package com.pomodoroapp.pomodoroapp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class StatisticsController {
    @FXML Button mainViewSwitch;
    @FXML Label avgBreakTimeLabel;
    @FXML Label avgBreakCountLabel;

    @FXML Label totalBreakTimeLabel;
    @FXML Label totalBreakCountLabel;


    private ArrayList<BreakTime> breakTimes;
    private AtomicInteger totalBreakTime;


    private int totalBreakCount;
    private double avgBreakTime;
    private double avgBreakCount;
    private ArrayList<String> breakDates;

    private Stage stage;
    private Scene scene;
    private Parent root;

    public void initialize(){
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
