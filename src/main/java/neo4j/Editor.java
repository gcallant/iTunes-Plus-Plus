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
        int songID = _finder.findIDByProperty(Label.SONGNAME,
                new PropertySet(Property.FILENAME, req.filename));
        int albumID = _finder.findIDByProperty(Label.ALBUM,
                new PropertySet(Property.ALBUM_NAME, req.album));
        int artistID = _finder.findIDByProperty(Label.ARTIST,
                new PropertySet(Property.ARTIST_NAME, req.artist));
        int genreID = _finder.findIDByProperty(Label.GENRE,
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
        Deleter deleter = new Deleter(_session);
        Importer importer = new Importer(_session);
        int songID = _finder.findIDByProperty(Label.SONGNAME,
                new PropertySet(Property.FILENAME, req.filename));

        int artistID, albumID, genreID;

        // set song -----------------------------------------------------------
        LinkedList<PropertySet> songProperties =
                PropertySet.populateSongProperties(req);
        setPropertyByID(songID, songProperties);

        // set artist ---------------------------------------------------------
        artistID = importer.createIfNotExists(_finder,
                Label.ARTIST,Property.ARTIST_NAME,req.artist);
        if(!req.sameArtist()){
            deleter.deleteRelationship(songID, Label.SONGNAME, artistID, Label.ARTIST);
        }

        // set album ----------------------------------------------------------
        albumID = importer.createIfNotExists(_finder,
                Label.ALBUM,Property.ALBUM_NAME,req.album);
        if(!req.sameAlbum()){
            deleter.deleteRelationship(songID, Label.SONGNAME, albumID, Label.ALBUM);
        }
        // set genre ----------------------------------------------------------
        genreID = importer.createIfNotExists(_finder,
                Label.GENRE,Property.GENRE_NAME,req.genre);
        if(!req.sameGenre()){
            deleter.deleteRelationship(songID, Label.SONGNAME, genreID, Label.GENRE);
        }

        importer.createAllRelationships(albumID, artistID, genreID, songID);
    }

    private void setPropertyByID(int ID, List<PropertySet> sets){
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
