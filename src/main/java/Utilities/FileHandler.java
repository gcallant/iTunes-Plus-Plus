package Utilities;

import java.io.File;
import java.util.ArrayList;
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
     * @param type - file type to search for
     * @param songPaths - empty list to add paths
     * @param id3s - empty list to add ID3's
     */
    public static void getAllFilesAndID3s(File root, String type, ArrayList<String> songPaths, ArrayList<ID3Object> id3s){

        if(root.isDirectory())
            for(File file : root.listFiles())
                getAllFilesAndID3s(file, type, songPaths, id3s);

        else if(root.isFile() && root.getName().endsWith(type)) {
            songPaths.add(root.getAbsolutePath());
            id3s.add(getID3fromFile(root));
        }
    }

    /**
     * Gets an ID3object from given songFile
     *
     * @param songFile - song file pointer
     * @return - the ID3Object
     */
    private static ID3Object getID3fromFile(File songFile){
        try {
            return new ID3Object(songFile);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
