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
    private TextField insertSongName;
    @FXML
    private TextField insertSongAlbum;
    @FXML
    private TextField insertSongLength;
    @FXML
    private TextField insertSongGenre;
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
    private void insertSongFunc(ActionEvent event){
        String songName = insertSongName.getText().trim();
        String songAlbum = insertSongAlbum.getText().trim();
        int songLength = Integer.parseInt(insertSongLength.getText().trim());
        String songGenre = insertSongGenre.getText().trim();

        try (Statement statement = connect.createStatement()) {
            String arguments = String.format("('%s','%s',%d,'%s');",songName,songAlbum,songLength,songGenre);
            String query = String.format("INSERT INTO Song(sname, album, length, genre) VALUES " + arguments);
            //System.out.println("Q: " + query);
            statement.executeUpdate(query);

            //VERIFICATION
            ResultSet rs;
            query = String.format(
                    "SELECT * FROM Song s WHERE " +
                            "s.sname = '%s' AND " +
                            "s.album = '%s' AND " +
                            "s.length = '%s' AND " +
                            "s.genre = '%s';"
                    ,songName, songAlbum, songLength, songGenre);
            rs = statement.executeQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            //System.out.println("Inserted: ");
            while (rs.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = rs.getString(i);
                    System.out.print(columnValue + " " + rsmd.getColumnName(i));
                }
                System.out.println("");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }



    }

    @FXML
    private void deleteSongFunc(ActionEvent event){
         String songName = deleteSongTextField.getText().trim();
         ResultSet rs;
        try (Statement statement = connect.createStatement()) {
            String query = String.format("DELETE FROM Song s WHERE s.sname = '%s'", songName);
            rs = statement.executeQuery(query);
            while (rs.next()) {
                String studentID = rs.getString(1),studentName = rs.getString(2);
                System.out.println("Student: " +
                        studentID + " " + studentName);
            }

            //TEMP
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = rs.getString(i);
                    System.out.print(columnValue + " " + rsmd.getColumnName(i));
                }
                System.out.println("");
            }
            //TEMP

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
