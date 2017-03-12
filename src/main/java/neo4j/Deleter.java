package neo4j;

import org.neo4j.driver.v1.*;
import Values.*;

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
//    private QueryHandler _qHandler;

//-----------------------------------------------------------------------------
// CONSTRUCTORS
//-----------------------------------------------------------------------------
    public Deleter(Session session){
        _session = session;
//        _qHandler = new QueryHandler(_session);
    }

//-----------------------------------------------------------------------------
// PUBLIC METHODS
//-----------------------------------------------------------------------------
    public void cleanDatabase(){
        String clearAll = "MATCH (n) DETACH DELETE n";
        _session.run(clearAll);
    }

    public void deleteByID(List<Integer> IDs){
        for(int id : IDs){
            deleteByID(id);
        }
    }

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

    public void deleteByID(int ID){
        String query = "MATCH (n) WHERE id(n)="+ID+" DETACH DELETE n";
        _session.run(query);
    }

    public void deleteByProperty(String label, PropertySet set){
        Finder finder = new Finder(_session);
        int ID = finder.findIDByProperty(label, set);
        deleteByID(ID);
    }

    //deletes node IF there are no relations to it
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
