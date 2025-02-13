package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import java.sql.*;

public class MainController {
    private Connection connect;

    @FXML
    private Button buttonFetch;

    @FXML
    private TextArea textAreaOutput;

    @FXML
    public void initialize() {
        connectToDatabase();
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
        String query = "SELECT * FROM Artist;";
        try (Statement statement = connect.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            textAreaOutput.clear();
            textAreaOutput.appendText("Artists List:\n");

            while (rs.next()) {
                String artistName = rs.getString("aname");
                textAreaOutput.appendText(artistName + "\n");
            }
        } catch (SQLException e) {
            textAreaOutput.appendText("Error fetching artists: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
}
