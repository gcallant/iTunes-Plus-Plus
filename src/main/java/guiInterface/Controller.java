package guiInterface;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class Controller
{
   @FXML
   private ImageView previousSongButton;
   @FXML
   private ImageView playButton;
   @FXML
   private ImageView nextSongButton;
   @FXML
   private ImageView importButon;

   @FXML
   public static void showAlert(String header, String message, Alert.AlertType alertType)
   {
      Alert alert = new Alert(alertType);
      alert.setHeaderText(header);
      alert.setContentText(message);
      alert.show();
   }

   @FXML protected void handlePlayButtonAction(MouseEvent event)
   {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setHeaderText("Play was clicked");
      alert.show();
   }

   @FXML protected void handleNextSongButtonAction(MouseEvent event)
   {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setHeaderText("Next song was clicked");
      alert.show();
   }

   @FXML protected void handleImportButtonAction(MouseEvent event)
   {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setHeaderText("Import was clicked");
      alert.show();
   }

   @FXML protected void handlePreviousSongButtonAction(MouseEvent event)
   {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setHeaderText("Previous song was clicked");
      alert.show();
   }
}
