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
      String song = null;
      String filePath = null;
      String artist = null;
      String album = null;
      String genre = null;

      song = values.get(0).asString();
      filePath = values.get(1).asString();
      artist = values.get(2).asString();
      album = values.get(3).asString();
      genre = values.get(4).asString();

      return new Music(song, artist, album, genre, filePath);
   }

   public String getFilePath()
   {
      return filePath;
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

   @Override
   public boolean equals(Object music)
   {
      Music that = (Music) music;
      return this.filePath.equals(that.filePath);
   }
}
