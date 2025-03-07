package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.sql.*;
import java.util.Optional;

public class PlaylistController {

    private final Connection connect;
    @FXML
    TabPane tabPane;
    public PlaylistController(Connection connect, TabPane tabPane) {
        this.connect = connect;
        this.tabPane = tabPane;
    }

    /** Fetch all playlists from DB and create tabs */
    public void loadExistingPlaylists() {
        String query = "SELECT plname FROM Playlist"; // Query to get all playlists

        try (Statement statement = connect.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            while (rs.next()) {
                String playlistName = rs.getString("plname");
                createPlaylistTab(playlistName); // Create a tab for each playlist
            }
        } catch (SQLException e) {
            System.out.println("Error loading playlists: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public void insertPlaylistIntoDatabase(String playlistName) {
        String insertSQL = "INSERT INTO Playlist (plname) VALUES (?)";
        try (PreparedStatement preparedStatement = connect.prepareStatement(insertSQL)) {
            preparedStatement.setString(1, playlistName);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createPlaylistTab(String playlistName) {
        if(tabPane == null) {
            System.out.println("Error: tabpane is null");
            return;
        }
        Tab playlistTab = new Tab(playlistName); // Create a new tab with the playlist name
        Label title = new Label(playlistName);
        title.setLayoutX(10);
        title.setLayoutY(10);
        title.setStyle("-fx-font-family: \"Inter\", \"Segoe UI\", \"Arial\", sans-serif;\n" +
                "    -fx-font-size: 24px;\n" +
                "    -fx-text-fill: #333333; /* Dark gray for readability */\n" +
                "    -fx-line-spacing: 4px;\n" +
                "    -fx-padding: 10px;");
        AnchorPane playlistPane = new AnchorPane(); // Layout for the tab

        ListView<String> playlistSongsView = new ListView<>(); // ListView to hold songs

        playlistPane.getStylesheets().add(getClass().getResource("../../style.css").toExternalForm()); // Add the stylesheet to the new AnchorPane

        // Fetch and display songs for this playlist
        loadSongsForPlaylist(playlistName, playlistSongsView);

        // Add "Add Song" button
        Button addSongButton = new Button("Add Song");
        addSongButton.setStyle("-fx-padding: 8px 10px; \n" +
                "    -fx-background-color: #ebe294;\n" +
                "    -fx-text-fill: #1E293B; \n" +
                "    -fx-font-weight: 500; \n" +
                "    -fx-border-radius: 999px; \n" +
                "    -fx-background-radius: 999px;\n" +
                "    -fx-cursor: hand; \n" +
                "    -fx-transition: all 0.3s ease-in-out;");
        addSongButton.setLayoutX(16);
        addSongButton.setLayoutY(50);
        addSongButton.setOnAction(event -> addSongToPlaylist(playlistName, playlistSongsView));

        // "Delete Playlist" Button
        Button deletePlaylistButton = new Button("Delete Playlist");
        deletePlaylistButton.setStyle("-fx-padding: 8px 10px;\n" +
                "    -fx-background-color: #FF6B6B;\n" +
                "    -fx-text-fill: white;\n" +
                "    -fx-font-weight: bold;\n" +
                "    -fx-border-radius: 999px;\n" +
                "    -fx-background-radius: 999px;\n" +
                "    -fx-cursor: hand;");
        deletePlaylistButton.setLayoutX(120);
        deletePlaylistButton.setLayoutY(50);
        deletePlaylistButton.setOnAction(event -> deletePlaylist(playlistName, playlistTab));


        // Positioning
        playlistSongsView.setLayoutX(20);
        playlistSongsView.setLayoutY(100);
        playlistSongsView.setPrefSize(680, 500);

        playlistPane.getChildren().addAll(title, addSongButton, deletePlaylistButton, playlistSongsView);
        playlistTab.setContent(playlistPane);
        tabPane.getTabs().add(playlistTab); // Add the tab to the TabPane
    }

    /** Loads songs for the playlist through fetching from DB */
    private void loadSongsForPlaylist(String playlistName, ListView<String> playlistSongsView) {
        playlistSongsView.setStyle("-fx-font-family: 'Monospaced';");
        String query = "SELECT s.sname, aname, s.album, s.length FROM Artist a NATURAL JOIN Performs " +
                " NATURAL JOIN Song s" +
                " NATURAL JOIN Playlist_Song ps " +
                "WHERE plid = (SELECT plid FROM Playlist WHERE plname = ?) " +
                "ORDER BY ps.song_order;";

        try (PreparedStatement statement = connect.prepareStatement(query)) {
            statement.setString(1, playlistName);
            ResultSet rs = statement.executeQuery();
            playlistSongsView.getItems().clear();

            while (rs.next()) {
                String songName = rs.getString("sname");
                String artist = rs.getString("aname");
                String album = rs.getString("album");
                String length = rs.getString("length");

                String result = String.format("%-25s | %-20s | %-24s | %4s", songName, artist, album, length);
                playlistSongsView.getItems().add(result + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /** Creates a new playlist tuple in Playlist Table DB */
    private void addSongToPlaylist(String playlistName, ListView<String> playlistSongsView) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add Song");
        dialog.setHeaderText("Enter Song Details:");

        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create the input fields
        TextField songNameField = new TextField();
        songNameField.setPromptText("Song Name");

        TextField artistNameField = new TextField();
        artistNameField.setPromptText("Artist Name");

        // Create a GridPane for the dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Song Name:"), 0, 0);
        grid.add(songNameField, 1, 0);
        grid.add(new Label("Artist Name:"), 0, 1);
        grid.add(artistNameField, 1, 1);

        // Set the content of the dialog to the GridPane
        dialog.getDialogPane().setContent(grid);

        // Convert the result to a Pair (song name, artist name)
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new Pair<>(songNameField.getText(), artistNameField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(pair -> {
            String songName = pair.getKey();
            String artistName = pair.getValue();

            // Check if the song exists in the Song table
            String checkSongQuery = "SELECT s.sid FROM Song s " +
                    "NATURAL JOIN Performs p " +
                    "NATURAL JOIN Artist a " +
                    "WHERE s.sname = ? AND a.aname = ?";
            try (PreparedStatement ps = connect.prepareStatement(checkSongQuery)) {
                ps.setString(1, songName);
                ps.setString(2, artistName);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    // Song does not exist in the database
                    showErrorDialog("Error", "Song not found in the database.");
                    return;
                }

                // Fetch the current max song_order for this playlist
                String getMaxOrderQuery = "SELECT MAX(song_order) FROM Playlist_Song WHERE plid = (SELECT plid FROM Playlist WHERE plname = ?)";
                int nextOrder = 1; // Default to 1 if no songs exist in the playlist
                try (PreparedStatement psMaxOrder = connect.prepareStatement(getMaxOrderQuery)) {
                    psMaxOrder.setString(1, playlistName);
                    ResultSet rsOrder = psMaxOrder.executeQuery();
                    if (rsOrder.next()) {
                        nextOrder = rsOrder.getInt(1) + 1; // Increment the max order
                    }
                }

                // Insert the song into the Playlist_Song table with the correct song_order
                String insertSQL = "INSERT INTO Playlist_Song (plid, sid, song_order) VALUES " +
                        "((SELECT plid FROM Playlist WHERE plname = ?), " +
                        "(SELECT sid FROM Song WHERE sname = ?), ?)";
                try (PreparedStatement preparedStatement = connect.prepareStatement(insertSQL)) {
                    preparedStatement.setString(1, playlistName);
                    preparedStatement.setString(2, songName);
                    preparedStatement.setInt(3, nextOrder); // Set the correct song order
                    preparedStatement.executeUpdate();
                    playlistSongsView.getItems().add(songName); // Update the UI
                    loadSongsForPlaylist(playlistName, playlistSongsView);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void deletePlaylist(String playlistName, Tab playlistTab) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setGraphic(null);
        alert.setTitle("Delete Playlist");
        alert.setHeaderText("Are you sure you want to delete this playlist?");
        alert.setContentText("This action cannot be undone.");

        // Wait for user confirmation
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String deleteQuery = "DELETE FROM Playlist WHERE plname = ?";

            try (PreparedStatement statement = connect.prepareStatement(deleteQuery)) {
                statement.setString(1, playlistName);
                int rowsDeleted = statement.executeUpdate();

                if (rowsDeleted > 0) {
                    tabPane.getTabs().remove(playlistTab); // Remove tab from UI
                    System.out.println("Playlist deleted successfully: " + playlistName);
                } else {
                    showErrorDialog("Error", "Failed to delete playlist.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showErrorDialog("Database Error", "An error occurred while deleting the playlist.");
            }
        }
    }


    // Method to show an error dialog
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
