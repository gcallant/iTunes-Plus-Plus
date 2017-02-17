package neo4j;

import ID3.ID3Object;
import Utilities.FileHandler;
import Values.Label;
import Values.Prop;
import Values.Relation;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * @author Josh Cotes
 */
public class Importer {

    private Session _session;

    public Importer(Session session) {
        _session = session;
    }

    public void addFolderRecursively(String root, List<String> fileExtensions) {

        ArrayList<String> filePaths = new ArrayList<>();
        ArrayList<ID3Object> id3s = new ArrayList<>();

        fileExtensions.parallelStream().forEach(E -> FileHandler.getAllFilesAndID3s(new File(root), E, filePaths, id3s));
        addSongs(filePaths, id3s);
    }

    private void addSongs(List<String> songPaths, List<ID3Object> id3s) {

        int i = 0;
        for (String songPath : songPaths)
            addSong(songPath, id3s.get(i++));
    }

    private String sanitizeString(String dirty) {
        return dirty.replace('\"', '\'').replace("\\", "//");
    }

    private void createNode(String... triples) {

        StringBuilder query = new StringBuilder();

        query.append("CREATE (" + triples[0] + ":" + triples[0] + " {");
        for (int i = 2; i < triples.length; i++) {
            if (i % 2 == 0)
                query.append(triples[i - 1] + ":\"" + triples[i] + "\"");
            if (i % 2 == 1 && i < triples.length - 1)
                query.append(",");
        }
        query.append("})");
        _session.run(query.toString());
    }


    private void addSong(String songPath, ID3Object id3) {

        String album = sanitizeString(id3.getAlbum());
        String artist = sanitizeString(id3.getArtist());
        String composer = sanitizeString(id3.getComposer());
        String comment = sanitizeString(id3.getComment());
        String songName = sanitizeString(id3.getTitle());
        String track = sanitizeString(id3.getTrack());
        String year = sanitizeString(id3.getYear());
        String fileName = sanitizeString(songPath);

        // import artist info //////////
        if (!nodeExists(Label.ARTIST, Prop.ARTIST_NAME, artist))
            createNode(Label.ARTIST,
                    Prop.ARTIST_NAME, artist);

        // import song info  /////////
        if (!nodeExists(Label.SONGNAME, Prop.SONG_NAME, songName))
            createNode(Label.SONGNAME,
                    Prop.SONG_NAME, songName,
                    Prop.TRACK_NUM, track,
                    Prop.COMMENT, comment,
                    Prop.YEAR, year,
                    Prop.COMPOSER_NAME, composer,
                    Prop.FILENAME, fileName);

        ///////// import album info ///////////////
        if (!nodeExists(Label.ALBUM, Prop.ALBUM_NAME, album))
            createNode(Label.ALBUM,
                    Prop.ALBUM_NAME, album,
                    Prop.YEAR, year);

        // create relationship ARTIST/SONG
        if (!relationshipExists(
                Label.ARTIST, Prop.ARTIST_NAME, artist, Relation.HAS_SONG,
                Label.SONGNAME, Prop.SONG_NAME, songName))
            createRelationshipReciprocal(
                    Label.ARTIST, Prop.ARTIST_NAME, artist, Relation.HAS_SONG,
                    Label.SONGNAME, Prop.SONG_NAME, songName, Relation.HAS_ARTIST);

        // create relationship ALBUM/ARTIST
        if (!relationshipExists(
                Label.ALBUM, Prop.ALBUM_NAME, album, Relation.HAS_ARTIST,
                Label.ARTIST, Prop.ARTIST_NAME, artist))
            createRelationshipReciprocal(
                    Label.ALBUM, Prop.ALBUM_NAME, album, Relation.HAS_ARTIST,
                    Label.ARTIST, Prop.ARTIST_NAME, artist, Relation.HAS_ALBUM);

        // create relationship ALBUM/SONG
        if (!relationshipExists(
                Label.ALBUM, Prop.ALBUM_NAME, album, Relation.HAS_SONG,
                Label.SONGNAME, Prop.SONG_NAME, songName))
            createRelationshipReciprocal(
                    Label.ALBUM, Prop.ALBUM_NAME, album, Relation.HAS_SONG,
                    Label.SONGNAME, Prop.SONG_NAME, songName, Relation.HAS_ALBUM
            );
    }

    private void createRelationshipReciprocal(String label1, String property1, String value1, String relation,
                                              String label2, String property2, String value2, String relation2) {

        createRelationship(label1, property1, value1, relation, label2, property2, value2);
        createRelationship(label2, property2, value2, relation2, label1, property1, value1);
    }

    private void createRelationship(String label, String property1, String value1, String relation,
                                    String label2, String property2, String value2) {

        _session.run("MATCH  (one:" + label + " {" + property1 + ":\"" + value1 + "\"} )" +
                "MATCH  (two:" + label2 + " {" + property2 + ":\"" + value2 + "\"} )" +
                "CREATE (one)-[" + relation + ":" + relation + "]" +
                "->(two)");
    }

    private boolean nodeExists(String label, String subject, String value) {

        String query = "MATCH (a:" + label + ") WHERE a." + subject + " = {" + subject + "} " +
                "RETURN a." + subject + " AS " + subject;
        StatementResult result = _session.run("MATCH (a:" + label + ") WHERE a." + subject + " = {" + subject + "} " +
                        "RETURN a." + subject + " AS " + subject,
                parameters(subject, value));
        if (result.hasNext())
            return true;
        return false;
    }

    private boolean relationshipExists(String label, String subject, String value, String relationship,
                                       String label2, String subject2, String value2) {

        String query = "MATCH (" + label + ":" + label + ")-[:" + relationship + "]->(" + label2 + ":" + label2 + ") " +
                "WHERE " + label + "." + subject + " = \"" + value + "\" " +
                "AND " + label2 + "." + subject2 + " = \"" + value2 + "\" " +
                "RETURN " + label + "." + subject + ", " + label2 + "." + subject2;

        StatementResult result = _session.run(query);

        return result.hasNext();
    }
}
