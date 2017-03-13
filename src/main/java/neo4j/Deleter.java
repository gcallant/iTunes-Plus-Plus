package neo4j;

import com.sun.org.apache.bcel.internal.generic.RETURN;
import com.sun.xml.internal.bind.v2.model.core.ID;
import org.neo4j.driver.v1.*;
import Values.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ryan on 2/7/2017.
 *
 * Notes: When deleting songs, this class checks relationships and deletes
 * empty artists, albums, and genres.
 */
public class Deleter {
//-----------------------------------------------------------------------------
// PARAMS
//-----------------------------------------------------------------------------
    private Session _session;
//-----------------------------------------------------------------------------
// CONSTRUCTORS
//-----------------------------------------------------------------------------
    public Deleter(Session session){
        _session = session;
    }
//-----------------------------------------------------------------------------
// PUBLIC METHODS
//-----------------------------------------------------------------------------

    /**
     * Removes everything from database
     */
    public void cleanDatabase(){
        String clearAll = "MATCH (n) DETACH DELETE n";
        _session.run(clearAll);
    }

    /**
     * Removes a list of nodes based on IDs
     * @param IDs   List of node IDs
     */
    public void deleteSong(List<Integer> IDs){
        for(int id : IDs){
            deleteSong(id);
        }
    }

    /**
     * Deletes a song
     * @param songID    Node ID
     */
    public void deleteSong(int songID){
        Finder finder = new Finder(_session);
        int artistID = finder.findIDByRelationship(
                Label.SONGNAME,songID,Relation.HAS_ARTIST
        );
        int albumID = finder.findIDByRelationship(
                Label.SONGNAME,songID,Relation.HAS_ALBUM
        );
        int genreID = finder.findIDByRelationship(
                Label.SONGNAME,songID,Relation.HAS_GENRE
        );

        deleteByID(songID);
        if(artistID >= 0) deleteOnEmpty(artistID);
        if(albumID >= 0) deleteOnEmpty(albumID);
        if(genreID >= 0) deleteOnEmpty(genreID);
    }

    /**
     * Deletes node, all songs associated with it, and any
     * other labels whom no long have any associated songs.
     * @param ID Node ID
     */
    public void deleteNode(String label, int ID){
        LinkedList<Integer> IDList = new LinkedList<>();
        String query = "MATCH (n:"+label+")-[:"+Relation.HAS_SONG+"]->(m) "
                + "WHERE id(n)="+ID+" RETURN id(m)";
        StatementResult result = _session.run(query);

        while (result.hasNext()){
            Record record = result.next();
            IDList.add(record.get(0).asInt());
        }

        deleteSong(IDList);
    }

    /**
     * Deletes node and relationships
     * @param ID Node ID
     */
    public void deleteNodeSimple(int ID){
        String query = "MATCH (n) WHERE id(n)="+ID+" DETACH DELETE n";
        _session.run(query);
    }

    /**
     * Deletes node based on property.
     * @param label Node label
     * @param set   The property and its value
     */
    public void deleteByProperty(String label, PropertySet set){
        Finder finder = new Finder(_session);
        int ID = finder.findIDByProperty(label, set);
        deleteByID(ID);
    }

//-----------------------------------------------------------------------------
// PACKAGE PRIVATE METHODS
//-----------------------------------------------------------------------------
    /**
     * Deletes node IF there are no relations to it
     * @param ID Node ID
     */
    void deleteOnEmpty(int ID){
        if(!hasSongs(ID)){
            deleteByID(ID);
        }
    }

    void deleteSongOnEmpty(int ID){
        if(!hasRelationships(ID)){
            deleteByID(ID);
        }
    }

    void deleteRelationship(int ID1, String label1, int ID2, String label2){
        String query = "MATCH (n:"+label1+")-[r]-(m:"+label2+") WHERE id(n)="+ID1
            + " AND id(m)="+ID2+" DELETE r";

        _session.run(query);
    }

//-----------------------------------------------------------------------------
// PRIVATE METHODS
//-----------------------------------------------------------------------------
    private boolean hasRelationships(int ID){
        int relCnt;
        String query = "MATCH (n)-[r]-(a) WHERE id(n)="+ID+" RETURN COUNT(r)";

        StatementResult result = _session.run(query);
        Record record = result.next();
        relCnt = record.get(0).asInt();

        return (relCnt > 0);
    }

    /**
     * Deletes a node using its ID. Deletes relationships as well.
     * @param ID
     */
    private void deleteByID(int ID){
        String query = "MATCH (n) WHERE id(n)="+ID+" DETACH DELETE n";
        _session.run(query);
    }

    private boolean hasSongs(int ID){
        int songCnt;
        String query = "MATCH (n)-[:"+Relation.HAS_SONG+"]->(a) "
            + "WHERE id(n)="+ID+" RETURN COUNT(a)";

        StatementResult result = _session.run(query);
        Record record = result.next();
        songCnt = record.get(0).asInt();

        return (songCnt > 0);
    }
}
