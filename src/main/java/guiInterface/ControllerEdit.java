package guiInterface;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Created by Ryan on 2/25/2017.
 */
public class ControllerEdit {
    @FXML
    private TextField title;
    @FXML
    private TextField artist;
    @FXML
    private TextField album;
    @FXML
    private TextField genre;
    @FXML
    private TextField discNo;
    @FXML
    private TextField track;
    @FXML
    private TextField year;
    @FXML
    private TextField composer;
    @FXML
    private TextArea comment;
    @FXML
    private Button submit;
    @FXML
    private Button cancel;

    @FXML protected void handleBtnSubmit(MouseEvent event)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Submit was clicked");
        alert.show();
    }

    @FXML protected void handleBtnCancel(MouseEvent event)
    {
        Stage stage = (Stage) cancel.getScene().getWindow();
        stage.close();

    }
}
