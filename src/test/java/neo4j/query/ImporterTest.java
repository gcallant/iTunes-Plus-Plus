package neo4j.query;

import neo4j.connection.NeoCon;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by Kelvinzero on 2/10/2017.
 */
class ImporterTest {

    @Test
    void addRecursively() {
        NeoCon connection = new NeoCon("localhost", 7687, "neo4j", "kaboom");
        Importer importer = new Importer(connection);

        LinkedList<String> extensions = new LinkedList<>();
        File root = new File("C:\\Users\\Josh Cotes\\Music");

        extensions.add(".mp3");
        extensions.add(".mp4");
        extensions.add(".wmv");

        importer.addFolderRecursively(root, extensions);

    }
}