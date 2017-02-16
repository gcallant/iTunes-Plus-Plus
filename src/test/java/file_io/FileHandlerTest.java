package file_io;

import Utilities.FileHandler;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedList;

/**
 * @author Josh Cotes
 */
class FileHandlerTest {

    @Test
    void getAllFilesOfType() {
        LinkedList<File> files = new LinkedList<>();
        File root = new File("C:\\Users\\Josh Cotes\\Downloads");
        FileHandler.getAllFilesOfType(root, ".mp3", files);
    }
}