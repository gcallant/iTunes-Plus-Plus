package guiInterface;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application
{
   private MediaPlayer _mPlayer;
   private MediaView _mView;

   public static void main(String[] args)
   {
      launch(args);
   }

   @Override
   public void start(Stage primaryStage) throws Exception
   {
      Parent root = FXMLLoader.load(getClass().getResource("/itunes++.fxml"));

      Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
      primaryStage.setTitle("iTunes++");
      primaryStage.setScene(new Scene(root, 300, 275));
      primaryStage.setX(screenBounds.getMinX());
      primaryStage.setY(screenBounds.getMinY());
      primaryStage.setWidth(screenBounds.getWidth());
      primaryStage.setHeight(screenBounds.getHeight());
      primaryStage.show();
   }



   private void setMediaPlayer(String path){
      Media media = new Media(new File(path).toURI().toString());
      _mPlayer = new MediaPlayer(media);
      _mPlayer.setAutoPlay(true);
      _mView = new MediaView(_mPlayer);
   }
}
