package Utilities;

import ID3.ID3Object;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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

        if(root.isDirectory()) {
            for(File file : root.listFiles()) {
                getAllFilesOfType(file, type, filesMatchingType);
            }
        } else if(root.isFile() && root.getName().endsWith(type)) {
            filesMatchingType.add(root);
        }
    }

    public static void getAllFilesAndID3s(File root, String type, ArrayList<String> songPaths, ArrayList<ID3Object> id3s){
        if(root.isDirectory()) {
            for(File file : root.listFiles())
                getAllFilesAndID3s(file, type, songPaths, id3s);

        } else if(root.isFile() && root.getName().endsWith(type)) {
            songPaths.add(root.getAbsolutePath());
            id3s.add(getID3fromFile(root));
        }
    }

    public static void getAllSongID3s(List<ID3Object> id3List, List<File> songFiles){
            songFiles.forEach(E -> id3List.add(getID3fromFile(E)));
    }

    private static ID3Object getID3fromFile(File songFile){
        try {
            return new ID3Object(songFile);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
