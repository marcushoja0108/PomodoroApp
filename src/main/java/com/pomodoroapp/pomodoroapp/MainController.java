package com.pomodoroapp.pomodoroapp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MainController {

    @FXML private Label clockLabel;

    @FXML private TableView<BreakTime> lastTimesView;
    @FXML public TableColumn<BreakTime, String> tableBreakDates;
    @FXML public TableColumn<BreakTime, String> tableBreakTimes;

    @FXML private Label timerLabel;
    @FXML private Button timerButton;
    @FXML Button restartButton;
    @FXML Button statSceneSwitch;
    private Scene scene;
    private Parent root;
    private ArrayList<BreakTime> breakTimes;
    private long breakStart;
    private long timeStart;
    private long timeSpan;
    private boolean timer_running;

    ObservableList<BreakTime> tableList;

    AnimationTimer clock = new AnimationTimer() {
        @Override
        public void handle(long now) {
            clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
    };

    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            timeSpan = (System.currentTimeMillis() - timeStart);
            long seconds = 0;
            long minutes = 0;
            long hours = 0;

            //Kalkuleringen av tid kan gjøres enklere. Se BreakTime klasse
            hours = Math.toIntExact((timeSpan / 3600000));
            minutes = Math.toIntExact((timeSpan / 60000) % 60);
            seconds = Math.toIntExact((timeSpan/1000) % 60);
            String seconds_string = String.format("%02d", seconds);
            String minutes_string = String.format("%02d", minutes);
            String hours_string = String.format("%02d", hours);
            timerLabel.setText(hours_string + ":" + minutes_string + ":" + seconds_string);
            }
        };

    public void initialize(){
        clock.start();
        Gson gson = new Gson();
        try {
            FileReader reader = new FileReader("breaks.json");
            Type breakTimeType = new TypeToken<ArrayList<BreakTime>>(){}.getType();
            breakTimes = gson.fromJson(reader, breakTimeType);
            if(breakTimes == null){
                breakTimes = new ArrayList<>();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        tableBreakDates.setCellValueFactory(new PropertyValueFactory<BreakTime, String>("shortPerformedBreak"));
        tableBreakTimes.setCellValueFactory(new PropertyValueFactory<BreakTime, String>("breakTimeSpan"));
        tableList = FXCollections.observableArrayList(breakTimes);
        lastTimesView.setItems(tableList);
    }


    public void toggleTimer(ActionEvent event) throws InterruptedException {
        if(!timer_running){
            if(timeStart == 0){
                timeStart = System.currentTimeMillis();
                if(timeSpan == 0){
                    breakStart = timeStart;
                }
            }
            timer_running = true;
            timerButton.setText("Pause");
            timer.start();
        }
        else{
            timer_running = false;
            timerButton.setText("Start");
            timeStart = 0;
            timer.stop();
        }
    }

    public void restartTimer(ActionEvent event) throws InterruptedException {
        saveBreakTime();
        breakStart = 0;
        timeSpan = 0;
        toggleTimer(event);
    }

    public void saveBreakTime(){
        BreakTime saveBreak = new BreakTime(breakStart, timeSpan);
        System.out.println(saveBreak);
        breakTimes.add(saveBreak);
        //Update table
        tableList = FXCollections.observableArrayList(breakTimes);
        lastTimesView.setItems(tableList);

        Gson gson = new GsonBuilder()
                .setPrettyPrinting().create();

        String myJson = gson.toJson(breakTimes);
        System.out.println(myJson);

        try {
            FileWriter writer = new FileWriter("breaks.json");
            gson.toJson(breakTimes, writer);
            writer.close();
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public void switchToStatisticsScene(ActionEvent event) throws IOException{
        Stage stage = (Stage)statSceneSwitch.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("statistics-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Pomodoro Statistics");
        stage.setScene(scene);

    }
}
