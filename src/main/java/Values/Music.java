package Values;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;

import java.util.List;

/**
 * Created by grant on 3/4/2017.
 */
public class Music
{
   private String song, artist, album, genre, filePath;

   private Music(String song, String artist, String album, String genre, String filePath)
   {
      this.song = song;
      this.artist = artist;
      this.album = album;
      this.genre = genre;
      this.filePath = filePath;
   }

   public String getSong()
   {
      return song;
   }

   public static Music musicFactory(Record record)
   {
      List<Value> values = record.values();
      StringBuilder song = new StringBuilder();
      StringBuilder artist = new StringBuilder();
      StringBuilder album = new StringBuilder();
      StringBuilder genre = new StringBuilder();
      StringBuilder filePath = new StringBuilder();
      for(Value v : values)
      {
         getValue(v, "songName", song);
         getValue(v, "artistName", artist);
         getValue(v, "albumName", album);
         getValue(v, "genreName", genre);
         getValue(v, "fileLocation", filePath);
      }
      return new Music(song.toString(), artist.toString(), album.toString(), genre.toString(), filePath.toString());
   }

   private static void getValue(Value value, String key, StringBuilder data)
   {
      String s;
      s = value.get(key, "");
      if(!s.isEmpty())
      {
         data.append(s);
      }
   }

   public String getFilePath()
   {
      return filePath;
   }

   public void setFilePath(String filePath)
   {
      this.filePath = filePath;
   }

   public String getGenre()
   {
      return genre;
   }

   public void setGenre(String genre)
   {
      this.genre = genre;
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
