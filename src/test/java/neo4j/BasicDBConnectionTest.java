package neo4j;

import org.junit.Test;
import org.neo4j.driver.v1.*;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * Created by grant on 2/9/2017.
 */
public class BasicDBConnectionTest
{
   @Test
   public void test()
   {
      Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("iTunes", "iTunes"));
      Session session = driver.session();

      session.run("CREATE (a:Person {name: {name}, title: {title}})",
                  parameters("name", "Arthur", "title", "King"));

      StatementResult result = session.run("MATCH (a:Person) WHERE a.name = {name} " +
                                                   "RETURN a.name AS name, a.title AS title",
                                           parameters("name", "Arthur"));
      while(result.hasNext())
      {
         Record record = result.next();
         System.out.println(record.get("title").asString() + " " + record.get("name").asString());
      }

      //Removes created values after testing
      StatementResult result2 = session.run("MATCH (a:Person) WHERE a.name = {name} " +
                                                    "DETACH DELETE a",
                                            parameters("name", "Arthur"));

      session.close();
      driver.close();
   }
}
