package Utilities;

import java.io.File;
import java.util.List;

/**
 * Handles file io
 */
public class FileHandler {


    /**
     * Recursively searches a file directory structure and returns
     * a list of files that match the given file type.
     *
     * @param root The root directory
     * @param type The file type to match
     * @param filesMatchingType List to add files to
     */
    public static void getAllFilesOfType(File root, String type, List<File> filesMatchingType) {

        if(root == null || filesMatchingType == null) return; //just for safety
        if(root.isDirectory()) {
            for(File file : root.listFiles()) {
                getAllFilesOfType(file, type, filesMatchingType);
            }
        } else if(root.isFile() && root.getName().endsWith(type)) {
            filesMatchingType.add(root);
        }
    }

    public static void getAllFilesOfType(File root, List<String> type, List<File> filesMatchingType) {
        type.forEach(E -> getAllFilesOfType(root, E, filesMatchingType));
    }
}
