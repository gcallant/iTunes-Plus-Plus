package neo4j;

import org.neo4j.driver.v1.Session;
import org.neo4j.graphdb.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Grant on 2/22/2017.
 */
public class SearchQuery
{
   private Session session = null;

   public SearchQuery(Session session)
   {
      this.session = session;
   }

   public List<Node> search(String value)
   {
      List<Node> list = new ArrayList<>();
      Node result = findArtist(value);
      if(result != null)
      {
         list.add(result);
      }
      return list;
   }

   public Node findArtist(String artist)
   {
      StringBuilder query = new StringBuilder();

      query.append("match (a:artist) where a.artistName=\"Aaron Goldberg\" return a;");

      return (Node) session.run(query.toString());
   }

   private String sanatizeUserInput(String dirtyString)
   {
      String copy = dirtyString;
      return null;
   }
}
