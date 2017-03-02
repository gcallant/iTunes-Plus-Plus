package Values;

//links
//neo4j: https://neo4j.com/developer/guide-data-modeling/


import Utilities.ID3Object;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import scala.collection.mutable.StringBuilder;

import java.io.File;
import java.io.IOException;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * Created by Ryan on 1/31/2017.
 *
 * The idea behind labels, props, and relations
 *
 * We are using these to stay consistent with our data, as well as organize
 * the music into our database in a sensible fashion. I.e. customers are not
 * going to make query searches on disk or track numbers, so they would be
 * properties. However, someone could want to find artists from the 90s,
 * so year is a label. Relationships describe the connection between
 * the labels.
 *
 */
public class ValueTester {
    private static Driver _driver;
    private static Session _session;

    public static void main(String...args){
        init();

        File mediaFile = new File("src\\main\\resources\\Music\\Blind Melon - No Rain.mp3");
        ID3Object id3 = null;

        try {
            id3 = new ID3Object(mediaFile);
        } catch (IOException e) {
            System.err.println("Unable to read media file");
            System.exit(-1);
        }

        String key = id3.getTitle().replace(" ", "");
        String song = "`" + id3.getTitle() + "`";
        String file = "`" + id3.getFile().replace("\\","\\\\") + "`";
        String discNo = id3.getDiscNo();
        String track = id3.getTrack();

//        importAlbum();
//        importArtist();
//        importComment();
//        importComposer();
//        importYear();

        importSong(key, song, file, discNo, track);

        querySong(key, song);

        _session.close();
        _driver.close();
    }

    private static void init(){
        System.out.println("Accessing database...");
        try{
//            _graphDB = new GraphDatabase();
            _driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("ryan", "ryan"));

            _session = _driver.session();
        } catch(ServiceUnavailableException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }
        System.out.println("Neo4j database active");
    }

    private static void importSong(String key, String song, String file, String discNo, String track){
        StringBuilder importQuery = new StringBuilder();

        importQuery.append("CREATE (" + key + ":" + Label.SONGNAME
                + " {" + Property.FILENAME + ": {" + song + "}, " + Property.FILENAME+ ": {"
                + file + "}");

        if((discNo != null) && (track != null)){
            importQuery.append(", " + Property.DISC_NO + ": {" + discNo + "}");
            importQuery.append(", " + Property.TRACK_NUM + ": {" + track + "}})");
            _session.run(importQuery.toString(),
                    parameters(Property.FILENAME, song, Property.FILENAME, file, Property.DISC_NO, discNo, Property.TRACK_NUM, track));
        }
        else if(discNo != null){
            importQuery.append(", " + Property.DISC_NO + ": {" + discNo + "}})");
            _session.run(importQuery.toString(),
                    parameters(Property.FILENAME, song, Property.FILENAME, file, Property.DISC_NO, discNo));
        }
        else if(track != null){
            importQuery.append(", " + Property.TRACK_NUM + ": {" + track + "}})");
            _session.run(importQuery.toString(),
                    parameters(Property.FILENAME, song, Property.FILENAME, file, Property.TRACK_NUM, track));
        }
        else {
            _session.run(importQuery.toString(),
                    parameters(Property.FILENAME, song, Property.FILENAME, file));
        }
        System.out.println(importQuery.toString());
    }

    private static void querySong(String key, String song){
        StringBuilder query = new StringBuilder();

//        query.append("MATCH (" + song + ")");
        query.append("MATCH (" + key + ":" + Label.SONGNAME + ")");
        query.append(" WHERE (" + song + ")." + Property.FILENAME + " = {" + song + "}");
        query.append(" RETURN (" + song + ")." + Property.FILENAME + " AS " + Property.FILENAME);

        System.out.println(query.toString());

        StatementResult result = _session.run(query.toString(),
                parameters(Property.FILENAME, song));

        while (result.hasNext()) {
            Record record = result.next();
            System.out.println(record.get(Property.FILENAME).asString());
        }
    }
}
