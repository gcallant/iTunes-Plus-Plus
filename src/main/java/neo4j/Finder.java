package neo4j;

import Values.PropertySet;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;

/**
 * Created by Ryan on 3/5/2017.
 * Finder is used to (for example) locate node IDs by properties.
 * Any useful package private implementations for finding useful
 * information.
 */
public class Finder {
    private Session _session;

    public Finder(Session session){ _session = session; }

    public int findIDByProperty(String label, PropertySet set){
        int ID = -1;
        String query = "MATCH (n:"+label+" {"+set.prop+": \""
                + set.val+"\"}) RETURN id(n)";

        StatementResult result = _session.run(query);
        try {
            ID = result.next().get(0).asInt();
        } catch(NoSuchRecordException e){}

        return ID;
    }

    public PropertySet findPropertyByID(String label, int ID, String prop){
        String query = "MATCH (n:"+label+") WHERE id(n)="+ID+" RETURN n."+prop;

        StatementResult result = _session.run(query);
        Record record = result.next();
        return new PropertySet(prop,record.get(0).asString());
    }

    /**
     * Finds one corresponding relationship to a node.
     * @param label
     * @param ID
     * @param rel
     * @return  The first id in the relationship
     */
    public int findIDByRelationship(String label, int ID, String rel){
        int ID2 = -1;
        String query = "MATCH (n:"+label+")-[:"+rel+"]->(m) WHERE id(n)="+ID+" RETURN id(m)";

        StatementResult result = _session.run(query);
        try {
            Record record = result.next();
            ID2 = record.get(0).asInt();
        }catch(NoSuchRecordException e){}

        return ID2;
    }
}
