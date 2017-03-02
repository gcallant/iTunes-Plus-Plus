package neo4j;

import Utilities.ID3Object;
import Values.Label;
import Values.Property;
import org.neo4j.driver.v1.Session;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Ryan on 2/19/2017.
 *
 * note: Implementation should create a list of only changed values. These changes
 * should be converted into a List of EditRequests and sent to the Editor.
 */
public class Editor {
//-----------------------------------------------------------------------------
// PARAMS
//-----------------------------------------------------------------------------
    private Session _session;

//-----------------------------------------------------------------------------
// CONSTRUCTORS
//-----------------------------------------------------------------------------
    public Editor(Session session) {
        _session = session;
    }

//-----------------------------------------------------------------------------
// PUBLIC METHODS
//-----------------------------------------------------------------------------
    public void edit(EditRequest req, ID3Object id3) {
        req.value = sanitizeString(req.value);
//        editDB(req);
        updateID3(req, id3);
    }

    public void edit(List<EditRequest> requests, ID3Object id3) {
        for (EditRequest req : requests) {
            edit(req, id3);
        }
    }

//-----------------------------------------------------------------------------
// PRIVATE METHODS
//-----------------------------------------------------------------------------
    private void deleteHelper(EditRequest req, ID3Object id3){
        switch (req.label) {
            case Label.ALBUM:
                id3.delAlbum();
                break;
            case Label.ARTIST:
                id3.delArtist();
                break;
            case Label.GENRE:
                id3.delGenre();
                break;
            case Label.SONGNAME:
                switch (req.prop) {
                    case Property.COMMENT:
                        id3.delComment();
                        break;
                    case Property.COMPOSER_NAME:
                        id3.delComposer();
                        break;
                    case Property.DISC_NO:
                        id3.delDiscNo();
                        break;
                    case Property.SONG_NAME:
                        id3.delTitle();
                        break;
                    case Property.TRACK_NUM:
                        id3.delTrack();
                        break;
                    case Property.YEAR:
                        id3.delYear();
                }
        }
    }

    private void editDB(EditRequest req) {
        StringBuilder query = new StringBuilder();

        query.append("MATCH (").append(req.key).append(":").append(req.label).append(") ");
        query.append("SET ").append(req.key).append(".").append(req.prop).append(" = '").append(req.value).append("'");

        _session.run(query.toString());
    }

    private String sanitizeString(String dirty) {
            return dirty.replace('\"', '\'').replace("\\", "//");
    }

    private void setHelper(EditRequest req, ID3Object id3){
        switch (req.label) {
            case Label.ALBUM:
                id3.setAlbum(req.value);
                break;
            case Label.ARTIST:
                id3.setArtist(req.value);
                break;
            case Label.GENRE:
                id3.setGenre(req.value);
                break;
            case Label.SONGNAME:
                switch (req.prop) {
                    case Property.COMMENT:
                        id3.setComment(req.value);
                        break;
                    case Property.COMPOSER_NAME:
                        id3.setComposer(req.value);
                        break;
                    case Property.DISC_NO:
                        id3.setDiscNo(req.value);
                        break;
                    case Property.SONG_NAME:
                        id3.setTitle(req.value);
                        break;
                    case Property.TRACK_NUM:
                        id3.setTrack(req.value);
                        break;
                    case Property.YEAR:
                        id3.setYear(req.value);
                }
        }
    }

    private void updateID3(EditRequest req, ID3Object id3) {
        if(req.value.equals("") || req.value == null){
            deleteHelper(req, id3);
        }
        else {
            setHelper(req, id3);
        }
    }
}
