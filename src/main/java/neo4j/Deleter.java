package neo4j;

import org.neo4j.driver.v1.*;
import static org.neo4j.driver.v1.Values.parameters;

/**
 * Created by Ryan on 2/7/2017.
 */
public class Deleter {

    public static void main(String...args) {
        Driver driver = GraphDatabase.driver("67.110.208.167:7474", AuthTokens.basic("ryan", "o#4uPUm-#BBx7G53Rt3$mj8FYa4!%_"));
        Session session = driver.session();

        session.run("CREATE (a:Person {name: {name}, title: {title}})",
                parameters("name", "Arthur", "title", "King"));

        StatementResult result = session.run("MATCH (a:Person) WHERE a.name = {name} " +
                        "RETURN a.name AS name, a.title AS title",
                parameters("name", "Arthur"));
        while (result.hasNext()) {
            Record record = result.next();
            System.out.println(record.get("title").asString() + " " + record.get("name").asString());
        }

        session.close();
        driver.close();
    }
}
