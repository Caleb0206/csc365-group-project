package com.example;

import com.sun.javafx.scene.control.behavior.ChoiceBoxBehavior;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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

    private Boolean reverseSort = true;

    @FXML
    public void initialize() {
        connectToDatabase();

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
    public void onFetchButtonClick() {
        //Get the search query from the search field
        String searchQuery = searchField.getText().toLowerCase();

        //Validate input
        if (searchQuery.trim().isEmpty()) {
            listViewResults.getItems().clear();
            listViewResults.getItems().add("Please enter a search query.\n");
            return;
        }

        //Determine the selected search type
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

        //Construct the query based on selected type
        String selectSQL = "";
        String column = ""; //column to search (either 'aname', 'sname', or 'album')

        //Choose the query depending on the selected type
        if (selectedType.equals("Artist")) {
            selectSQL = "select s.sname from Song s " +
                    "inner join Performs p on s.sid = p.sid " +
                    "inner join Artist a on p.aid = a.aid " +
                    "where a.aname LIKE ?;";
            column = "sname"; // We will display song names based on artist
        } else if (selectedType.equals("Song")) {
            selectSQL = "select sname from Song where sname LIKE ?;";
            column = "sname"; // Search by song name
        } else if (selectedType.equals("Album")) {
            selectSQL = "select sname from Song where album LIKE ?;";
            column = "sname"; // Search by album name and display song names
        }

        try (PreparedStatement preparedStatement = connect.prepareStatement(selectSQL)) {
            //Set the parameter with the search query wrapped in wildcards for partial match
            preparedStatement.setString(1, "%" + searchQuery + "%");

            //Execute the query
            try (ResultSet rs = preparedStatement.executeQuery()) {
                listViewResults.getItems().clear();  //Clear any previous output
                labelViewInfo.setText(selectedType + " Search Results:\n");

                //Check if any results were found
                boolean resultsFound = false;
                while (rs.next()) {
                    String result = rs.getString(column);  //Retrieve the value based on the selected type
                    listViewResults.getItems().add(result + "\n");  //Display the result in the text area
                    resultsFound = true;
                }

                System.out.println(" mark: " + checkboxReverse);

                if (!resultsFound) {
                    listViewResults.getItems().add("No results found for " + selectedType + ": " + searchQuery + (checkboxReverse.isSelected()? " asc;" : " desc;") + "\n");
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

                String result = String.format("%-20s | %-20s | %-20s | %5s", songName, artist, album, length);
                listViewResults.getItems().add(result + "\n");
            }
        } catch (SQLException e) {
            listViewResults.getItems().add("Error fetching data: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
}
