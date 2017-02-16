package neo4j;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * Created by grant on 2/9/2017.
 */
public class DatabaseManagerTest
{
   private DatabaseManager databaseManager;
   private Session         session;
   @Before
   public void setUp() throws Exception
   {
      databaseManager = DatabaseManager.getInstance();
      session = databaseManager.getDatabaseConnector();
   }


   @Test
   public void insertionTest() throws Exception
   {
      Assert.assertNotNull(session.run("CREATE (a:Person { name: {name}, title: {title} })",
                                       parameters("name", "Arthur", "title", "King")));
   }

   @Test
   public void matchTest() throws Exception
   {
      StatementResult result = session.run("MATCH (a:Person) WHERE a.name = {name} " +
                                                   "RETURN a.name AS name, a.title AS title",
                                           parameters("name", "Arthur"));
      while(result.hasNext())
      {
         Record record = result.next();
         Assert.assertEquals("King Arthur", record.get("title").asString() + " " + record.get("name").asString());
      }
   }

   @Test
   public void deleteInsertion() throws Exception
   {
      StatementResult result = session.run("MATCH (a:Person) WHERE a.name = {name} " +
                                                   "DETACH DELETE a",
                                           parameters("name", "Arthur"));
      while(result.hasNext())
      {
         Record record = result.next();
         Assert.assertEquals("", record.get("title").asString() + " " + record.get("name").asString());
      }
   }

   @After
   public void tearDown() throws Exception
   {
      databaseManager = null;
   }
}