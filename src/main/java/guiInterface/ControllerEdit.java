package guiInterface;

import Utilities.ID3Object;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import neo4j.EditRequest;
import neo4j.Editor;

import java.io.File;
import java.io.IOException;

/**
 * Created by Ryan on 2/25/2017.
 * This class is used as the controller between the Editor and the GUI.
 * It is called by the Controller class.
 */
public class ControllerEdit {
    private Editor editor;
    private EditRequest editRequest;
    private ID3Object id3;

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
    private Button cancel;

    ControllerEdit(Editor editor){ this.editor = editor; }

    /**
     * Checks to see if a change has occurred and sends changes as
     * edit requests to the editor.
     *
     * @param event does nothing...
     */
    @FXML protected void handleBtnSubmit(MouseEvent event)
    {
        editRequest.album = album.getText();
        editRequest.artist = artist.getText();
        editRequest.comment = comment.getText();
        editRequest.composer = composer.getText();
        editRequest.discNo = discNo.getText();
        editRequest.genre = genre.getText();
        editRequest.title = title.getText();
        editRequest.track = track.getText();
        editRequest.year = year.getText();

        editor.edit(editRequest, id3);

        handleBtnCancel(event);
    }

    @FXML protected void handleBtnCancel(MouseEvent event)
    {
        Stage stage = (Stage) cancel.getScene().getWindow();
        stage.close();
    }

    /**
     * Initializes the text field values according to id3 tag and records
     * original values.
     * @throws IOException Caused by ID3Object if path is bad
     */
    void initData(String path) throws IOException{
        id3 = new ID3Object(new File(path));
        editRequest = new EditRequest(id3);
        EditRequest original = editRequest.getOriginal();

        album.setText(original.album);
        artist.setText(original.artist);
        comment.setText(original.comment);
        composer.setText(original.composer);
        discNo.setText(original.discNo);
        genre.setText(original.genre);
        title.setText(original.title);
        track.setText(original.track);
        year.setText(original.year);
    }
}
