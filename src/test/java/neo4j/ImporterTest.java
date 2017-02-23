package neo4j;

import org.jaudiotagger.audio.AudioFileIO;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.logging.Level;


/**
 * Created by Josh on 2/16/2017.
 */
public class ImporterTest {

    DatabaseManager _NEOCONNECTION;

    @Before
    public void setUp() throws Exception{
        _NEOCONNECTION = DatabaseManager.getInstance(DatabaseManager._URL, DatabaseManager._PORT, "iTunes", "iTunes");
        _NEOCONNECTION.getDatabaseConnector().run("MATCH (n) DETACH DELETE n");
    }

    @Test
    public void addFolderRecursively() throws Exception {

        Importer importer = new Importer(_NEOCONNECTION.getDatabaseConnector());
        ArrayList<String> songFiles = new ArrayList<>();
        songFiles.add(".mp3");
        songFiles.add(".mp4");
        songFiles.add(".wmv");
        songFiles.add(".mpeg");

        AudioFileIO.logger.setLevel(Level.OFF);
        importer.addFolderRecursively("E:\\Aaron Goldberg", songFiles);
    }

}