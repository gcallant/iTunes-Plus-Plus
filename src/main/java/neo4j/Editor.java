package neo4j;

import ID3.ID3Object;
import Values.Label;
import Values.Prop;
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
    public void edit(EditRequest req) {
        ID3Object id3 = null;
        try {
            id3 = new ID3Object(new File(req.filename));
        } catch (IOException e) {
            System.err.print("Could not edit media properties: "
                + e.getLocalizedMessage());
        }
        editDB(req);
        updateID3(req, id3);
    }

    public void edit(List<EditRequest> requests) {
        for (EditRequest req : requests) {
            edit(req);
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
                    case Prop.COMMENT:
                        id3.delComment();
                        break;
                    case Prop.COMPOSER_NAME:
                        id3.delComposer();
                        break;
                    case Prop.DISC_NO:
                        id3.delDiscNo();
                        break;
                    case Prop.SONG_NAME:
                        id3.delTitle();
                        break;
                    case Prop.TRACK_NUM:
                        id3.delTrack();
                        break;
                    case Prop.YEAR:
                        id3.delYear();
                }
        }
    }

    private void editDB(EditRequest req) {
        StringBuilder query = new StringBuilder();

        sanitizeString(req.value);

        query.append("MATCH (" + req.key + ":" + req.label + ") ");
        query.append("SET " + req.key + "." + req.prop + " = " + "'" + req.value + "'");

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
                    case Prop.COMMENT:
                        id3.setComment(req.value);
                        break;
                    case Prop.COMPOSER_NAME:
                        id3.setComposer(req.value);
                        break;
                    case Prop.DISC_NO:
                        id3.setDiscNo(req.value);
                        break;
                    case Prop.SONG_NAME:
                        id3.setTitle(req.value);
                        break;
                    case Prop.TRACK_NUM:
                        id3.setTrack(req.value);
                        break;
                    case Prop.YEAR:
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
