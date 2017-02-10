package file_io;

import org.junit.jupiter.api.Test;
import org.neo4j.test.mockito.mock.Link;

import java.io.File;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Kelvinzero on 2/10/2017.
 */
class FileHandlerTest {
    @Test
    void getAllFilesOfType() {
        LinkedList<File> files = new LinkedList<>();
        File root = new File("C:\\Users\\Josh Cotes\\Downloads");
        FileHandler.getAllFilesOfType(root, ".pdf", files);
    }

}