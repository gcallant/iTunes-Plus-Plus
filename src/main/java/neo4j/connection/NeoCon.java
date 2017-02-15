package neo4j.connection;

import org.neo4j.driver.v1.*;

import java.util.List;
import java.util.Vector;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * Makes and holds a connection to the neo4j db. Executes queries and
 * returns results. hahaha
 *
 * @author Josh Cotes
 */
public class NeoCon {

    private Driver _dbDriver;
    private Session _session;

    public NeoCon(String url, int port, String username, String password){
        _dbDriver = GraphDatabase.driver("bolt://"+url+":"+port, AuthTokens.basic(username, password));
        _session = _dbDriver.session();
    }

    public void create(){
        _session.run( "CREATE (a:Person {name: {name}, title: {title}})",
                parameters( "name", "Arthur", "title", "King" ) );
    }

    public Session getSession(){
        return _session;
    }

    public StatementResult query(String query) {
        return _session.run(query);
    }
}
