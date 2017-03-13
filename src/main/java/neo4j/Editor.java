package neo4j;

import Utilities.ID3Object;
import Utilities.Sanitizer;
import Values.Label;
import Values.Property;
import Values.PropertySet;
import Values.Relation;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ryan on 2/19/2017.
 *
 * This is a simplified Editor class. It can edit by album, artist, song, and
 * genre. A more complicated and usable editor would allow a client to change
 * multiple diverse songs at once. This task can get quite daunting is outside
 * of the scope of this project.
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
    public void editSong(EditRequest req, ID3Object id3) {
        sanitize(req);
        editDB(req);
        updateID3(req,id3);
    }

    public void editNode(String label, int id, PropertySet set) {
        int existingID = _finder.findIDByProperty(label,set);
        String query;
        String rel = Relation.getRelationship(label);
        LinkedList<Integer> songIDList = new LinkedList<>();
        set.val = Sanitizer.sanitize(set.val);
        //If node with new value already exists, move all relationships and delete old node
        if(existingID >= 0){
            Deleter deleter = new Deleter(_session);
            copyRelationships(id,existingID,rel,Relation.HAS_ALBUM);
            copyRelationships(id,existingID,rel,Relation.HAS_ARTIST);
            copyRelationships(id,existingID,rel,Relation.HAS_GENRE);
            copyRelationships(id,existingID,rel,Relation.HAS_SONG);
            deleter.deleteNodeSimple(id);

            query = "MATCH (n:"+label+")-[:"+Relation.HAS_SONG+"]->(m:"+Label.SONGNAME
                    +") WHERE id(n)="+existingID+" RETURN id(m)";
        }else{
            query = "MATCH (n:"+label+")-[:"+Relation.HAS_SONG+"]->(m:"+Label.SONGNAME
                    +") WHERE id(n)="+id+" SET n."+set.prop+"=\""+set.val+"\" RETURN id(m)";
        }

        StatementResult result = _session.run(query);

        while(result.hasNext())
            songIDList.add(result.next().get(0).asInt());

        updateID3(label,set.val,songIDList);
    }

    public void edit(List<EditRequest> requests, List<ID3Object> id3List) {
        int sz = requests.size();
        if(sz != id3List.size()){
            throw new IllegalArgumentException("Different list sizes");
        }
        for (int i=0;i<sz;i++) {
            EditRequest req = requests.get(i);
            ID3Object id3 = id3List.get(i);
            editSong(req,id3);
        }
    }

//-----------------------------------------------------------------------------
// PRIVATE METHODS
//-----------------------------------------------------------------------------

    /**
     * Updates song's ID3 tag from request values
     * @param req The values requested to change
     * @param id3 The song as ID3Object
     */
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

    /**
     * Updates ID3s' album, artist, and genre. Meant to be used when a
     * albumName, artistName, or genreName is changed.
     * @param label Label of node whose value has changed
     * @param newName New value of node
     * @param songIDList List of songs associated with node
     */
    private void updateID3(String label, String newName, List<Integer> songIDList){
        ID3Object id3;

        for(Integer id : songIDList){
            String filepath = _finder.findPropertyByID(
                    Label.SONGNAME,id,Property.FILENAME).val;
            try{
                id3 = new ID3Object(new File(filepath));
            }catch(IOException e){
                System.err.println("Unable to edit file: '"
                +filepath+"'");
                continue;
            }

            switch (label){
                case Label.ALBUM :
                    id3.setAlbum(newName);
                case Label.ARTIST :
                    id3.setArtist(newName);
                case Label.GENRE :
                    id3.setGenre(newName);
            }
        }
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
        EditRequest original = req.getOriginal();
        Deleter deleter = new Deleter(_session);
        Importer importer = new Importer(_session);
        int songID = _finder.findIDByProperty(Label.SONGNAME,
                new PropertySet(Property.FILENAME, req.filename));

        int artistID, albumID, genreID, oldID;

        // set song -----------------------------------------------------------
        LinkedList<PropertySet> songProperties =
                PropertySet.populateSongProperties(req);
        setPropertyByID(songID, songProperties);

        // set artist ---------------------------------------------------------
        artistID = importer.createIfNotExists(_finder,
                Label.ARTIST,Property.ARTIST_NAME,req.artist);
        if(!req.sameArtist()){
            oldID = importer.createIfNotExists(_finder,
                    Label.ARTIST,Property.ARTIST_NAME,original.artist);
            deleter.deleteRelationship(songID, Label.SONGNAME, oldID, Label.ARTIST);
        }

        // set album ----------------------------------------------------------
        albumID = importer.createIfNotExists(_finder,
                Label.ALBUM,Property.ALBUM_NAME,req.album);
        if(!req.sameAlbum()){
            oldID = importer.createIfNotExists(_finder,
                    Label.ALBUM,Property.ALBUM_NAME,original.album);
            deleter.deleteRelationship(songID, Label.SONGNAME, oldID, Label.ALBUM);
        }
        // set genre ----------------------------------------------------------
        genreID = importer.createIfNotExists(_finder,
                Label.GENRE,Property.GENRE_NAME,req.genre);
        if(!req.sameGenre()){
            oldID = importer.createIfNotExists(_finder,
                    Label.GENRE,Property.GENRE_NAME,original.genre);
            deleter.deleteRelationship(songID, Label.SONGNAME, oldID, Label.GENRE);
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

    /**
     * Copies all relationships from one node to another
     * @param id1 ID of node to copy from
     * @param id2 ID of node to copy to
     * @param relationKey Relationship to this node
     * @param relationToCopy The relationship to copy
     */
    private void copyRelationships(int id1, int id2, String relationKey, String relationToCopy){
        String query = "MATCH (n1)-[:"+relationToCopy+"]->(m)"
                +" MATCH (n2) WHERE id(n1)="+id1+" AND id(n2)="+id2
                +" CREATE (n2)-[:"+relationToCopy+"]->(m)"
                +" CREATE (m)-[:"+relationKey+"]->(n2)";

        _session.run(query);
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
