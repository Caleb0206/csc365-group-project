package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class SongController {
    private Connection connect;
    private MainController mainController; //reference to MainController

    @FXML
    private Label connectDatabase;

    @FXML
    private Button closeModalButton;
    @FXML
    private TextField deleteSongTextField;

    @FXML
    public void initialize() {
        connectToDatabase();
        // ensure at least 1 song-field is on the modal upon launching
        //addSongField(null);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.jdbc.Driver"); // Updated Driver
            connect = DriverManager.getConnection(
                    "jdbc:mysql://ambari-node5.csc.calpoly.edu/songsdatabase",
                    "songsdatabase", "coding");
            connectDatabase.setText("Connected to database!\n");
        } catch (Exception e) {
            connectDatabase.setText("Error connecting to database: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
    @FXML
    private void closeModal(ActionEvent event) {
        Stage stage = (Stage) closeModalButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void deleteSongFunc(ActionEvent event){
         String songName = deleteSongTextField.getText().trim();
        try (Statement statement = connect.createStatement()) {
            String query = String.format("DELETE FROM Song WHERE Song.sname = '%s';", songName);
            statement.executeUpdate(query);

            closeModal(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
