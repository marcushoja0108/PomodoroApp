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
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;


import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

public class MainController {

    @FXML private Label clockLabel;

    @FXML private TableView<BreakTime> lastTimesView;
    @FXML public TableColumn<BreakTime, String> tableBreakDates;
    @FXML public TableColumn<BreakTime, String> tableBreakTimes;

    @FXML private Label timerLabel;
    @FXML private Button timerButton;
    @FXML Button restartButton;
    @FXML Button statSceneSwitch;
    @FXML Label messageLabel;
    @FXML private Spinner<Integer> hoursSpinner;
    @FXML private Spinner<Integer> minutesSpinner;
    @FXML private Label fromWorkTimeLabel;
    @FXML private Label toWorkTimeLabel;
    @FXML private Button confirmWorkButton;
    private Scene scene;
    private Parent root;
    private ArrayList<BreakTime> breakTimes;
    private long breakStart;
    private long timeStart;
    private long timeSpan;
    private boolean timer_running;
    private Settings settings;
    private Label editableWorkLabel;

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

            hours = Math.toIntExact((timeSpan / 3600000));
            minutes = Math.toIntExact((timeSpan / 60000) % 60);
            seconds = Math.toIntExact((timeSpan/1000) % 60);
            timerLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
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
        setTableData();
        messageLabel.setVisible(false);
        loadWorkTime();
    }

    public void setTableData(){
        tableBreakDates.setCellValueFactory(new PropertyValueFactory<BreakTime, String>("shortPerformedBreak"));
        tableBreakTimes.setCellValueFactory(new PropertyValueFactory<BreakTime, String>("breakTimeSpan"));
        tableList = FXCollections.observableArrayList(breakTimes);
        if(tableList.size() > 9){
//            tableList.subList(0,9);   Why this no work
            tableList.subList(0, (int) ((long) tableList.size() - 9)).clear();
        }
        Collections.reverse(tableList);
        lastTimesView.setItems(tableList);
    }

    public void loadWorkTime(){

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File file = new File("settings.json");
        if(file.exists()){
            System.out.println("File exists, loading settings from json.");
            try{
                FileReader reader = new FileReader("settings.json");
                settings = gson.fromJson(reader, Settings.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            if(settings == null){
                System.out.println("File exists, but is null. Constructing settings object.");
                settings = new Settings(LocalTime.of(9, 0), LocalTime.of(15, 0));
            }
        }
        else{
            settings = new Settings(LocalTime.of(9, 0), LocalTime.of(15, 0));
            System.out.println("File doesnt exist, creating new settings object.");
        }
            createSpinners();

        fromWorkTimeLabel.setText(settings.getFromWorkTime());
        fromWorkTimeLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, this::showWorkTimeControls);

        toWorkTimeLabel.setText(settings.getToWorkTime());
        toWorkTimeLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, this::showWorkTimeControls);
    }

    public void createSpinners(){
        int hoursInt = Integer.parseInt(settings.fromWorkTime.substring(0, 2));
        int minutesInt = Integer.parseInt(settings.fromWorkTime.substring(3, 5));
        SpinnerValueFactory<Integer> hoursValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0,23, hoursInt);
        hoursSpinner.setValueFactory(hoursValueFactory);
        SpinnerValueFactory<Integer> minutesValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0,59, minutesInt);
        minutesSpinner.setValueFactory(minutesValueFactory);
    }

    public void updateWorkTime(){
        LocalTime spinnerValue = LocalTime.of(hoursSpinner.getValue(), minutesSpinner.getValue());
        if(editableWorkLabel == fromWorkTimeLabel){
            settings.fromWorkTime = spinnerValue.toString();
            fromWorkTimeLabel.setText(settings.getFromWorkTime());
            settings.updateWorkTimeSpan(spinnerValue, LocalTime.parse(settings.toWorkTime));
        }
        else if (editableWorkLabel == toWorkTimeLabel) {
            settings.toWorkTime = spinnerValue.toString();
            toWorkTimeLabel.setText(settings.getToWorkTime());
            settings.updateWorkTimeSpan(LocalTime.parse(settings.fromWorkTime), spinnerValue);
        }
        writeSettingsJson();

        hoursSpinner.setVisible(false);
        minutesSpinner.setVisible(false);
        confirmWorkButton.setVisible(false);

        messageLabel.setText("Work time updated.");
        messageLabel.setVisible(true);
    }
    public void writeSettingsJson(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String settingsJson = gson.toJson(settings);
        try{
            FileWriter writer = new FileWriter("settings.json");
            gson.toJson(settings, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void showWorkTimeControls(MouseEvent event){
        messageLabel.setVisible(false);
        hoursSpinner.setVisible(true);
        minutesSpinner.setVisible(true);
        confirmWorkButton.setVisible(true);
        if(event.getSource() == fromWorkTimeLabel){
            editableWorkLabel = fromWorkTimeLabel;
        } else if (event.getSource() == toWorkTimeLabel) {
            editableWorkLabel = toWorkTimeLabel;
        }
        int hours = Integer.parseInt(editableWorkLabel.getText().substring(0, 2));
        hoursSpinner.getValueFactory().setValue(hours);
        int minutes = Integer.parseInt(editableWorkLabel.getText().substring(3, 5));
        minutesSpinner.getValueFactory().setValue(minutes);
    }

    public void toggleTimer(ActionEvent event) throws InterruptedException {
        messageLabel.setVisible(false);
        if(!timer_running){
            if(timeSpan == 0){
            breakStart = System.currentTimeMillis();
            timeStart = breakStart;
            }
            if(timeSpan > 0){
                timeStart = System.currentTimeMillis() - timeSpan;
            }
            timer.start();
            timer_running = true;
            timerButton.setText("Pause");
        }
        else{
            timer_running = false;
            timer.stop();
            timerButton.setText("Resume");
        }
    }

    public void resetTimer(ActionEvent event) throws InterruptedException {
        saveBreakTime();
        breakStart = 0;
        timeSpan = 0;
        timerButton.setText("Start");
        timerLabel.setText("00:00:00");
    }

    public void saveBreakTime(){
        BreakTime saveBreak = new BreakTime(breakStart, timeSpan);
        breakTimes.add(saveBreak);
        //Update table
        setTableData();

        Gson gson = new GsonBuilder()
                .setPrettyPrinting().create();

        try {
            FileWriter writer = new FileWriter("breaks.json");
            gson.toJson(breakTimes, writer);
            writer.close();
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }
        messageLabel.setText("Break saved!");
        messageLabel.setVisible(true);
    }

    public void switchToStatisticsScene(ActionEvent event) throws IOException{
        Stage stage = (Stage)statSceneSwitch.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("statistics-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Pomodoro Statistics");
        stage.setScene(scene);

    }
    public void switchToLineChartScene(ActionEvent event) throws IOException{
        Stage stage = (Stage)statSceneSwitch.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("lineChart-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Pomodoro Line chart");
        stage.setScene(scene);

    }
}
