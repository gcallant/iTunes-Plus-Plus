package guiInterface;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application
{
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
}
