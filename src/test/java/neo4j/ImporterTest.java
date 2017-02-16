package neo4j;

import io.netty.util.internal.RecyclableArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import scala.collection.mutable.ArrayLike;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.neo4j.driver.v1.Values.parameters;

/**
 * Created by Josh on 2/16/2017.
 */
public class ImporterTest {

    DatabaseManager _NEOCONNECTION;

    @Before
    public void setUp() throws Exception{
        _NEOCONNECTION = DatabaseManager.getInstance(DatabaseManager._URL, DatabaseManager._PORT, "neo4j", "kaboom");
    }

    @Test
    public void addFolderRecursively() throws Exception {

        Importer importer = new Importer(_NEOCONNECTION.getDatabaseConnector());
        ArrayList<String> songFiles = new ArrayList<>();
        songFiles.add(".mp3");
        songFiles.add(".mp4");
        songFiles.add(".wmv");
        songFiles.add(".mpeg");

        importer.addFolderRecursively(new File("C:\\Users\\Josh Cotes\\Music"), songFiles);
    }

}