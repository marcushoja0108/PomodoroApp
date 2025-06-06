package com.pomodoroapp.pomodoroapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class LineChartController implements Initializable {
    @FXML private LineChart<?, ?> lineChart;
    @FXML Button mainViewSwitch;

    StatisticsController statisticsController;
    private ArrayList<BreakTime> breakTimes;
    private ArrayList<String> breakDates;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("statistics-view.fxml"));
        try {
            Parent root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        statisticsController = loader.getController();
        loadData();
        fillChart();

    }

    public void loadData() {
        breakTimes = statisticsController.getBreakTimes();
        breakDates = statisticsController.getBreakDates();
    }

    public void fillChart(){
        XYChart.Series series = new XYChart.Series();
        if(!breakDates.isEmpty()){
            breakDates.forEach(breakDate -> {
                List<BreakTime> filteredList = breakTimes.stream().filter(breakTime ->
                        breakTime.shortPerformedBreak.contains(breakDate)).toList();
                double dailyBreakTime = filteredList.stream().mapToDouble(breakTime -> breakTime.breakTimeSpanMillis).sum();
                double avgDailyBreakTime = dailyBreakTime / filteredList.size();

                series.getData().add(new XYChart.Data<>(breakDate, ((avgDailyBreakTime / 1000) / 60)));

            });
            lineChart.getData().add(series);
        }
    }

    public void switchToMainScene(ActionEvent event)  throws IOException {
        Stage stage = (Stage)mainViewSwitch.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Pomodoro Timer");
        stage.setScene(scene);
    }
}
