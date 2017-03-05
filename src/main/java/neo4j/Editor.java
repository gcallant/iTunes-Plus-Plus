package neo4j;

import Utilities.ID3Object;
import Values.Label;
import Values.Property;
import Values.PropertySet;
import Values.Relation;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.util.LinkedList;
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
    public void edit(EditRequest req, ID3Object id3) {
        sanitize(req);
        editDB(req);
        updateID3(req,id3);
    }

    public void edit(List<EditRequest> requests, List<ID3Object> id3List) {
        int sz = requests.size();
        if(sz != id3List.size()){
            throw new IllegalArgumentException("Different list sizes");
        }
        for (int i=0;i<sz;i++) {
            EditRequest req = requests.get(i);
            ID3Object id3 = id3List.get(i);
            edit(req,id3);
        }
    }

//-----------------------------------------------------------------------------
// PRIVATE METHODS
//-----------------------------------------------------------------------------
    private void createRelationships(Importer importer, EditRequest req){
        // create relationship ARTIST/GENRE -----------------------------------
        if(!importer.relationshipExists(
                Label.GENRE, Property.GENRE_NAME, req.genre,
                Relation.HAS_ARTIST,
                Label.ARTIST, Property.ARTIST_NAME, req.artist
        )) {
            importer.createRelationshipReciprocal(
                    Label.GENRE, Property.GENRE_NAME, req.genre,
                    Relation.HAS_ARTIST,
                    Label.ARTIST, Property.ARTIST_NAME, req.artist,
                    Relation.HAS_GENRE
            );
        }

        // create relationship SONG/GENRE -------------------------------------
        if(!importer.relationshipExists(
                Label.GENRE, Property.GENRE_NAME, req.genre,
                Relation.HAS_SONG,
                Label.SONGNAME, Property.SONG_NAME, req.title
        ))
            importer.createRelationshipReciprocal(
                    Label.GENRE, Property.GENRE_NAME, req.genre,
                    Relation.HAS_SONG,
                    Label.SONGNAME, Property.SONG_NAME, req.title,
                    Relation.HAS_GENRE
            );

        // create relationship ALBUM/GENRE ------------------------------------
        if(!importer.relationshipExists(
                Label.GENRE, Property.GENRE_NAME, req.genre,
                Relation.HAS_ALBUM,
                Label.ALBUM, Property.ALBUM_NAME, req.album
        ))
            importer.createRelationshipReciprocal(
                    Label.GENRE, Property.GENRE_NAME, req.genre,
                    Relation.HAS_ALBUM,
                    Label.ALBUM, Property.ALBUM_NAME, req.album,
                    Relation.HAS_GENRE
            );

        // create relationship ARTIST/SONG ------------------------------------
        if (!importer.relationshipExists(
                Label.ARTIST, Property.ARTIST_NAME, req.artist,
                Relation.HAS_SONG,
                Label.SONGNAME, Property.SONG_NAME, req.title))
            importer.createRelationshipReciprocal(
                    Label.ARTIST, Property.ARTIST_NAME, req.artist,
                    Relation.HAS_SONG,
                    Label.SONGNAME, Property.SONG_NAME, req.title,
                    Relation.HAS_ARTIST
            );

        // create relationship ALBUM/ARTIST -----------------------------------
        if (!importer.relationshipExists(
                Label.ALBUM, Property.ALBUM_NAME, req.album, Relation.HAS_ARTIST,
                Label.ARTIST, Property.ARTIST_NAME, req.artist))
            importer.createRelationshipReciprocal(
                    Label.ALBUM, Property.ALBUM_NAME, req.album, Relation.HAS_ARTIST,
                    Label.ARTIST, Property.ARTIST_NAME, req.artist, Relation.HAS_ALBUM
            );

        // create relationship ALBUM/SONG -------------------------------------
        if (!importer.relationshipExists(
                Label.ALBUM, Property.ALBUM_NAME, req.album, Relation.HAS_SONG,
                Label.SONGNAME, Property.SONG_NAME, req.title))
            importer.createRelationshipReciprocal(
                    Label.ALBUM, Property.ALBUM_NAME, req.album, Relation.HAS_SONG,
                    Label.SONGNAME, Property.SONG_NAME, req.title, Relation.HAS_ALBUM
            );
    }

    private void updateID3(EditRequest req, ID3Object id3){
        id3.setAlbum(req.album);
        id3.setArtist(req.artist);
        id3.setComment(req.comment);
        id3.setComposer(req.composer);
        id3.setDiscNo(req.discNo);
        id3.setGenre(req.genre);
        id3.setTitle(req.title);
        id3.setTrack(req.track);
        id3.setYear(req.year);
    }

    private void editDB(EditRequest req) {
        updateDB(req);
        delOldNodes(req.getOriginal());
    }

    /**
     * Deletes nodes that no longer have any song relationships.
     * Also deletes song that no longer have any relationships.
     * @param req Old values that may be stale.
     */
    private void delOldNodes(EditRequest req){
        Deleter deleter = new Deleter(_session);
        String songID = findIDByProperty(Label.SONGNAME,
                new PropertySet(Property.SONG_NAME, req.title));
        String albumID = findIDByProperty(Label.ALBUM,
                new PropertySet(Property.ALBUM_NAME, req.album));
        String artistID = findIDByProperty(Label.ARTIST,
                new PropertySet(Property.ARTIST_NAME, req.artist));
        String genreID = findIDByProperty(Label.GENRE,
                new PropertySet(Property.GENRE_NAME, req.genre));

        deleter.deleteSongOnEmpty(songID);
        deleter.deleteOnEmpty(albumID);
        deleter.deleteOnEmpty(artistID);
        deleter.deleteOnEmpty(genreID);
    }

    /**
     * Adds and/or sets values based on a edit request
     *
     * @param req The values for the fields to change
     */
    private void updateDB(EditRequest req) {
        EditRequest original = req.getOriginal();
        Deleter deleter = new Deleter(_session);
        Importer importer = new Importer(_session);

        // set song -----------------------------------------------------------
        if (!importer.nodeExists(Label.SONGNAME, Property.SONG_NAME, req.title)) {
            if(req.title != null){if(!req.title.equals("")) {
                importer.createNode(Label.SONGNAME, Property.SONG_NAME, req.title);
            }}
        }
        String newSongID = findIDByProperty(Label.SONGNAME,
                new PropertySet(Property.SONG_NAME, req.title));
        String oldSongID = findIDByProperty(Label.SONGNAME,
                new PropertySet(Property.SONG_NAME, original.title));
        LinkedList<PropertySet> songProperties = PropertySet.populateSongProperties(req);
        setPropertyByID(newSongID, songProperties);

        // set artist ---------------------------------------------------------
        if (!importer.nodeExists(Label.ARTIST, Property.ARTIST_NAME, req.artist)) {
            if(req.artist != null){if(!req.artist.equals("")) {
                importer.createNode(Label.ARTIST, Property.ARTIST_NAME, req.artist);
            }}
        }
        if(!req.sameArtist()){
            String artistID = findIDByProperty(Label.ARTIST,
                    new PropertySet(Property.ARTIST_NAME, original.artist));
            deleter.deleteRelationship(oldSongID, Label.SONGNAME, artistID, Label.ARTIST);
        }

        // set album ----------------------------------------------------------
        if (!importer.nodeExists(Label.ALBUM, Property.ALBUM_NAME, req.album)) {
            if(req.album != null){if(!req.album.equals("")) {
                importer.createNode(Label.ALBUM,
                        Property.ALBUM_NAME, req.album,
                        Property.YEAR, req.year);
            }}
        }
        if(!req.sameAlbum()){
            String albumID = findIDByProperty(Label.ALBUM,
                    new PropertySet(Property.ALBUM_NAME, original.album));
            deleter.deleteRelationship(oldSongID, Label.SONGNAME, albumID, Label.ALBUM);
        }
        // set genre ----------------------------------------------------------
        if (!importer.nodeExists(Label.GENRE, Property.GENRE_NAME, req.genre)) {
            if(req.genre != null){if(!req.genre.equals("")) {
                importer.createNode(Label.GENRE, Property.GENRE_NAME, req.genre);
            }}
        }
        if(!req.sameGenre()){
            String genreID = findIDByProperty(Label.GENRE,
                    new PropertySet(Property.GENRE_NAME, original.genre));
            deleter.deleteRelationship(oldSongID, Label.SONGNAME, genreID, Label.GENRE);
        }

        createRelationships(importer, req);
    }

    private String findIDByProperty(String label, PropertySet set){
        String ID;
        String query = "MATCH (n:"+label+" {"+set.prop+": \""
                + set.val+"\"}) RETURN id(n)";

        StatementResult result = _session.run(query);
        Record record = result.next();
        ID = Integer.toString(record.get(0).asInt());

        return ID;
    }

    private void setPropertyByID(String ID, List<PropertySet> sets){
        int sz = sets.size();
        StringBuilder query = new StringBuilder();

        query.append("MATCH (n) WHERE ID(n)=")
             .append(ID).append(" SET");
        for(int i=0;i<sz;i++){
            PropertySet set = sets.get(i);
            if(i > 0){
                query.append(",");
            }
            query.append(" n.").append(set.prop)
            .append(" = '").append(set.val).append("'");
        }
        _session.run(query.toString());
    }

    private void sanitize(EditRequest req){
        req.album = sanitizeString(req.album);
        req.artist = sanitizeString(req.artist);
        req.composer = sanitizeString(req.comment);
        req.comment = sanitizeString(req.composer);
        req.discNo = sanitizeString(req.discNo);
        req.title = sanitizeString(req.title);
        req.track = sanitizeString(req.track);
        req.year = sanitizeString(req.year);
        req.genre = sanitizeString(req.genre);
        req.getOriginal().filename = sanitizeString(
                req.getOriginal().filename);
    }

    private String sanitizeString(String dirty) {
        if(dirty == null) return null;
        return dirty.replace('\"', '\'').replace("\\", "//");
    }
}
