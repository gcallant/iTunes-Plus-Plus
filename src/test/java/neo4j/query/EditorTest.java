package neo4j.query;

import Utilities.ID3Object;
import Values.Label;
import Values.Property;
import neo4j.DatabaseManager;
import neo4j.EditRequest;
import neo4j.Editor;
import Values.PropertySet;
import org.junit.Before;
import org.junit.Test;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.io.File;
import java.io.IOException;

/**
 * Created by Ryan on 3/4/2017.
 * Used to test the Editor class functions.
 */
public class EditorTest {
    private Session _session;
    private Editor _editor;
    private final String SONG_NAME = "Vaz Deferenz";

    @Before
    public void setUp() throws Exception{
        DatabaseManager dbm = DatabaseManager.getInstance(DatabaseManager._URL, DatabaseManager._PORT, "iTunes", "iTunes");
        _session = dbm.getDatabaseConnector();
        _editor = new Editor(_session);
    }

    @Test
    public void editSong(){
        EditRequest req;
        ID3Object id3 = null;

        String songID = findIDByProperty(Label.SONGNAME, new PropertySet(Property.SONG_NAME, SONG_NAME));
        String path = getPropertyByID(songID, Property.FILENAME);

        try{
            id3 = new ID3Object(new File(path));
        } catch(IOException e){
            System.err.print("Failed to open file: ");
            System.out.println(e.getLocalizedMessage());
        }
        req = new EditRequest(id3);
//        req.title = "Outside";
        req.album = "newAlbum";
        req.artist = "newArtist";
        req.genre = "newGenre";
        req.year = "nyear";

        _editor.edit(req, id3);
    }

    private String findIDByProperty(String label, PropertySet set){
        String ID;
        StringBuilder query = new StringBuilder();
        query.append("MATCH (n:").append(label).append(" ")
                .append("{").append(set.prop).append(": \"")
                .append(set.val).append("\"} ").append(") RETURN id(n)");

        StatementResult result = _session.run(query.toString());
        Record record = result.next();
        ID = Integer.toString(record.get(0).asInt());

        return ID;
    }

    private String getPropertyByID(String ID, String prop){
        StringBuilder query = new StringBuilder();

        query.append("MATCH (n) WHERE ID(n)=")
                .append(ID).append(" RETURN n.").append(prop);

        StatementResult result = _session.run(query.toString());
        Record record = result.next();

        return record.get(0).asString();
    }
}
