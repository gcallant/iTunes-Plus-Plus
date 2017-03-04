package neo4j;

import com.sun.javafx.collections.ObservableListWrapper;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

   public List search(String value)
   {
      List<List<Record>> lists = new ArrayList<>();
      lists.add(new ArrayList<>());
      lists.add(new ArrayList<>());
      lists.add(new ArrayList<>());

      insertResults(lists, findSong(value), 0);
      insertResults(lists, findArtist(value), 1);
      insertResults(lists, findAlbum(value), 2);




         for(int i = 0; i < lists.size(); i++)
         {
            for(List l : lists)
            {
               if(! l.isEmpty())
               {
                  lists.set(i, new ObservableListWrapper<Record>(l));
               }
               i++;
            }
         }

      return lists;
   }

   private void insertResults(List<List<Record>> lists, StatementResult result, int index)
   {
      while(result.hasNext())
      {
         lists.get(index).add(result.next());
      }
   }

   public StatementResult findArtist(String artist)
   {
      Map<String, Object> params = new HashMap<>(1);
      params.put("like", String.format("(?i).*%s.*", artist));
      String query = "MATCH (a : artist) WHERE a.artistName =~ $like RETURN a;";
      return session.run(query, params);
   }

   public StatementResult findSong(String song)
   {
      Map<String, Object> params = new HashMap<>(1);
      params.put("like", String.format("(?i).*%s.*", song));
      String query = "MATCH (s : song) WHERE s.songName =~ $like RETURN s;";
      return session.run(query, params);
   }

   public StatementResult findAlbum(String album)
   {
      Map<String, Object> params = new HashMap<>(1);
      params.put("like", String.format("(?i).*%s.*", album));
      String query = "MATCH (a : album) WHERE a.albumName =~ $like RETURN a;";
      return session.run(query, params);
   }

   private String sanatizeUserInput(String dirtyString)
   {
      String copy = dirtyString;
      return null;
   }
}
