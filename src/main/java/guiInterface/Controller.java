package guiInterface;

import Values.*;
import Values.Label;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Controller
{
    private int curTrack = -1;
    private String prevSearch;
    private DatabaseManager dbm = DatabaseManager.getInstance();
    private Importer importer = new Importer(dbm.getDatabaseConnector());
    private Editor editor = new Editor(dbm.getDatabaseConnector());
    private Deleter deleter = new Deleter(dbm.getDatabaseConnector());
    private SearchQuery query = new SearchQuery(dbm.getDatabaseConnector());

//    private final String[] SUPPORTED_EXT = {".mp3",".mp4",".wmv",".mpeg", ".aac",
//                                            ".pcm", ".aif", ".aiff", ".flv", ".fxm",
//                                            ".wav", ".m4a", ".m4v", ".m4p", ".m4r", ".3gp"};
    private final String[] SUPPORTED_EXT = {//based on Jaudiotagger support
        ".mp3",".mp4",".m4a",".m4p",".sb0",".flac",".wav",".rm",".ra"
    };

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
        if(curTrack < 0) return;
        String songPath = getSelectedSongPath();
        if(!mediaControl.isLoaded(songPath)){
            initMediaPlayer(songPath);
        }
        mediaControl.play();
    }

    @FXML protected void handleBtnNext(MouseEvent event)
    {
        if(curTrack < 0) return;
        incrementCurrentTrack();
        handleBtnPlay(event);
    }

    @FXML
    protected void handleSearchBar(KeyEvent keyEvent)
    {
        if(keyEvent.getCode().equals(KeyCode.ENTER))
        {
            curTrack = -1;
            String searchValue = searchBar.getText();
            prevSearch = searchValue;
            List<Music> list = query.search(searchValue);
           
           showResults((ObservableList<Music>) list);
        }
    }

    @FXML
    protected void handleTableView(MouseEvent event){
        curTrack = tViewSongList.getSelectionModel().getSelectedIndex();
    }

    @FXML protected void handleBtnPause(MouseEvent event){
        mediaControl.pause();
    }

    @FXML protected void handleBtnStop(MouseEvent event){
        mediaControl.stop();
    }

    @FXML protected void handleBtnPrev(MouseEvent event)
    {
        if(curTrack < 0) return;
        decrementCurrentTrack();
        handleBtnPlay(event);
    }

    @FXML protected void handleMenuItemAbout(ActionEvent event)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Project for Modern Database Systems\n"
                + "written by Ryan Babcock, Grant Callant, and Josh Cotes\n\n"
                + "Based on iTunes application, however this media player uses a Neo4j database.");
        alert.initOwner(getPrimaryStage());
        alert.show();
    }

    @FXML protected void handleMenuItemClose(ActionEvent event)
    {
        Stage stage = (Stage) menuBar.getScene().getWindow();
        stage.close();
        System.exit(0);
    }

    @FXML protected void handleMenuItemClearAll(ActionEvent event)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear All");
        alert.setHeaderText("Clear all contents from database?");
        alert.setContentText("Are you sure you want to clear all contents from database?\n"
        + "*Note: Your songs (files) will not be deleted.");

        alert.initOwner(getPrimaryStage());
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

        long startTime = System.currentTimeMillis();

        importer.addFolderRecursively(rootDir.getAbsolutePath(),
                new ArrayList<>(Arrays.asList(SUPPORTED_EXT)));

        double totalTime = (System.currentTimeMillis() - startTime)/1000;
        DecimalFormat df = new DecimalFormat("#.###");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        if((songCnt = importer.getSongCount()) == 1){
            alert.setHeaderText(songCnt + " song imported!");
        } else {
            alert.setHeaderText(songCnt + " songs imported!");
        }
        alert.setContentText("Process took "+df.format(totalTime)+" sec");
        alert.initOwner(getPrimaryStage());
        alert.show();
    }

    @FXML protected void handleMenuItemDelete(ActionEvent event)
    {
        if(curTrack < 0){
            noSelection("Delete Song");
            return;
        }
        Music song = getSelectedMusic();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete");
        alert.setHeaderText("Remove song from database?");
        alert.setContentText("Are you sure you want to remove '"+song.getSong()
                +"' from the database?\n*Note: Your file will not be deleted.");

        alert.initOwner(getPrimaryStage());
        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == ButtonType.OK){
            Finder finder = new Finder(dbm.getDatabaseConnector());
            String songID = finder.findIDByProperty(Label.SONGNAME,
                new PropertySet(Property.FILENAME,song.getFilePath()));
            deleter.deleteSong(songID);
        }

        refreshResults();
    }

    @FXML protected void handleMenuItemEdit(ActionEvent event)
    {
        if(curTrack < 0){
            noSelection("Edit Song");
            return;
        }
        String song = getSelectedSongPath();
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
            alert.initOwner(getPrimaryStage());
            alert.show();
            return;
        }
        stage.setTitle("ITunes++ Song Editor");
        stage.initOwner(getPrimaryStage());
        stage.show();
        refreshResults();
    }

//-----------------------------------------------------------------------------
// NON-GUI METHODS
//-----------------------------------------------------------------------------
    public void playNextSong(){
        handleBtnNext(null);
        handleBtnPlay(null);
    }

    private void noSelection(String title){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText("No song is selected");
        alert.setContentText("Please select a song and try again.");
        alert.initOwner(getPrimaryStage());
        alert.show();
    }

    private Stage getPrimaryStage(){
        return (Stage) menuBar.getScene().getWindow();
    }

    private Music getSelectedMusic(){
        return tViewSongList.getItems().get(curTrack);
    }

    private String getSelectedSongPath(){
        Music song = tViewSongList.getItems().get(curTrack);
        return song.getFilePath();
    }

    private void initMediaPlayer(String path){
        Media media = new Media(new File(path).toURI().toString());
        MediaPlayer mp = new MediaPlayer(media);
        mp.setAutoPlay(true);
        mediaControl.setMediaPlayer(mp);
    }

    private void initTable()
    {
        TableColumn<Music, String> song = new TableColumn<>("Song");
        song.setCellValueFactory(new PropertyValueFactory<>("Song"));

        TableColumn<Music, String> artist = new TableColumn<>("Artist");
        artist.setCellValueFactory(new PropertyValueFactory<>("Artist"));

        TableColumn<Music, String> album = new TableColumn<>("Album");
        album.setCellValueFactory(new PropertyValueFactory<>("Album"));

        TableColumn<Music, String> genre = new TableColumn<>("Genre");
        genre.setCellValueFactory(new PropertyValueFactory<>("Genre"));

        tViewSongList.getColumns().addAll(song, artist, album, genre);
    }

    private void incrementCurrentTrack(){
        int sz = tViewSongList.getItems().size();
        curTrack = ++curTrack % sz;
        tViewSongList.getSelectionModel().select(curTrack);
    }

    private void decrementCurrentTrack(){
        int sz = tViewSongList.getItems().size();
        curTrack = --curTrack % sz;
        if(curTrack < 0)
            curTrack = sz + curTrack;

        tViewSongList.getSelectionModel().select(curTrack);
    }

    private void refreshResults(){
        curTrack = -1;
        List<Music> list = query.search(prevSearch);

        showResults((ObservableList<Music>) list);
    }

    private void showResults(ObservableList<Music> list)
    {
        tViewSongList.getColumns().clear();
        initTable();
        tViewSongList.setItems(list);
    }
}
