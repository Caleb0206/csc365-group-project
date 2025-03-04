package com.example;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import org.w3c.dom.Text;

import java.sql.*;


public class ModalController {
    private Connection connect;
    private MainController mainController; //reference to MainController

    @FXML
    private Label connectDatabase;


    @FXML
    private Button closeModalButton;
    @FXML
    private Button addSongButton;
    @FXML
    private VBox songListContainer;
    @FXML
    private TextField artistTextField;
    @FXML
    private TextField albumTextField;

    @FXML
    public void initialize() {
        connectToDatabase();
        // ensure at least 1 song-field is on the modal upon launching
        addSongField(null);
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
        durationField.setPrefWidth(songRow.getPrefWidth() * 0.2);

        TextField genreField = new TextField();
        genreField.setPromptText("Genre");
        genreField.getStyleClass().add("search-bar");
        genreField.setPrefWidth(songRow.getPrefWidth() * 0.30);

        songRow.getChildren().addAll(newSongField, durationField, genreField);
        songListContainer.getChildren().addAll(songRow);

    }

    @FXML
    public void saveAlbum(ActionEvent event) {
        String artistName = artistTextField.getText().trim();
        String albumName = albumTextField.getText().trim();

        if (artistName.isEmpty() || albumName.isEmpty()) {
            connectDatabase.setText("Artist and Album Name cannot be empty!");
            return;
        }
        try {
            int artistId = -1;

            // Check if artist exists
            String findArtistQuery = "SELECT aid FROM Artist WHERE aname = ?";
            PreparedStatement checkArtistStmt = connect.prepareStatement(findArtistQuery);
            checkArtistStmt.setString(1, artistName);
            ResultSet rs = checkArtistStmt.executeQuery();

            if (rs.next()) {
                artistId = rs.getInt("aid");
            } else {
                // If artist does not exist, insert them
                String insertArtistQuery = "INSERT INTO Artist (aname) VALUES (?)";
                PreparedStatement insertArtistStmt = connect.prepareStatement(insertArtistQuery, Statement.RETURN_GENERATED_KEYS);
                insertArtistStmt.setString(1, artistName);
                insertArtistStmt.executeUpdate();

                ResultSet artistKeys = insertArtistStmt.getGeneratedKeys();
                if(artistKeys.next()) {
                    artistId = artistKeys.getInt(1);
                }
            }

            // Insert songs from songListContainer link to Performs
            for (javafx.scene.Node node : songListContainer.getChildren()) {
                if (node instanceof HBox) {
                    HBox songRow = (HBox) node;
                    TextField songField = (TextField) songRow.getChildren().get(0);
                    TextField durationField = (TextField) songRow.getChildren().get(1);
                    TextField genreField = (TextField) songRow.getChildren().get(2);

                    String songName = songField.getText().trim();
                    String duration = durationField.getText().trim();
                    String genre = genreField.getText().trim();

                    if (!songName.isEmpty()) {
                        int songId = -1;

                        // insert the song into Song table
                        String insertSongQuery = "INSERT INTO Song (sname, album, genre, length) VALUES (?, ?, ?, ?)";
                        PreparedStatement insertSongStmt = connect.prepareStatement(insertSongQuery, Statement.RETURN_GENERATED_KEYS);
                        insertSongStmt.setString(1, songName);
                        insertSongStmt.setString(2, albumName);
                        insertSongStmt.setString(3, genre);
                        insertSongStmt.setString(4, duration);
                        insertSongStmt.executeUpdate();

                        ResultSet songKeys = insertSongStmt.getGeneratedKeys();
                        if (songKeys.next()) {
                            songId = songKeys.getInt(1);
                        }

                        // Insert into Performs table to link artist and song
                        if (artistId != -1 && songId != -1) {
                            String insertPerformsQuery = "INSERT INTO Performs (aid, sid) VALUES (?, ?)";
                            PreparedStatement insertPerformsStmt = connect.prepareStatement(insertPerformsQuery);
                            insertPerformsStmt.setInt(1, artistId);
                            insertPerformsStmt.setInt(2, songId);
                            insertPerformsStmt.executeUpdate();
                            System.out.println("inserted " + artistId + " " + songId );
                        }

                    }
                }
            }

            connectDatabase.setText("Album and songs saved successfully!");

            // refresh the ListView in main controller
            if(mainController != null) {
                mainController.onOpenFetchSongs();
            }
            //close modal after saving album
            closeModal(null);

        } catch (SQLException e) {
            connectDatabase.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        }

    }

}
