package guiInterface;

import Utilities.ID3Object;
import Values.Music;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import neo4j.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Controller
{
    private DatabaseManager dbm = DatabaseManager.getInstance();
    private Importer importer = new Importer(dbm.getDatabaseConnector());
    private Editor editor = new Editor(dbm.getDatabaseConnector());
    private Deleter deleter = new Deleter(dbm.getDatabaseConnector());

    private final String[] SUPPORTED_EXT = {".mp3",".mp4",".wmv",".mpeg"};
    private final String ROOT_MUSIC_DIR = "src\\main\\resources\\Music";
    private String testString = "src\\main\\resources\\Music\\Blind Melon - No Rain.mp3";
    private ArrayList<String> _searchList;

    @FXML
    private MediaControl mediaControl;
    @FXML
    private MenuBar menuBar;
    @FXML
    private TableView<Music> tViewSongList;
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

    @FXML
    protected void handleSearchBar(KeyEvent keyEvent)
    {
        if(keyEvent.getCode().equals(KeyCode.ENTER))
        {
            String searchValue = searchBar.getText();
            SearchQuery query = new SearchQuery(DatabaseManager.getInstance().getDatabaseConnector());
           List<Music> list = query.search(searchValue);
           
           showResults((ObservableList)list);
        }
    }

    private void showResults(ObservableList<Music> list)
    {
        initTable();
        tViewSongList.setItems(list);
    }

    private void initTable()
    {
        TableColumn artist = new TableColumn("Artist");
        TableColumn album = new TableColumn("Album");
        TableColumn song = new TableColumn("Song");
        tViewSongList.getColumns().addAll(song, artist, album);
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

    @FXML protected void handleMenuItemClearAll(ActionEvent event)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear All");
        alert.setHeaderText("Clear all contents from database?");
        alert.setContentText("Are you sure you want to clear all contents from database?\n"
        + "*Note: Your songs (files) will not be deleted.");

        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == ButtonType.OK){
            deleter.cleanDatabase();
        }
    }

    @FXML protected void handleMenuItemImport(ActionEvent event)
    {
        int songCnt;
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Open Music Directory");
        File rootDir = dirChooser.showDialog(menuBar.getContextMenu());

        if(rootDir == null){
            return;
        }

        importer.addFolderRecursively(rootDir.getAbsolutePath(), new ArrayList<>(Arrays.asList(SUPPORTED_EXT)));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        if((songCnt = importer.getSongCount()) == 1){
            alert.setHeaderText(songCnt + " song imported!");
        } else {
            alert.setHeaderText(songCnt + " songs imported!");
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
        String song = testString;
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("itunes++_edit.fxml"));
        ControllerEdit controller = new ControllerEdit(editor);
        loader.setController(controller);
        Stage stage = new Stage(StageStyle.DECORATED);
        try{
            stage.setScene(new Scene(loader.load()));
            controller.initData(song);
        }catch(IOException e){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("File not found");
            alert.setHeaderText("Unable to locate file: " + song);
            alert.setContentText("Please double check that your file still exists.");
            alert.show();
            return;
        }
        stage.setTitle("ITunes++ Song Editor");
        stage.show();
    }

//-----------------------------------------------------------------------------
// NON-GUI METHODS
//-----------------------------------------------------------------------------
    private void initMediaPlayer(String path){
        Media media = new Media(new File(path).toURI().toString());
        MediaPlayer mp = new MediaPlayer(media);
        mp.setAutoPlay(true);
        mediaControl.setMediaPlayer(mp);
    }

    private void populateEditWindow(String path) throws IOException{
        ID3Object id3 = new ID3Object(new File(path));

    }
}
