package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

import java.sql.*;

public class MainController {

    private Connection connect;

    @FXML
    private Button buttonFetch;

    @FXML
    private TextArea textAreaOutput;

    @FXML
    private TextField searchField;

    @FXML
    private RadioButton radioButtonArtist;

    @FXML
    private RadioButton radioButtonSong;

    @FXML
    private RadioButton radioButtonAlbum;

    private ToggleGroup toggleGroup;

    @FXML
    public void initialize() {
        connectToDatabase();

        //Initialize the ToggleGroup and assign it to the radio buttons
        toggleGroup = new ToggleGroup();
        radioButtonArtist.setToggleGroup(toggleGroup);
        radioButtonSong.setToggleGroup(toggleGroup);
        radioButtonAlbum.setToggleGroup(toggleGroup);
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.jdbc.Driver"); // Updated Driver
            connect = DriverManager.getConnection(
                    "jdbc:mysql://ambari-node5.csc.calpoly.edu/songsdatabase",
                    "songsdatabase", "coding");
            textAreaOutput.appendText("Connected to database!\n");
        } catch (Exception e) {
            textAreaOutput.appendText("Error connecting to database: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    @FXML
    public void onFetchButtonClick() {
        //Get the search query from the search field
        String searchQuery = searchField.getText().toLowerCase();

        //Validate input
        if (searchQuery.trim().isEmpty()) {
            textAreaOutput.appendText("Please enter a search query.\n");
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
            textAreaOutput.appendText("Please select a search option (Artist, Song, or Album).\n");
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
                textAreaOutput.clear();  //Clear any previous output
                textAreaOutput.appendText(selectedType + " Search Results:\n");

                //Check if any results were found
                boolean resultsFound = false;
                while (rs.next()) {
                    String result = rs.getString(column);  //Retrieve the value based on the selected type
                    textAreaOutput.appendText(result + "\n");  //Display the result in the text area
                    resultsFound = true;
                }

                if (!resultsFound) {
                    textAreaOutput.appendText("No results found for " + selectedType + ": " + searchQuery + "\n");
                }
            }
        } catch (SQLException e) {
            textAreaOutput.appendText("Error fetching data: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
}
