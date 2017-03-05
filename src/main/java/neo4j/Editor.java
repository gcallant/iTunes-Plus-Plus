package neo4j;

import Utilities.ID3Object;
import Values.Label;
import Values.Property;
import Values.Relation;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan on 2/19/2017.
 *
 * This is a simplified Editor class. It can edit by album, artist, song, and
 * genre. A more complicated and usable editor would allow a client to change
 * multiple diverse songs at once. This task can get quite daunting and is
 * outside of the scope of this project.
 *      I.e. User selects multiple songs and changes album
 *      - The ideal course of action would be the following:
 *          - Search for album name in DB and see if any exist
 *          - Prompt user which album they prefer or create a new one
 *          - Redirect all relationships from selected songs and artists
 *          - Delete old albums if no song relationships exist
 *          - Update all corresponding id3 tags
 */
public class Editor {
//-----------------------------------------------------------------------------
// PARAMS
//-----------------------------------------------------------------------------
    private Session _session;

//-----------------------------------------------------------------------------
// CONSTRUCTORS
//-----------------------------------------------------------------------------
    public Editor(Session session) {
        _session = session;
    }

//-----------------------------------------------------------------------------
// PUBLIC METHODS
//-----------------------------------------------------------------------------
    public void editAlbum(EditRequest req) {
        ID3Object id3;
        req.value = sanitizeString(req.value);
        editDB(req);
        if(req.label.equals(Label.SONGNAME)){
            updateID3(req);
        } else {

        }
    }

    public void editSong(EditRequest req) {
        req.value = sanitizeString(req.value);
        editDB(req);
        updateID3(req);
    }

    public void edit(List<EditRequest> requests) {
        for (EditRequest req : requests) {
            editSong(req);
        }
    }

//-----------------------------------------------------------------------------
// PRIVATE METHODS
//-----------------------------------------------------------------------------
    private void deleteHelper(EditRequest req, ID3Object id3){
        switch (req.label) {
            case Label.ALBUM:
                id3.delAlbum();
                break;
            case Label.ARTIST:
                id3.delArtist();
                break;
            case Label.GENRE:
                id3.delGenre();
                break;
            case Label.SONGNAME:
                switch (req.prop) {
                    case Property.COMMENT:
                        id3.delComment();
                        break;
                    case Property.COMPOSER_NAME:
                        id3.delComposer();
                        break;
                    case Property.DISC_NO:
                        id3.delDiscNo();
                        break;
                    case Property.SONG_NAME:
                        id3.delTitle();
                        break;
                    case Property.TRACK_NUM:
                        id3.delTrack();
                        break;
                    case Property.YEAR:
                        id3.delYear();
                }
        }
    }

    private void editDB(EditRequest req) {
        String tempKey, newKey;
        StringBuilder query = new StringBuilder();

        if(!req.label.equals(Label.SONGNAME)) {
            tempKey = findByNodeID(req.ID, req.label).get(0);
            createIfNotExist(req);
            newKey = findIDByProperty(req.prop, req.value).get(0);
            attachRelations(req.ID, newKey, req.label)
        } else {
            query.append("MATCH (n) WHERE ID(n)=").append(req.ID).append(" ");
            query.append("SET n.").append(req.prop).append(" = '").append(req.value).append("'");
            query.append("RETURN n");
        }
        _session.run(query.toString());
    }

    ////////////////////////////////// Temporary Searching Code ///////////////////////////////////////
    /**
     * Adds and/or sets values based on a edit request
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
                Relation.HAS_SONG,
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
                Relation.HAS_ALBUM,
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

    public void createIfNotExist(EditRequest req){
        Importer importer = new Importer(_session);
        if (!importer.nodeExists(req.label, req.prop, req.value)) {
            importer.createNode(req.label, req.prop, req.value);

        }
    }
    //finds ID(s) of corresponding node(s) by relation(s) to node ID
    public ArrayList<String> findByNodeID(String ID, String relation){
        int i = 0;
        ArrayList<String> IDList = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("MATCH (n)-[:").append(relation).append("]->(a)")
                .append("WHERE ID(n)=").append(ID).append(" RETURN a");

        StatementResult result = _session.run(query.toString());

        while(result.hasNext()){
            Record record = result.next();
            IDList.add(Integer.toString(record.get(i++).asInt()));
        }

        return (i == 0) ? null : IDList;
    }

    public ArrayList<String> findIDByProperty(String prop, String value){
        int i = 0;
        String label = getLabelByProperty(prop);
        ArrayList<String> IDList = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("MATCH (n:").append(label).append(") where (n.").append(prop).append("=\"")
                .append(value).append("\") RETURN id(n)");
        StatementResult result = _session.run(query.toString());

        while(result.hasNext()) {
            Record record = result.next();
            IDList.add(Integer.toString(record.get(i++).asInt()));
        }

        return (i == 0) ? null : IDList;
    }

    public String getLabelByProperty(String prop){
        switch (prop) {
            case Property.ALBUM_NAME:
                return Label.ALBUM;
            case Property.ARTIST_NAME:
                return Label.ARTIST;
            case Property.GENRE_NAME:
                return Label.GENRE;
            default:
                return Label.SONGNAME;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    private String sanitizeString(String dirty) {
            return dirty.replace('\"', '\'').replace("\\", "//");
    }

    private void setHelper(EditRequest req, ID3Object id3){
        switch (req.label) {
            case Label.ALBUM:
                id3.setAlbum(req.value);
                break;
            case Label.ARTIST:
                id3.setArtist(req.value);
                break;
            case Label.GENRE:
                id3.setGenre(req.value);
                break;
            case Label.SONGNAME:
                switch (req.prop) {
                    case Property.COMMENT:
                        id3.setComment(req.value);
                        break;
                    case Property.COMPOSER_NAME:
                        id3.setComposer(req.value);
                        break;
                    case Property.DISC_NO:
                        id3.setDiscNo(req.value);
                        break;
                    case Property.SONG_NAME:
                        id3.setTitle(req.value);
                        break;
                    case Property.TRACK_NUM:
                        id3.setTrack(req.value);
                        break;
                    case Property.YEAR:
                        id3.setYear(req.value);
                }
        }
    }

    private void updateID3(EditRequest req) {
        ID3Object id3 = req.getID3(_session);
        if (id3 == null) {
            return;
        }
        else if(req.value.equals("") || req.value == null){
            deleteHelper(req, id3);
        }
        else {
            setHelper(req, id3);
        }
    }
}
