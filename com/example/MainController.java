package com.example;

import com.sun.javafx.scene.control.behavior.ChoiceBoxBehavior;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.util.Pair;

import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MainController {

    private Connection connect;
    private PlaylistController playlistController;
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

    @FXML
    private TabPane tabPane;


    @FXML
    public void initialize() {
        connectToDatabase();
        playlistController = new PlaylistController(connect, tabPane);
        if (tabPane == null) {
            System.out.println("Error: tabPane is null");
        }

        //Initialize the ToggleGroup and assign it to the radio buttons
        toggleGroup = new ToggleGroup();
        radioButtonArtist.setToggleGroup(toggleGroup);
        radioButtonSong.setToggleGroup(toggleGroup);
        radioButtonAlbum.setToggleGroup(toggleGroup);

        checkboxReverse.selectedProperty().addListener((observable, oldValue, newValue) -> {
            onOpenFetchSongs(); // Refresh song list when checkbox changes
        });

        Set<String> sorts = new HashSet<>();
        sorts.add("Song Name");
        sorts.add("Song Duration");

        for (String x : sorts){
            sortingChoice.getItems().addAll(x);
            sortingChoice.setValue(x);
        }

        // Load existing playlists on startup
        playlistController.loadExistingPlaylists();
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
        toggleGroup.selectToggle(null); // clear ToggleGroup's radio items
        //reverseToggle.selectToggle(null);
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
                listViewResults.setStyle("-fx-font-family: 'Monospaced';");
                labelViewInfo.setText(selectedType + " Search Results:\n");

                boolean resultsFound = false;
                while (rs.next()) {
                    String songName = rs.getString("sname");
                    String artist = rs.getString("aname");
                    String album = rs.getString("album");
                    String length = rs.getString("length");

                    String result = String.format("%-25s | %-20s | %-24s | %4s", songName, artist, album, length);
                    listViewResults.getItems().add(result + "\n");
                    resultsFound = true;
                }

                if (!resultsFound) {
                    listViewResults.getItems().add("No results found for " + selectedType + ": " + searchQuery + (checkboxReverse.isSelected() ? " asc;" : " desc;") + "\n");
                }
            }
        } catch (SQLException e) {
            listViewResults.getItems().clear();
            listViewResults.getItems().add("Error fetching data: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
    @FXML
    public void onOpenFetchSongs() {
        String selectedSorting = "";
        String orderByColumn = null;

        if (sortingChoice.getValue().equals("Song Name")) {
            selectedSorting = "Song Name";
            orderByColumn = "sname";
        } else if (sortingChoice.getValue().equals("Song Duration")){
            selectedSorting = "Song Duration";
            orderByColumn = "length";
        }

        String selectSQL = "select sname, aname, album, length from Song natural join Performs natural join Artist order by " + orderByColumn + (checkboxReverse.isSelected()? " asc;" : " desc;");

        try (Statement statement = connect.createStatement()) {

            ResultSet rs = statement.executeQuery(selectSQL);
            listViewResults.setStyle("-fx-font-family: 'Monospaced';"); // Or "Monospaced"

            labelViewInfo.setText("Songs sorted by: " + selectedSorting);
            listViewResults.getItems().clear();
            while(rs.next()) {
                String songName = rs.getString("sname");
                String artist = rs.getString("aname");
                String album = rs.getString("album");
                String length = rs.getString("length");

                String result = String.format("%-25s | %-20s | %-24s | %4s", songName, artist, album, length);
                listViewResults.getItems().add(result + "\n");
            }
        } catch (SQLException e) {
            listViewResults.getItems().add("Error fetching data: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    // Playlist Creation
    @FXML
    private void onCreatePlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Playlist");
        dialog.setHeaderText("Enter Playlist Name");
        dialog.setContentText("Playlist Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(playlistName -> {
            playlistController.insertPlaylistIntoDatabase(playlistName);  // Save to DB
            playlistController.createPlaylistTab(playlistName);          // Create a new tab
        });
    }




}
