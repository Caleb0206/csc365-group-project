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
    private Label errorLabel;

    @FXML
    private Button closeModalButton;
    @FXML
    private TextField deleteSongTextField;
    @FXML
    private TextField deleteArtistNameField;

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
        String artistName = deleteArtistNameField.getText().trim();
        if (songName.isEmpty()) {
            errorLabel.setText("Please enter a song name and artist name.");
            errorLabel.setVisible(true);
            return;
        }

        String checkQuery = "SELECT COUNT(*) FROM Song WHERE sname = ? AND sid IN ( " +
                            "SELECT sid FROM Performs WHERE aid = (SELECT aid FROM Artist WHERE aname = ?))";

        // Query to delete the song from Song table
        String deleteQuery = "DELETE FROM Song WHERE sname = ? AND sid IN ( " +
                "SELECT sid FROM Performs WHERE aid = (SELECT aid FROM Artist WHERE aname = ?))";

        try (PreparedStatement checkStatement = connect.prepareStatement(checkQuery);
             PreparedStatement deleteStatement = connect.prepareStatement(deleteQuery)) {
            // Check if the song by the given artist exists
            checkStatement.setString(1, songName);
            checkStatement.setString(2, artistName);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) == 0) {
                // Song by this artist not found, show error
                errorLabel.setText("Song not found for the given artist.");
                errorLabel.setVisible(true);
                return;
            }

            // If found, proceed with deletion
            deleteStatement.setString(1, songName);
            deleteStatement.setString(2, artistName);
            deleteStatement.executeUpdate();

            // refresh the ListView in main controller
            if(mainController != null) {
                mainController.onOpenFetchSongs();
            }
            closeModal(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
