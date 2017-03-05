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

    public void delete(List<String> IDs){
        for(String s : IDs){
            delete(s);
        }
    }

    //deletes node IF there are no relations to it
    void deleteOnEmpty(String ID){
        if(!hasSongs(ID)){
            delete(ID);
        }
    }

    void deleteSongOnEmpty(String ID){
        if(!hasRelationships(ID)){
            delete(ID);
        }
    }

    void deleteRelationship(String ID1, String label1, String ID2, String label2){
        String query = "MATCH (n:"+label1+")-[r]-(m:"+label2+") WHERE id(n)="+ID1
            + " AND id(m)="+ID2+" DELETE r";

        _session.run(query);
    }

//-----------------------------------------------------------------------------
// PRIVATE METHODS
//-----------------------------------------------------------------------------
    private void delete(String ID){
        String query = "MATCH (n) WHERE id(n)="+ID+" DETACH DELETE n";

        _session.run(query);
    }

    private boolean hasRelationships(String ID){
        int relCnt;
        String query = "MATCH (n)-[r]-(a) WHERE id(n)="+ID+" RETURN COUNT(r)";

        StatementResult result = _session.run(query);
        Record record = result.next();
        relCnt = record.get(0).asInt();

        return (relCnt > 0);
    }

    private boolean hasSongs(String ID){
        int songCnt;
        String query = "MATCH (n)-[:"+Relation.HAS_SONG+"]->(a) "
            + "WHERE id(n)="+ID+" RETURN COUNT(a)";

        StatementResult result = _session.run(query);
        Record record = result.next();
        songCnt = record.get(0).asInt();

        return (songCnt > 0);
    }


}
