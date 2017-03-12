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
        //_NEOCONNECTION.getDatabaseConnector().run("MATCH (n) DETACH DELETE n");
    }

    @Test
    public void addFolderRecursively() throws Exception {

        Importer importer = new Importer(_NEOCONNECTION.getDatabaseConnector());

        AudioFileIO.logger.setLevel(Level.OFF);
        importer.addFolderRecursively("E:\\Andre Kostelanetz, Andre Previn, Columbia Symphony Orchestra, Leonard Bernstein & New York Philharmonic");
    }

}