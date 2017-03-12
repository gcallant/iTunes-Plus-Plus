package Utilities;

import Values.Extensions;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Handles file io and ID3 tag retrieval
 *
 * @author Josh Cotes
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

        if(root.isDirectory()) {
            for(File file : root.listFiles()) {
                getAllFilesOfType(file, type, filesMatchingType);
            }
        } else if(root.isFile() && root.getName().endsWith(type)) {
            filesMatchingType.add(root);
        }
    }

    /**
     * Get all file paths and associated ID3 tags using recursive folder search
     *
     * @param root - the root directory
     * @param songPaths - empty list to add paths
     */
    public static void getAllMusicFiles(File root, SharedQueue<String> songPaths){

        if(root.isDirectory()){
            for(File file : root.listFiles()) {
                getAllMusicFiles(file, songPaths);
            }
            return;
        }

        for(String ext : Extensions.SUPPORTED) {
            String filepath = root.getAbsolutePath();
            if (filepath.endsWith(ext)) {
                songPaths.enqueue(filepath);
                break;
            }
        }
    }

    public static int getMusicFileCount(File root){
        return getMusicFileCount(root, 0);
    }

    /**
     * Get media file count
     *
     * @param root - the root directory
     * @param count - the number of files, starts at zero
     */
    private static int getMusicFileCount(File root, int count) {

        if (root.isDirectory()) {
            for (File file : root.listFiles()) {
                for (String ext : Extensions.SUPPORTED) {
                    String filepath = root.getName();
                    if (filepath.endsWith(ext)) {
                        return getMusicFileCount(root, ++count);
                    }
                }

                return getMusicFileCount(file, count);
            }
        }

        return count;
    }
}
