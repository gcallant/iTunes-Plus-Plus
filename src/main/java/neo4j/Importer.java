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

    public void addFolderRecursively(File root, List<String> fileExtensions) {

        ArrayList<File> files = new ArrayList<>();
        fileExtensions.parallelStream().forEach(E -> FileHandler.getAllFilesOfType(root, E, files));
        addSongs(files);
    }

    private void addSongs(List<File> songs) {
        songs.forEach(this::addSong);
    }

    private String sanitizeString(String dirty) {

        StringBuilder sb = new StringBuilder();
        for (char c : dirty.toCharArray()) {
            if (c == '"')
                sb.append('\'');
            else
                sb.append(c);
        }
        return sb.toString();
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

    private void createNode(String... triples) {

        StringBuilder query = new StringBuilder();

        query.append("CREATE (" + triples[0] + ":" + triples[0] + " {");
        for (int i = 2; i < triples.length; i++) {
            if (i % 2 == 0)
                query.append(triples[i - 1] + ":\"" + triples[i] + "\"");
            if (i % 2 == 1 && i < triples.length - 1)
                query.append(", \n");
        }
        query.append("})");
        _session.run(query.toString());
    }


    private void addSong(File song) {

        try {
            ID3Object id3 = new ID3Object(song);

            String album = sanitizeString(id3.getAlbum());
            String artist = sanitizeString(id3.getArtist());
            String composer = sanitizeString(id3.getComposer());
            String comment = sanitizeString(id3.getComment());
            String songName = sanitizeString(id3.getTitle());
            String track = sanitizeString(id3.getTrack());
            String year = sanitizeString(id3.getYear());

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
                        Prop.YEAR, year);
                //TODO: add file info to song

            ///////// import album info ///////////////
            if (!nodeExists(Label.ALBUM, Prop.ALBUM_NAME, album))
                createNode(Label.ALBUM,
                        Prop.ALBUM_NAME, album,
                        Prop.YEAR, year);

            ///////// import composer info //////////////
            if(!nodeExists(Label.COMPOSER, Prop.COMPOSER_NAME, composer))
                createNode(Label.COMPOSER,
                        Prop.COMPOSER_NAME, composer);

            createRelationshipReciprocal(
                    Label.ARTIST, Prop.ARTIST_NAME, artist, Relation.HAS_SONG,
                    Label.SONGNAME, Prop.SONG_NAME, songName, Relation.HAS_ARTIST
            );

            createRelationshipReciprocal(
                    Label.ALBUM, Prop.ALBUM_NAME, album, Relation.HAS_ARTIST,
                    Label.ARTIST, Prop.ARTIST_NAME, artist, Relation.HAS_ALBUM
            );

            createRelationshipReciprocal(
                    Label.ALBUM, Prop.ALBUM_NAME, album, Relation.HAS_SONG,
                    Label.SONGNAME, Prop.SONG_NAME, songName, Relation.HAS_ALBUM
            );

            createRelationshipReciprocal(
                    Label.COMPOSER, Prop.COMPOSER_NAME, composer, Relation.HAS_SONG,
                    Label.SONGNAME, Prop.SONG_NAME, songName, Relation.HAS_COMPOSER
            );

            createRelationshipReciprocal(
                    Label.COMPOSER, Prop.COMPOSER_NAME, composer, Relation.HAS_SONG,
                    Label.ALBUM, Prop.ALBUM_NAME, album, Relation.HAS_ALBUM
            );
        } catch (IOException filenotfound$) {
            filenotfound$.printStackTrace();
        }
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
}
