package Values;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;

import java.util.List;
import java.util.Map;

/**
 * Created by grant on 3/4/2017.
 */
public class Music
{
   private String song, artist, album, filePath;

   private Music(String song, String artist, String album, String filePath)
   {
      this.song = song;
      this.artist = artist;
      this.album = album;
      this.filePath = filePath;
   }

   public String getSong()
   {
      return song;
   }

   public static Music musicFactory(Record record)
   {
      List<Value> values = record.values();
      for(Value v : values)
      {
        Node n = v.asNode();
        Map m =  n.asMap();
         System.out.println(m);
      }
      return null;
   }

   public String getFilePath()
   {
      return filePath;
   }

   public void setFilePath(String filePath)
   {
      this.filePath = filePath;
   }

   public void setSong(String song)
   {
      this.song = song;
   }

   public String getArtist()
   {
      return artist;
   }

   public void setArtist(String artist)
   {
      this.artist = artist;
   }

   public String getAlbum()
   {
      return album;
   }

   public void setAlbum(String album)
   {
      this.album = album;
   }
}
