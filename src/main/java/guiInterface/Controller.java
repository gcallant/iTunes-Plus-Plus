package guiInterface;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Controller
{
   private ArrayList<String> _list;
   private MediaPlayer _mPlayer;
   private MediaView _mView;

   @FXML
   private ImageView btnNext;
   @FXML
   private ImageView btnPause;
   @FXML
   private ImageView btnPlay;
   @FXML
   private ImageView btnPrev;
   @FXML
   private ImageView btnStop;
   @FXML
   private MenuBar menuBar;
    @FXML
    private MenuItem itemAbout;
    @FXML
    private MenuItem itemClose;
    @FXML
    private MenuItem itemDelete;
    @FXML
    private MenuItem itemEdit;
    @FXML
    private MenuItem itemImport;
   @FXML
   private TableView tViewSongList;
   @FXML
   private TextField searchBar;

   @FXML
   public static void showAlert(String header, String message, Alert.AlertType alertType)
   {
      Alert alert = new Alert(alertType);
      alert.setHeaderText(header);
      alert.setContentText(message);
      alert.show();
   }

   @FXML protected void handleBtnPlay(MouseEvent event)
   {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setHeaderText("Play was clicked");
      alert.show();
   }

   @FXML protected void handleBtnNext(MouseEvent event)
   {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setHeaderText("Next song was clicked");
      alert.show();
   }

   @FXML protected void handleBtnPause(MouseEvent event)
   {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setHeaderText("Pause was clicked");
      alert.show();
   }

   @FXML protected void handleBtnStop(MouseEvent event)
   {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setHeaderText("Stop was clicked");
      alert.show();
   }

   @FXML protected void handleBtnPrev(MouseEvent event)
   {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setHeaderText("Previous song was clicked");
      alert.show();
   }

//    @FXML protected void handleMenuBar(MouseEvent event)
//    {
//        System.out.println("Manage Accbtnclick");
//        Stage stage = (Stage) menuBar.getScene().getWindow();
//        Scene scene = Main.screens.get("tweet");
//        stage.setScene(scene);
//        stage.show();
//    }

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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Import");
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

   private void setMediaPlayer(String path){
      Media media = new Media(new File(path).toURI().toString());
      _mPlayer = new MediaPlayer(media);
      _mPlayer.setAutoPlay(true);
      _mView = new MediaView(_mPlayer);
   }
}
