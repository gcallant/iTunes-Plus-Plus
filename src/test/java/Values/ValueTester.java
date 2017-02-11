package Values;

//links
//neo4j: https://neo4j.com/developer/guide-data-modeling/


import ID3.ID3Object;
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

        String song = id3.getTitle();
        String file = id3.getFile();
        String discNo = id3.getDiscNo();
        String track = id3.getTrack();

//        importAlbum();
//        importArtist();
//        importComment();
//        importComposer();
//        importSong(song, file, discNo, track);
//        importYear();

        importSong(song, file, discNo, track);
        querySong(song);

        _session.close();
        _driver.close();
    }

    private static void init(){
        System.out.println("Accessing database...");
        try{
            _driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("ryan", "o#4uPUm-#BBx7G53Rt3$mj8FYa4!%_"));
            _session = _driver.session();
        } catch(ServiceUnavailableException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }
        System.out.println("Neo4j database active");
    }

    private static void importSong(String song, String file, String discNo, String track){
        StringBuilder importQuery = new StringBuilder();

        importQuery.append("CREATE (" + song + "):" + Label.SONG
                + " {" + Prop.NAME + ": " + song + ", " + Prop.FILENAME+ ": "
                + file);

        if((discNo != null) && (track != null)){
            importQuery.append(", " + Prop.DISC_NO + ": " + discNo);
            importQuery.append(", " + Prop.TRACK + ": " + track + "})");
            _session.run(importQuery.toString(),
                    parameters(Prop.NAME, song, Prop.FILENAME, file, Prop.DISC_NO, discNo, Prop.TRACK, track));
        }
        else if(discNo != null){
            importQuery.append(", " + Prop.DISC_NO + ": " + discNo + "})");
            _session.run(importQuery.toString(),
                    parameters(Prop.NAME, song, Prop.FILENAME, file, Prop.DISC_NO, discNo));
        }
        else if(track != null){
            importQuery.append(", " + Prop.TRACK + ": " + track + "})");
            _session.run(importQuery.toString(),
                    parameters(Prop.NAME, song, Prop.FILENAME, file, Prop.TRACK, track));
        }
        else {
            _session.run(importQuery.toString(),
                    parameters(Prop.NAME, song, Prop.FILENAME, file));
        }
    }

    private static void querySong(String song){
        StringBuilder query = new StringBuilder();

        query.append("MATCH (" + song + ":" + Label.SONG + ")");
        query.append(" WHERE " + song + "." + Prop.NAME + " = {" + song + "}");
        query.append(" RETURN " + song + "." + Prop.FILENAME);

        StatementResult result = _session.run(query.toString(),
                parameters(Prop.NAME, song));
        while (result.hasNext()) {
            Record record = result.next();
            System.out.println(record.get(Prop.FILENAME).asString() + " " + record.get(song).asString());
        }
    }
}
