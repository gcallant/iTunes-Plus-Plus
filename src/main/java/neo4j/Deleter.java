package neo4j;

import org.neo4j.driver.v1.*;
import Values.*;

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

    public void deleteSong(String key){
//        String album = _qHandler.findAlbum(key);
//        String artist = _qHandler.findArtist(key);
//        String genre = _qHandler.findGenre(key);

        delete(key, Label.SONGNAME);

//        deleteOnEmpty(album, Label.ALBUM);
//        deleteOnEmpty(artist, Label.ARTIST);
//        deleteOnEmpty(genre, Label.GENRE);
    }

    public void deleteSongs(String...keys){
        for(String s : keys){
            deleteSong(s);
        }
    }

    //deletes node IF there are no relations to it
    public void deleteOnEmpty(String key, String label){
        if(!hasRelations(key, label)){
            delete(key, label);
        }
    }

    public void deleteRelationship(String key1, String label1, String key2, String label2){
        StringBuilder query = new StringBuilder();
        query.append("MATCH (").append(key1).append(":").append(label1).append(")");
        query.append("-[r:*]-(").append(key2).append(":").append(label2).append(")");
        query.append("DELETE r");
    }

//-----------------------------------------------------------------------------
// PRIVATE METHODS
//-----------------------------------------------------------------------------
    private void delete(String key, String label){
        StringBuilder query = new StringBuilder();

        query.append("MATCH (" + key + ":" + label + ")");
        query.append("DETACH DELETE " + key);

        _session.run(query.toString());
    }

    private boolean hasRelations(String key, String label){
        int relCnt = 0;
        StringBuilder query = new StringBuilder();

        query.append("MATCH (" + key + ":" + label + ")-[*]-() ");
        query.append("RETURN COUNT(*)");

        StatementResult result = _session.run(query.toString());

        while(result.hasNext()){
            Record record = result.next();
            relCnt += record.get(0).asInt();
        }

        return (relCnt > 0);
    }


}
