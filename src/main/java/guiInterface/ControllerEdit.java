package guiInterface;

import Utilities.ID3Object;
import Values.Label;
import Values.Property;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import neo4j.EditRequest;
import neo4j.Editor;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by Ryan on 2/25/2017.
 */
public class ControllerEdit {
    private Editor editor;
    private ID3Object id3;
    //Starting values for tags
    private String sTitle, sArtist, sAlbum, sGenre, sDisc, sTrack, sYear, sComp, sComm;
    private String songID, artistID, albumID, genreID;
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

    public ControllerEdit(Editor editor){ this.editor = editor; }

    /**
     * Checks to see if a change has occurred and sends changes as
     * edit requests to the editor.
     *
     * @param event
     */
    @FXML protected void handleBtnSubmit(MouseEvent event)
    {
        int reqCnt;
        EditRequest editRequest;
        LinkedList<EditRequest> reqList = new LinkedList<>();

        if((editRequest = checkAlbum(album.getText())) != null)
            reqList.add(editRequest);
        if((editRequest = checkArtist(artist.getText())) != null)
            reqList.add(editRequest);
        if((editRequest = checkComment(comment.getText())) != null)
            reqList.add(editRequest);
        if((editRequest = checkComposer(composer.getText())) != null)
            reqList.add(editRequest);
        if((editRequest = checkDiscNo(discNo.getText())) != null)
            reqList.add(editRequest);
        if((editRequest = checkGenre(genre.getText())) != null)
            reqList.add(editRequest);
        if((editRequest = checkTitle(title.getText())) != null)
            reqList.add(editRequest);
        if((editRequest = checkTrack(track.getText())) != null)
            reqList.add(editRequest);
        if((editRequest = checkYear(year.getText())) != null)
            reqList.add(editRequest);

        if((reqCnt = reqList.size()) > 0) editor.edit(reqList);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        if(reqCnt == 1){
            alert.setHeaderText(reqCnt + " field updated!");
        } else {
            alert.setHeaderText(reqCnt + " fields updated!");
        }

        alert.show();
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
     * @param path  Path to ID3Tag
     * @throws IOException
     */
    public void initData(String path) throws IOException{
        id3 = new ID3Object(new File(path));
        album.setText(sAlbum = id3.getAlbum());
        artist.setText(sArtist = id3.getArtist());
        comment.setText(sComm = id3.getComment());
        composer.setText(sComp = id3.getComposer());
        discNo.setText(sDisc = id3.getDiscNo());
        genre.setText(sGenre = id3.getGenre());
        title.setText(sTitle = id3.getTitle());
        track.setText(sTrack = id3.getTrack());
        year.setText(sYear = id3.getYear());

//        AlbumID= searchAlbum(sAlbum);
//        ArtistID= searchArtist(sArtist);
//        GenreID= searchGenre(sGenre);
//        SongID= searchSong(sTitle);
    }

//-----------------------------------------------------------------------------
// CHECKS
//-----------------------------------------------------------------------------
    private EditRequest checkAlbum(String nAlbum){
        if(nAlbum.equals(sAlbum)) return null;
        return new EditRequest(albumID, Label.ALBUM, Property.ALBUM_NAME, nAlbum);
    }

    private EditRequest checkArtist(String nArtist){
        if(nArtist.equals(sArtist)) return null;
        return new EditRequest(artistID, Label.ARTIST, Property.ARTIST_NAME, nArtist);
    }

    private EditRequest checkComment(String nComm){
        if(nComm.equals(sComm)) return null;
        return new EditRequest(songID, Label.SONGNAME, Property.COMMENT, nComm);
    }

    private EditRequest checkComposer(String nComp){
        if(nComp.equals(sComp)) return null;
        return new EditRequest(songID, Label.SONGNAME, Property.COMPOSER_NAME, nComp);
    }

    private EditRequest checkDiscNo(String nDisc){
        if(nDisc.equals(sDisc)) return null;
        return new EditRequest(songID, Label.SONGNAME, Property.DISC_NO, nDisc);
    }

    private EditRequest checkGenre(String nGenre){
        if(nGenre.equals(sGenre)) return null;
        return new EditRequest(genreID, Label.GENRE, Property.GENRE_NAME, nGenre);
    }

    private EditRequest checkTitle(String nTitle){
        if(nTitle.equals(sTitle)) return null;
        return new EditRequest(songID, Label.SONGNAME, Property.SONG_NAME, nTitle);
    }

    private EditRequest checkTrack(String nTrack){
        if(nTrack.equals(sTrack)) return null;
        return new EditRequest(songID, Label.SONGNAME, Property.TRACK_NUM, nTrack);
    }

    private EditRequest checkYear(String nYear){
        if(nYear.equals(sYear)) return null;
        return new EditRequest(songID, Label.SONGNAME, Property.YEAR, nYear);
    }
}
