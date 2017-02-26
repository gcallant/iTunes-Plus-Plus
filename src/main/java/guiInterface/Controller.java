package guiInterface;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import neo4j.DatabaseManager;
import neo4j.Deleter;
import neo4j.Editor;
import neo4j.Importer;
import org.jaudiotagger.audio.AudioFileIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

public class Controller
{
    private DatabaseManager dbm = DatabaseManager.getInstance();
    private Importer importer = new Importer(dbm.getDatabaseConnector());
    private Editor editor = new Editor(dbm.getDatabaseConnector());
    private Deleter deleter = new Deleter(dbm.getDatabaseConnector());

    private final String ROOT_MUSIC_DIR = "src\\main\\resources\\Music";
    private String testString = "src\\main\\resources\\Music\\Blind Melon - No Rain.mp3";
    private ArrayList<String> _searchList;

    @FXML
    private MediaControl mediaControl;
    @FXML
    private MenuBar menuBar;
    @FXML
    private TableView tViewSongList;
    @FXML
    private TextField searchBar;

    public static void showAlert(String header, String message, Alert.AlertType alertType)
    {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
    }

    @FXML protected void handleBtnPlay(MouseEvent event)
    {
        if(!mediaControl.isLoaded(testString)){
            initMediaPlayer(testString);
        }
        mediaControl.play();
    }

    @FXML protected void handleBtnNext(MouseEvent event)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Next song was clicked");
        alert.show();
    }

    @FXML protected void handleBtnPause(MouseEvent event)
    {
        mediaControl.pause();
    }

    @FXML protected void handleBtnStop(MouseEvent event)
    {
        mediaControl.stop();
    }

    @FXML protected void handleBtnPrev(MouseEvent event)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Previous song was clicked");
        alert.show();
    }

    @FXML protected void handleMenuItemAbout(ActionEvent event)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Project for Modern Database Systems\n"
                + "written by Ryan Babcock, Grant Callant, and Josh Cotes\n\n"
                + "Based on iTunes application, however this media player uses a Neo4j database.");
        alert.show();
    }

    @FXML protected void handleMenuItemClose(ActionEvent event)
    {
        Stage stage = (Stage) menuBar.getScene().getWindow();
        stage.close();
    }

    @FXML protected void handleMenuItemImport(ActionEvent event)
    {
        ArrayList<String> songFiles = new ArrayList<>();
        songFiles.add(".mp3");
        songFiles.add(".mp4");
        songFiles.add(".wmv");
        songFiles.add(".mpeg");

        AudioFileIO.logger.setLevel(Level.OFF);
        importer.addFolderRecursively(ROOT_MUSIC_DIR, songFiles);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        if(importer.getSongCount() == 1){
            alert.setHeaderText(importer.getSongCount() + " song imported!");
        } else {
            alert.setHeaderText(importer.getSongCount() + " songs imported!");
        }
        alert.show();
    }

    @FXML protected void handleMenuItemDelete(ActionEvent event)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Delete");
        alert.show();
    }

    @FXML protected void handleMenuItemEdit(ActionEvent event)
    {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("itunes++_edit.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("ITunes++ Edit");
            stage.setScene(new Scene(root));
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initMediaPlayer(String path){
        Media media = new Media(new File(path).toURI().toString());
        MediaPlayer mp = new MediaPlayer(media);
        mp.setAutoPlay(true);
        mediaControl.setMediaPlayer(mp);
    }

}
