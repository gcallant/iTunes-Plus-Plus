package neo4j.query;

import ID3.ID3Object;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Import;
import file_io.FileHandler;
import neo4j.connection.NeoCon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by Kelvinzero on 2/10/2017.
 */
public class Importer {

    private NeoCon _neoCon;

    public Importer(NeoCon _connection){
        _neoCon = _connection;
    }

    public void addFolderRecursively(File root, List<String> fileExtensions) {

        ArrayList<File> files = new ArrayList<>();
        fileExtensions.parallelStream().forEach(E -> FileHandler.getAllFilesOfType(root, E, files));
        addSongs(files);
    }

    private void addSongs(List<File> songs) {
        songs.forEach(this::addSong);
    }

    private void addSong(File song){

        try {
            ID3Object id3Object = new ID3Object(song);
            //TODO: Add code to add song info to db (Importer.query(String query))

            System.out.println(id3Object.getArtist());
        }
        catch (IOException filenotfound$){
            filenotfound$.printStackTrace();
        }
    }

    private String makeAddQuery(ID3Object id3Object){
                return "";
    }
}
