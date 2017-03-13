package guiInterface;

import Utilities.ID3Object;
import Utilities.Sanitizer;
import Values.Label;
import Values.Property;
import Values.PropertySet;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import neo4j.Editor;
import neo4j.Finder;

import java.io.File;
import java.io.IOException;

/**
 * Created by Ryan on 3/12/2017.
 *
 * This class is used to edit any label that is not a song
 */
public class ControllerNodeEdit {
    private Finder finder;
    private Editor editor;
    private Controller parentController;
    private String label;
    private String original;

    @FXML private javafx.scene.control.Label nodeType;
    @FXML private TextField input;
    @FXML private Button cancel;

    public ControllerNodeEdit(Finder finder, Editor editor, Controller parentController){
        this.finder = finder;
        this.editor = editor;
        this.parentController = parentController;
    }

    @FXML protected void handleInput(KeyEvent keyEvent){
        if(keyEvent.getCode().equals(KeyCode.ENTER)){
            handleBtnSubmit(null);
        }
    }

    @FXML protected void handleBtnSubmit(MouseEvent event){
        int id = -1;
        PropertySet set = null;
        String newValue = input.getText();

        if(newValue.equals(original)) {
            handleBtnCancel(event);
        }

        switch (label){
            case Label.ALBUM :
                set = new PropertySet(Property.ALBUM_NAME,original);
                id = finder.findIDByProperty(label,set);
                break;
            case Label.ARTIST :
                set = new PropertySet(Property.ARTIST_NAME,original);
                id = finder.findIDByProperty(label,set);
                break;
            case Label.GENRE :
                set = new PropertySet(Property.GENRE_NAME,original);
                id = finder.findIDByProperty(label,set);
                break;
            default:
                System.err.println("Unable to process label");
                handleBtnCancel(event);
        }

        set.val = newValue;
        long startTime = System.currentTimeMillis();
        editor.editNode(label,id,set);
        double totalTime = ((double)System.currentTimeMillis() - startTime)/1000;

        parentController.setEditTime(totalTime);
        handleBtnCancel(event);
    }

    @FXML
    private void handleBtnCancel(MouseEvent event)
    {
        Stage stage = (Stage) cancel.getScene().getWindow();
        stage.close();
    }

    void initData(String label, String path) throws IOException {
        this.label = label;
        ID3Object id3 = new ID3Object(new File(path));
        switch (label){
            case Label.ALBUM :
                nodeType.setText("Album");
                input.setText(original = id3.getAlbum());
                return;
            case Label.ARTIST :
                nodeType.setText("Artist");
                input.setText(original = id3.getArtist());
                return;
            case Label.GENRE :
                nodeType.setText("Genre");
                input.setText(original = id3.getGenre());
                return;
            default :
                System.err.println("Do not recognize data");
        }
    }
}
