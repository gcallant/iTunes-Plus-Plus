package neo4j;

import Utilities.ID3Object;
import Utilities.Sanitizer;
import Values.Label;
import Values.Property;
import Values.PropertySet;
import Values.Relation;

import org.neo4j.driver.v1.Session;

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
    private Finder _finder;

//-----------------------------------------------------------------------------
// CONSTRUCTORS
//-----------------------------------------------------------------------------
    public Editor(Session session) {
        _session = session;
        _finder = new Finder(_session);
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
    private void createRelationships(Importer importer, EditRequest req,
        boolean artistExists, boolean albumExists, boolean genreExists){
        // create relationship ARTIST/GENRE
        if(artistExists && genreExists) {
            if (!importer.relationshipExists(
                    Label.GENRE, Property.GENRE_NAME, req.genre,
                    Relation.HAS_ARTIST,
                    Label.ARTIST, Property.ARTIST_NAME, req.artist)
                    )
                importer.createRelationshipReciprocal(
                        Label.GENRE, Property.GENRE_NAME, req.genre,
                        Relation.HAS_ARTIST,
                        Label.ARTIST, Property.ARTIST_NAME, req.artist,
                        Relation.HAS_GENRE
                );
        }
        // create relationship SONG/GENRE
        if(genreExists) {
            if (!importer.relationshipExists(
                    Label.GENRE, Property.GENRE_NAME, req.genre,
                    Relation.HAS_SONG,
                    Label.SONGNAME, Property.SONG_NAME, req.title)
                    )
                importer.createRelationshipReciprocal(
                        Label.GENRE, Property.GENRE_NAME, req.genre,
                        Relation.HAS_SONG,
                        Label.SONGNAME, Property.SONG_NAME, req.title,
                        Relation.HAS_GENRE
                );
        }
        // create relationship ALBUM/GENRE
        if(albumExists && genreExists) {
            if (!importer.relationshipExists(
                    Label.GENRE, Property.GENRE_NAME, req.genre,
                    Relation.HAS_ALBUM,
                    Label.ALBUM, Property.ALBUM_NAME, req.album)
                    )
                importer.createRelationshipReciprocal(
                        Label.GENRE, Property.GENRE_NAME, req.genre,
                        Relation.HAS_ALBUM,
                        Label.ALBUM, Property.ALBUM_NAME, req.album,
                        Relation.HAS_GENRE
                );
        }
        // create relationship ARTIST/SONG
        if(artistExists) {
            if (!importer.relationshipExists(
                    Label.ARTIST, Property.ARTIST_NAME, req.artist,
                    Relation.HAS_SONG,
                    Label.SONGNAME, Property.SONG_NAME, req.title)
                    )
                importer.createRelationshipReciprocal(
                        Label.ARTIST, Property.ARTIST_NAME, req.artist,
                        Relation.HAS_SONG,
                        Label.SONGNAME, Property.SONG_NAME, req.title,
                        Relation.HAS_ARTIST);
        }
        // create relationship ALBUM/ARTIST
        if(albumExists && artistExists) {
            if (!importer.relationshipExists(
                    Label.ALBUM, Property.ALBUM_NAME, req.album, Relation.HAS_ARTIST,
                    Label.ARTIST, Property.ARTIST_NAME, req.artist)
                    )
                importer.createRelationshipReciprocal(
                        Label.ALBUM, Property.ALBUM_NAME, req.album, Relation.HAS_ARTIST,
                        Label.ARTIST, Property.ARTIST_NAME, req.artist, Relation.HAS_ALBUM);
        }
        // create relationship ALBUM/SONG
        if(albumExists) {
            if (!importer.relationshipExists(
                    Label.ALBUM, Property.ALBUM_NAME, req.album, Relation.HAS_SONG,
                    Label.SONGNAME, Property.SONG_NAME, req.title)
                    )
                importer.createRelationshipReciprocal(
                        Label.ALBUM, Property.ALBUM_NAME, req.album, Relation.HAS_SONG,
                        Label.SONGNAME, Property.SONG_NAME, req.title, Relation.HAS_ALBUM
                );
        }
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
        String songID = _finder.findIDByProperty(Label.SONGNAME,
                new PropertySet(Property.FILENAME, req.filename));
        String albumID = _finder.findIDByProperty(Label.ALBUM,
                new PropertySet(Property.ALBUM_NAME, req.album));
        String artistID = _finder.findIDByProperty(Label.ARTIST,
                new PropertySet(Property.ARTIST_NAME, req.artist));
        String genreID = _finder.findIDByProperty(Label.GENRE,
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
        String songID = _finder.findIDByProperty(Label.SONGNAME,
                new PropertySet(Property.FILENAME, req.filename));

        boolean artistExists, albumExists, genreExists;

        // set song -----------------------------------------------------------
        LinkedList<PropertySet> songProperties =
                PropertySet.populateSongProperties(req);
        setPropertyByID(songID, songProperties);

        // set artist ---------------------------------------------------------
        artistExists = importer.createIfNotExists(
                Label.ARTIST,Property.ARTIST_NAME,req.artist);
        if(!req.sameArtist()){
            String artistID = _finder.findIDByProperty(Label.ARTIST,
                    new PropertySet(Property.ARTIST_NAME, original.artist));
            deleter.deleteRelationship(songID, Label.SONGNAME, artistID, Label.ARTIST);
        }

        // set album ----------------------------------------------------------
        albumExists = importer.createIfNotExists(
                Label.ALBUM,Property.ALBUM_NAME,req.album);
        if(!req.sameAlbum()){
            String albumID = _finder.findIDByProperty(Label.ALBUM,
                    new PropertySet(Property.ALBUM_NAME, original.album));
            deleter.deleteRelationship(songID, Label.SONGNAME, albumID, Label.ALBUM);
        }
        // set genre ----------------------------------------------------------
        genreExists = importer.createIfNotExists(
                Label.GENRE,Property.GENRE_NAME,req.genre);
        if(!req.sameGenre()){
            String genreID = _finder.findIDByProperty(Label.GENRE,
                    new PropertySet(Property.GENRE_NAME, original.genre));
            deleter.deleteRelationship(songID, Label.SONGNAME, genreID, Label.GENRE);
        }

        createRelationships(importer, req, artistExists, albumExists, genreExists);
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
            .append(" = \"").append(set.val).append("\"");
        }
        _session.run(query.toString());
    }

    private void sanitize(EditRequest req){
        req.album = Sanitizer.sanitize(req.album);
        req.artist = Sanitizer.sanitize(req.artist);
        req.composer = Sanitizer.sanitize(req.comment);
        req.comment = Sanitizer.sanitize(req.composer);
        req.discNo = Sanitizer.sanitize(req.discNo);
        req.title = Sanitizer.sanitize(req.title);
        req.track = Sanitizer.sanitize(req.track);
        req.year = Sanitizer.sanitize(req.year);
        req.genre = Sanitizer.sanitize(req.genre);
        req.filename = Sanitizer.sanitize(req.filename);
        req.updateOriginalFilename();
    }
}
