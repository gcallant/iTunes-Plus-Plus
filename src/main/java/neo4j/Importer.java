package neo4j;

import Utilities.ID3Object;
import Utilities.FileHandler;
import Values.Label;
import Values.Property;
import Values.Relation;
import org.jaudiotagger.audio.AudioFileIO;
import org.neo4j.driver.v1.Session;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * Handles importing songs ID3 and file information into database.
 * Adds appropriate nodes and relationships, doesn't add duplicates of
 * either.
 *
 * @author Josh Cotes
 */
public class Importer {

    private int _songCnt = 0;
    private Session _session;

    public Importer(Session session) {
        _session = session;
    }

    /**
     * Retrieves all files from all folders recursively from given file root.
     * Searches each time per file extension in list.
     *
     * @param root - file path
     * @param fileExtensions - list of file extensions to search
     */
    public void addFolderRecursively(String root, List<String> fileExtensions) {

        ArrayList<String> filePaths = new ArrayList<>();
        ArrayList<ID3Object> id3s = new ArrayList<>();
        fileExtensions.parallelStream().forEach(E -> addFolderRecursively(root, E));
        addSongs(filePaths, id3s);
    }

    /**
     * Retrieves all files from all folders recursively from given file root.
     * Searches for file extension.
     *
     * @param root - file path
     * @param fileExtension - file extension to search
     */
    public void addFolderRecursively(String root, String fileExtension){

        ArrayList<String> filePaths = new ArrayList<>();
        ArrayList<ID3Object> id3s = new ArrayList<>();
        FileHandler.getAllFilesAndID3s(new File(root), fileExtension, filePaths, id3s);
        addSongs(filePaths, id3s);
    }

    /**
     * Add all songs from list using id3s to the database.
     *
     * @param songPaths - list of song paths
     * @param id3s - list of id3 tags
     */
    private void addSongs(List<String> songPaths, List<ID3Object> id3s) {

        int i = 0;
        for (String songPath : songPaths)
            addSong(songPath, id3s.get(i++));
        _songCnt += i;
    }

    public int getSongCount() {
        int tmp = _songCnt;
        _songCnt = 0;
        return tmp;
    }

    /**
     * Sanitizes strings for database entry.
     *
     * @param dirty - the dirty string
     * @return - clean string
     */
    private String sanitizeString(String dirty) {
        return dirty.replace('\"', '\'').replace("\\", "//");
    }

    public void createNode(String... triples) {

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


    /**
     * Adds a song's ID3 information to the database with the appropriate relationships.
     * Prevents duplicate node and relationships from being created.
     *
     * @param songPath - path to the song file
     * @param id3 - the ID3Object
     */
    public void addSong(String songPath, ID3Object id3) {

        String album = sanitizeString(id3.getAlbum());
        String artist = sanitizeString(id3.getArtist());
        String composer = sanitizeString(id3.getComposer());
        String comment = sanitizeString(id3.getComment());
        String songName = sanitizeString(id3.getTitle());
        String track = sanitizeString(id3.getTrack());
        String year = sanitizeString(id3.getYear());
        String genre = sanitizeString(id3.getGenre());
        String fileName = sanitizeString(songPath);


        // import artist info //////////
        if (!nodeExists(Label.ARTIST, Property.ARTIST_NAME, artist))
            createNode(Label.ARTIST,
                    Property.ARTIST_NAME, artist);

        // import song info  /////////
        if (!nodeExists(Label.SONGNAME, Property.SONG_NAME, songName))
            createNode(Label.SONGNAME,
                    Property.SONG_NAME, songName,
                    Property.TRACK_NUM, track,
                    Property.COMMENT, comment,
                    Property.YEAR, year,
                    Property.COMPOSER_NAME, composer,
                    Property.FILENAME, fileName);

        ///////// import album info ///////////////
        if (!nodeExists(Label.ALBUM, Property.ALBUM_NAME, album))
            createNode(Label.ALBUM,
                    Property.ALBUM_NAME, album,
                    Property.YEAR, year);

        ////////// import genre info ////////////////
        if (!nodeExists(Label.GENRE, Property.GENRE_NAME, genre))
            createNode(Label.GENRE,
                    Property.GENRE_NAME, genre);

        // create relationship ARTIST/GENRE
        if(!relationshipExists(
                Label.GENRE, Property.GENRE_NAME, genre,
                Relation.HAS_ARTIST,
                Label.ARTIST, Property.ARTIST_NAME, artist
        ))
            createRelationshipReciprocal(
                    Label.GENRE, Property.GENRE_NAME, genre,
                    Relation.HAS_ARTIST,
                    Label.ARTIST, Property.ARTIST_NAME, artist,
                    Relation.HAS_GENRE
            );

        // create relationship SONG/GENRE
        if(!relationshipExists(
                Label.GENRE, Property.GENRE_NAME, genre,
                Relation.HAS_ARTIST,
                Label.SONGNAME, Property.SONG_NAME, songName
        ))
            createRelationshipReciprocal(
                    Label.GENRE, Property.GENRE_NAME, genre,
                    Relation.HAS_SONG,
                    Label.SONGNAME, Property.SONG_NAME, songName,
                    Relation.HAS_GENRE
            );

        // create relationship ALBUM/GENRE
        if(!relationshipExists(
                Label.GENRE, Property.GENRE_NAME, genre,
                Relation.HAS_ARTIST,
                Label.ALBUM, Property.ALBUM_NAME, album
        ))
            createRelationshipReciprocal(
                    Label.GENRE, Property.GENRE_NAME, genre,
                    Relation.HAS_ALBUM,
                    Label.ALBUM, Property.ALBUM_NAME, album,
                    Relation.HAS_GENRE
            );

        // create relationship ARTIST/SONG
        if (!relationshipExists(
                Label.ARTIST, Property.ARTIST_NAME, artist,
                Relation.HAS_SONG,
                Label.SONGNAME, Property.SONG_NAME, songName))
            createRelationshipReciprocal(
                    Label.ARTIST, Property.ARTIST_NAME, artist,
                    Relation.HAS_SONG,
                    Label.SONGNAME, Property.SONG_NAME, songName,
                    Relation.HAS_ARTIST);

        // create relationship ALBUM/ARTIST
        if (!relationshipExists(
                Label.ALBUM, Property.ALBUM_NAME, album, Relation.HAS_ARTIST,
                Label.ARTIST, Property.ARTIST_NAME, artist))
            createRelationshipReciprocal(
                    Label.ALBUM, Property.ALBUM_NAME, album, Relation.HAS_ARTIST,
                    Label.ARTIST, Property.ARTIST_NAME, artist, Relation.HAS_ALBUM);

        // create relationship ALBUM/SONG
        if (!relationshipExists(
                Label.ALBUM, Property.ALBUM_NAME, album, Relation.HAS_SONG,
                Label.SONGNAME, Property.SONG_NAME, songName))
            createRelationshipReciprocal(
                    Label.ALBUM, Property.ALBUM_NAME, album, Relation.HAS_SONG,
                    Label.SONGNAME, Property.SONG_NAME, songName, Relation.HAS_ALBUM
            );
    }

    /**
     * Creates a reciprocal relationship between two nodes given properties.
     *
     * @param label1 - first node label
     * @param property1 - first node subject
     * @param value1 - first node subject value
     * @param relationship - relationship to second node
     * @param label2 - second node label
     * @param property2 - second node subject
     * @param value2 - second node value
     */
    public void createRelationshipReciprocal(String label1, String property1, String value1, String relationship,
                                              String label2, String property2, String value2, String relation2) {

        createRelationship(label1, property1, value1, relationship, label2, property2, value2);
        createRelationship(label2, property2, value2, relation2, label1, property1, value1);
    }

    /**
     * Creates a relationship from the first node to the second.
     *
     * @param label1 - first node label
     * @param property1 - first node subject
     * @param value1 - first node subject value
     * @param relationship - relationship to second node
     * @param label2 - second node label
     * @param property2 - second node subject
     * @param value2 - second node value
     */
    public void createRelationship(String label1, String property1, String value1, String relationship,
                                    String label2, String property2, String value2) {

        _session.run("MATCH  (one:" + label1 + " {" + property1 + ":\"" + value1 + "\"} )" +
                "MATCH  (two:" + label2 + " {" + property2 + ":\"" + value2 + "\"} )" +
                "CREATE (one)-[" + relationship + ":" + relationship + "]" +
                "->(two)");
    }

    /**
     * Returns true if there exists a node with the given parameters
     * @param label - the node label
     * @param subject - the subject
     * @param value - the subject value
     * @return - true if node exists
     */
    public boolean nodeExists(String label, String subject, String value) {

        return _session.run("MATCH (a:" + label + ") WHERE a." + subject + " = {" + subject + "} " +
                        "RETURN a." + subject + " AS " + subject,
                parameters(subject, value)).hasNext();
    }

    /**
     * Returns true if there exists a specific relationship between two nodes.
     *
     * @param label1 - first node label
     * @param property1 - first node subject
     * @param value1 - first node subject value
     * @param relationship - relationship to second node
     * @param label2 - second node label
     * @param property2 - second node subject
     * @param value2 - second node value
     * @return - true if that relationship exists
     */
    public boolean relationshipExists(String label1, String property1, String value1, String relationship,
                                       String label2, String property2, String value2) {

        String query = "MATCH (" + label1 + ":" + label1 + ")-[:" + relationship + "]->(" + label2 + ":" + label2 + ") " +
                "WHERE " + label1 + "." + property1 + " = \"" + value1 + "\" " +
                "AND " + label2 + "." + property2 + " = \"" + value2 + "\" " +
                "RETURN " + label1 + "." + property1 + ", " + label2 + "." + property2;

       return _session.run(query).hasNext();
    }
}
