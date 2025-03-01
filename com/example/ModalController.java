package com.example;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.sql.Connection;
import java.sql.DriverManager;

public class ModalController {
    private Connection connect;
    @FXML
    private Label connectDatabase;


    @FXML
    private Button closeModalButton;
    @FXML
    private Button addSongButton;
    @FXML
    private VBox songListContainer;

    @FXML
    public void initialize() {
        // ensure at least 1 song-field is on the modal upon launching
        addSongField(null);
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
    private void addSongField(ActionEvent event) {
        HBox songRow = new HBox(10); // HBox for a row with spacing
        songRow.setPrefWidth(360);

        TextField newSongField = new TextField();
        newSongField.setPromptText("Song Name");
        newSongField.getStyleClass().add("search-bar");
        newSongField.setPrefWidth(songRow.getPrefWidth() * 0.5);

        TextField durationField = new TextField();
        durationField.setPromptText("Duration");
        durationField.getStyleClass().add("search-bar");
        durationField.setPrefWidth(songRow.getPrefWidth() * 0.1667);

        TextField genreField = new TextField();
        genreField.setPromptText("Genre");
        genreField.getStyleClass().add("search-bar");
        genreField.setPrefWidth(songRow.getPrefWidth() * 0.3333);

        songRow.getChildren().addAll(newSongField, durationField, genreField);
        songListContainer.getChildren().addAll(songRow);

    }

    @FXML
    private void submitNewAlbum() {

    }
}
