package neo4j;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * Makes and holds a connection to the neo4j db. Executes queries and
 * returns results.
 *
 * @author Josh Cotes
 */
public class NeoCon {

    Driver _dbDriver;
    Session _session;

    public NeoCon(String url, int port, String username, String password){
        _dbDriver = GraphDatabase.driver("bolt://"+url+":"+port, AuthTokens.basic(username, password));
        _session = _dbDriver.session();
    }

    public void create(){
        _session.run( "CREATE (a:Person {name: {name}, title: {title}})",
                parameters( "name", "Arthur", "title", "King" ) );
    }

}
