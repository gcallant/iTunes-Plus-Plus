package file_io;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedList;

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