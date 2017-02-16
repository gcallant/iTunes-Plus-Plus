package neo4j;

import ID3.ID3Object;
import Utilities.FileHandler;
import Values.Label;
import Values.Prop;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * Created by Kelvinzero on 2/10/2017.
 */
public class Importer {

    private Session _session;

    public Importer(Session session){
        _session = session;
    }

    public void addFolderRecursively(File root, List<String> fileExtensions) {

        ArrayList<File> files = new ArrayList<>();
        fileExtensions.parallelStream().forEach(E -> FileHandler.getAllFilesOfType(root, E, files));
        addSongs(files);
    }

    private void addSongs(List<File> songs) {
        songs.forEach(this::addSong);
    }

    /*
    System.out.println("Album: " + id3.getAlbum());
		System.out.println("Artist: " + id3.getArtist());
		System.out.println("Comment: " + id3.getComment());
		System.out.println("Composer: " + id3.getComposer());
		System.out.println("Title: " + id3.getTitle());
		System.out.println("Track: " + id3.getTrack());
		System.out.println("Year: " + id3.getYear());
		System.out.println("ALL: " + id3.getAll());
*/

    private void createNode(String label, String subject, String value){
        _session.run("CREATE (a:"+ Label.ARTIST + " {" +  Prop.NAME + ": {" + Prop.NAME + "} })",
                parameters(Prop.NAME, value));
    }


    private void addSong(File song){

        try {
            ID3Object id3 = new ID3Object(song);

            // if id3 artist info present
            if(id3.getArtist() != null && !nodeExists(Label.ARTIST, Prop.NAME, id3.getArtist()))
                createNode(Label.ARTIST, Prop.NAME, id3.getArtist());

            // if id3 track info present
            if(id3.getTrack() != null && !nodeExists(Label.SONG, Prop.NAME, id3.getTrack())){
                createNode(Label.SONG, Prop.NAME, id3.getTrack());
                if(id3.getComment() != null){ // if song has comment
                    _session.run("CREATE (a:"+ Label.SONG + " {" +  Prop.NAME + ": {" + Prop.NAME + "}, "+
                                    Prop.COMMENT + ": {"+ id3.getComment() +"  } })",
                            parameters(Prop.NAME, id3.getTrack(), Prop.COMMENT, id3.getComment()));
                }
            }

        }
        catch (IOException filenotfound$){
            filenotfound$.printStackTrace();
        }
    }

    private boolean nodeExists(String label, String subject, String value){

        String query = "MATCH (a:" + label + ") WHERE a."+subject+" = {"+subject+"} " +
                "RETURN a."+subject+" AS "+ subject;
        StatementResult result = _session.run("MATCH (a:" + label + ") WHERE a."+subject+" = {"+subject+"} " +
                        "RETURN a."+subject+" AS "+ subject,
                parameters(subject, value));
        if(result.hasNext())
            return true;
        return false;
    }
}
