package com.example;

import com.sun.javafx.scene.control.behavior.ChoiceBoxBehavior;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class MainController {

    private Connection connect;

    @FXML
    private TextField searchField;

    @FXML
    private RadioButton radioButtonArtist;

    @FXML
    private RadioButton radioButtonSong;

    @FXML
    private CheckBox checkboxReverse;

    @FXML
    private RadioButton radioButtonAlbum;

    private ToggleGroup toggleGroup;

    private ToggleGroup reverseToggle;

    @FXML
    private ChoiceBox sortingChoice;

    @FXML
    private Label labelViewInfo;

    @FXML
    private Label connectDatabase;

    @FXML
    private ListView<String> listViewResults;

    private boolean isSearchMode = false;
    private List<String> currentResults = new ArrayList<>();
    private boolean showingSearchResults = false;



    @FXML
    private Button openAlbumModalButton;

    @FXML
    public void initialize() {
        connectToDatabase();

        toggleGroup = new ToggleGroup();
        radioButtonArtist.setToggleGroup(toggleGroup);
        radioButtonSong.setToggleGroup(toggleGroup);
        radioButtonAlbum.setToggleGroup(toggleGroup);

        checkboxReverse.selectedProperty().addListener((observable, oldValue, newValue) -> {
                onOpenFetchSongs();
        });

        sortingChoice.getItems().addAll("Song Name", "Song Duration");
        sortingChoice.setValue("Song Name");

        onOpenFetchSongs(); //Load all songs on startup
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
    public void onResetViewButtonClick() {
        listViewResults.getItems().clear();
        toggleGroup.selectToggle(null);
        searchField.clear();
        showingSearchResults = false;
        currentResults.clear();
        onOpenFetchSongs();
    }



    @FXML
    private void openAlbumModalButton(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("create-album-modal.fxml"));
            Parent root = fxmlLoader.load();

            ModalController modalController = fxmlLoader.getController();
            modalController.setMainController(this);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL); // Block interaction with the main window
            modalStage.setTitle("Add new album");
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait(); // Wait until the modal is closed

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onFetchButtonClick() {
        String searchQuery = searchField.getText().toLowerCase();

        if (searchQuery.trim().isEmpty()) {
            listViewResults.getItems().clear();
            listViewResults.getItems().add("Please enter a search query.\n");
            return;
        }

        String selectedType = "";
        if (radioButtonArtist.isSelected()) {
            selectedType = "Artist";
        } else if (radioButtonSong.isSelected()) {
            selectedType = "Song";
        } else if (radioButtonAlbum.isSelected()) {
            selectedType = "Album";
        }

        if (selectedType.isEmpty()) {
            listViewResults.getItems().clear();
            listViewResults.getItems().add("Please select a search option (Artist, Song, or Album).\n");
            return;
        }

        String selectSQL = "";

        if (selectedType.equals("Artist")) {
            selectSQL = "SELECT s.sname, a.aname, s.album, s.length FROM Song s " +
                    "INNER JOIN Performs p ON s.sid = p.sid " +
                    "INNER JOIN Artist a ON p.aid = a.aid " +
                    "WHERE a.aname LIKE ?;";
        } else if (selectedType.equals("Song")) {
            selectSQL = "SELECT s.sname, a.aname, s.album, s.length FROM Song s " +
                    "INNER JOIN Performs p ON s.sid = p.sid " +
                    "INNER JOIN Artist a ON p.aid = a.aid " +
                    "WHERE s.sname LIKE ?;";
        } else if (selectedType.equals("Album")) {
            selectSQL = "SELECT s.sname, a.aname, s.album, s.length FROM Song s " +
                    "INNER JOIN Performs p ON s.sid = p.sid " +
                    "INNER JOIN Artist a ON p.aid = a.aid " +
                    "WHERE s.album LIKE ?;";
        }

        try (PreparedStatement preparedStatement = connect.prepareStatement(selectSQL)) {
            preparedStatement.setString(1, "%" + searchQuery + "%");

            try (ResultSet rs = preparedStatement.executeQuery()) {
                listViewResults.getItems().clear();
                currentResults.clear();
                showingSearchResults = true;

                listViewResults.setStyle("-fx-font-family: 'Monospaced';");
                labelViewInfo.setText(selectedType + " Search Results:\n");

                boolean resultsFound = false;
                while (rs.next()) {
                    String songName = rs.getString("sname");
                    String artist = rs.getString("aname");
                    String album = rs.getString("album");
                    String length = formatDuration(rs.getInt("length"));

                    String result = String.format("%-25s | %-20s | %-24s | %5s", songName, artist, album, length);
                    currentResults.add(result + "\n");
                    resultsFound = true;
                }

                if (!resultsFound) {
                    listViewResults.getItems().add("No results found for " + selectedType + ": " + searchQuery + "\n");
                } else {
                    applySorting();
                }
            }
        } catch (SQLException e) {
            listViewResults.getItems().clear();
            listViewResults.getItems().add("Error fetching data: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void applySorting() {
        String selectedSorting = (String) sortingChoice.getValue();
        boolean reverse = checkboxReverse.isSelected();

        Comparator<String> comparator;
        if ("Song Name".equals(selectedSorting)) {
            comparator = Comparator.comparing(s -> s.substring(0, 25).trim().toLowerCase());
        } else {
            comparator = Comparator.comparing(s -> {
                String time = s.substring(s.lastIndexOf("|") + 1).trim();
                String[] parts = time.split(":");
                return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
            });
        }

        currentResults.sort(reverse ? comparator.reversed() : comparator);

        listViewResults.getItems().setAll(currentResults);
    }

    @FXML
    public void onOpenFetchSongs() {
        if (showingSearchResults) {
            applySorting();
            return;
        }

        String selectedSorting = (String) sortingChoice.getValue();
        boolean reverse = checkboxReverse.isSelected();

        String selectSQL = "SELECT sname, aname, album, length FROM Song " +
                "NATURAL JOIN Performs NATURAL JOIN Artist";

        if ("Song Name".equals(selectedSorting)) {
            selectSQL += " ORDER BY LOWER(sname)" + (reverse ? " DESC;" : " ASC;");
        } else if ("Song Duration".equals(selectedSorting)) {
            selectSQL += " ORDER BY length" + (reverse ? " DESC;" : " ASC;");
        }

        try (Statement statement = connect.createStatement()) {

            ResultSet rs = statement.executeQuery(selectSQL);
            listViewResults.setStyle("-fx-font-family: 'Monospaced';");
            labelViewInfo.setText("Songs sorted by: " + selectedSorting);

            listViewResults.getItems().clear();

            while (rs.next()) {
                String songName = rs.getString("sname");
                String artist = rs.getString("aname");
                String album = rs.getString("album");
                int length = rs.getInt("length");

                String lengthFormatted = formatDuration(length);

                String result = String.format("%-25s | %-20s | %-24s | %5s", songName, artist, album, lengthFormatted);
                listViewResults.getItems().add(result + "\n");
            }

        } catch (SQLException e) {
            listViewResults.getItems().add("Error fetching data: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    //Helper method to convert seconds to "mm:ss"
    private String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
