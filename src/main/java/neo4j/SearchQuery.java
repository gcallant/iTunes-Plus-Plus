package neo4j;

import Values.Music;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

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

   public List<Music> search(String value)
   {
      ObservableList<Music> list = FXCollections.observableArrayList();

      insertResults(list, findSong(value));
      insertResults(list, findArtist(value));
      insertResults(list, findAlbum(value));

      return list;
   }

   private void insertResults(List<Music> list, StatementResult result)
   {
      while(result.hasNext())
      {
         Record record = result.next();
         Music musicItem = Music.musicFactory(record);
         if(!list.contains(musicItem))//Prevents duplicate
         {
            list.add(musicItem);
         }
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
}
