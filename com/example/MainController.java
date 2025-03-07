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
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

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
    private List<String> currentResults = new ArrayList<>();
    private boolean showingSearchResults = false;
    @FXML
    private ChoiceBox genreChoice;
    @FXML
    private TabPane tabPane;

    @FXML
    public void initialize() {
        connectToDatabase();
        playlistController = new PlaylistController(connect, tabPane);
        if (tabPane == null) {
            System.out.println("Error: tabPane is null");
        }

        toggleGroup = new ToggleGroup();
        radioButtonArtist.setToggleGroup(toggleGroup);
        radioButtonSong.setToggleGroup(toggleGroup);
        radioButtonAlbum.setToggleGroup(toggleGroup);

        //Create listener to check for reverse list events
        checkboxReverse.selectedProperty().addListener((observable, oldValue, newValue) -> {
                onFetchButtonClick();
        });

        sortingChoice.getItems().addAll("Song Name", "Song Duration"); //list of possible sort bys
        sortingChoice.setValue("Song Name");

        // Load existing playlists on startup
        playlistController.loadExistingPlaylists();

        genreChoice.getItems().addAll(
                "All Genres", "Pop", "Hip-Hop", "Rap", "Country", "Reggae",
                "R&B", "Folk", "Blues", "EDM", "Classical", "Rock", "Jazz",
                "Metal", "Punk", "Latin", "K-Pop", "Other"
        );
        genreChoice.setValue("All Genres");

        //Add a listener to genreChoice to trigger fetching songs when selection changes
        genreChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            onFetchButtonClick();
        });

        //Add a listener to sortingChoice to trigger fetching songs when selection changes
        sortingChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            onFetchButtonClick();
        });

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
        checkboxReverse.setSelected(false);
        sortingChoice.setValue("Song Name");
        genreChoice.setValue("All Genres");

        onOpenFetchSongs();
    }

    @FXML
    private void insertSongButton(ActionEvent event){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("insertSong-modal.fxml"));
            Parent root = fxmlLoader.load();

            SongController songController = fxmlLoader.getController();
            songController.setMainController(this);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL); // Block interaction with the main window
            modalStage.setTitle("Insert song ");
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait(); // Wait until the modal is closed

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteSongByIDButton(ActionEvent event){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("deleteSongByID-modal.fxml"));
            Parent root = fxmlLoader.load();

            SongController songController = fxmlLoader.getController();
            songController.setMainController(this);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL); // Block interaction with the main window
            modalStage.setTitle("Delete song by id");
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait(); // Wait until the modal is closed

        } catch (IOException e) {
            e.printStackTrace();
        }
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
        String selectedGenre = (String) genreChoice.getValue();
        
        //If neither search query nor genre is selected, show a message and return early
        if (searchQuery.trim().isEmpty() && "All Genres".equals(selectedGenre)) {
            onOpenFetchSongs();
            return;
        }

        String selectedType = "";
        if(!searchQuery.trim().isEmpty()){
            if (radioButtonArtist.isSelected()) {
                selectedType = "Artist";
            } else if (radioButtonSong.isSelected()) {
                selectedType = "Song";
            } else if (radioButtonAlbum.isSelected()) {
                selectedType = "Album";
            }
            }

        if (!searchQuery.trim().isEmpty() && selectedType.isEmpty()) {
            listViewResults.getItems().clear();
            listViewResults.getItems().add("Please select a search option (Artist, Song, or Album).\n");
            return;
        }

        String selectSQL = "";
        String searchCondition = " WHERE ";
        String genreCondition = "";

        //Set the SQL query
        selectSQL = "SELECT s.sname, a.aname, s.album, s.length, s.genre FROM Song s " +
                "INNER JOIN Performs p ON s.sid = p.sid " +
                "INNER JOIN Artist a ON p.aid = a.aid ";

        //Apply search query condition if not empty
        if (!searchQuery.trim().isEmpty()) {
            if (selectedType.equals("Artist")) {
                selectSQL += searchCondition + "a.aname LIKE ?";
            } else if (selectedType.equals("Song")) {
                selectSQL += searchCondition + "s.sname LIKE ?";
            } else if (selectedType.equals("Album")) {
                selectSQL += searchCondition + "s.album LIKE ?";
            }
            searchCondition = " AND ";
        }

        //Apply genre condition if a genre other than "All Genres" is selected
        if (!"All Genres".equals(selectedGenre)) {
            selectSQL += searchCondition + "s.genre = ?";
        }

        //Apply sorting if selected
        String selectedSorting = (String) sortingChoice.getValue();
        boolean reverse = checkboxReverse.isSelected();

        if ("Song Name".equals(selectedSorting)) {
            selectSQL += " ORDER BY LOWER(s.sname)" + (reverse ? " DESC;" : " ASC;");
        } else if ("Song Duration".equals(selectedSorting)) {
            selectSQL += " ORDER BY s.length" + (reverse ? " DESC;" : " ASC;");
        }

        //Execute the query with the correct parameters
        try (PreparedStatement preparedStatement = connect.prepareStatement(selectSQL)) {
            int parameterIndex = 1;
            //Set search query parameter if not empty
            if (!searchQuery.trim().isEmpty()) {
                preparedStatement.setString(parameterIndex++, "%" + searchQuery + "%");
            }

            //Set genre parameter if not "All Genres"
            if (!"All Genres".equals(selectedGenre)) {
                preparedStatement.setString(parameterIndex, selectedGenre);
            }

            try (ResultSet rs = preparedStatement.executeQuery()) {
                listViewResults.setStyle("-fx-font-family: 'Monospaced';");
                labelViewInfo.setText(selectedType + " Search Results:");

                listViewResults.getItems().clear();
                boolean resultsFound = false;

                while (rs.next()) {
                    String songName = rs.getString("sname");
                    String artist = rs.getString("aname");
                    String album = rs.getString("album");
                    int length = rs.getInt("length");

                    String lengthFormatted = formatDuration(length);

                    String result = String.format("%-25s | %-20s | %-24s | %5s", songName, artist, album, lengthFormatted);
                    listViewResults.getItems().add(result + "\n");
                    resultsFound = true;
                }

                if (!resultsFound) {
                    listViewResults.getItems().add("No results found.\n");
                }

            }
        } catch (SQLException e) {
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

        //Get the selected genre from the genreChoice dropdown
        String selectedGenre = (String) genreChoice.getValue();

        String selectSQL = "SELECT sname, aname, album, length, genre FROM Song " +
                "NATURAL JOIN Performs NATURAL JOIN Artist";

        //Add filtering for the selected genre
        if (!"All Genres".equals(selectedGenre)) {
            selectSQL += " WHERE genre = ?";
        }

        //Apply sorting based on user choice
        if ("Song Name".equals(selectedSorting)) {
            selectSQL += " ORDER BY LOWER(sname)" + (reverse ? " DESC;" : " ASC;");
        } else if ("Song Duration".equals(selectedSorting)) {
            selectSQL += " ORDER BY length" + (reverse ? " DESC;" : " ASC;");
        }

        try (PreparedStatement preparedStatement = connect.prepareStatement(selectSQL)) {
            //If a genre other than "All Genres" is selected, set the genre parameter
            if (!"All Genres".equals(selectedGenre)) {
                preparedStatement.setString(1, selectedGenre);
            }

            try (ResultSet rs = preparedStatement.executeQuery()) {
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





    //Helper method to convert seconds to "mm:ss"
    private String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

}
