package neo4j;

import Utilities.FileHandler;
import Utilities.ID3Object;
import Values.Label;
import Values.Property;
import Values.Relation;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * Handles importing songs ID3 and file information into database.
 * Adds appropriate nodes and relationships, doesn't add duplicates of
 * either.
 *
 * @author Josh Cotes
 */
public class Importer
{

   private int _songCount = 0;
   private Session              _session;
   private Vector<String>       filePaths;
   private Vector<ID3Object> id3s;


   public Importer(Session session)
   {
      _session = session;
   }

   /**
    * Retrieves all files from all folders recursively from given file root.
    * Searches each time per file extension in list.
    *
    * @param root           - file path
    * @param fileExtensions - list of file extensions to search
    */
   public void addFolderRecursively(String root, List<String> fileExtensions)
   {


      //fileExtensions.parallelStream().forEach(E -> addFolderRecursively(root, E));
        /*
        This happens...
        Caused by: org.neo4j.driver.v1.exceptions.ClientException: You are using a
        session from multiple locations at the same time, which is not supported.
        If you want to use multiple threads, you should ensure that each session is
        used by only one thread at a time. One way to do that is to give each thread
        its own dedicated session.
         */
      for(String e : fileExtensions)
      {
         addFolderRecursively(root, e);
      }
      addSongs(filePaths, id3s);
   }

   /**
    * Retrieves all files from all folders recursively from given file root.
    * Searches for file extension.
    *
    * @param root          - file path
    * @param fileExtension - file extension to search
    */
   public void addFolderRecursively(String root, String fileExtension)
   {
      filePaths = new Vector<>();
      id3s = new Vector<>();
      ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
      Runnable task = () ->  addSongs(filePaths, id3s);
      executor.scheduleAtFixedRate(task, 3, 3, TimeUnit.SECONDS);
      FileHandler.getAllFilesAndID3s(new File(root), fileExtension, filePaths, id3s);
   }

   /**
    * Add all songs from list using id3s to the database.
    *
    * @param songPaths - list of song paths
    * @param id3s      - list of id3 tags
    */
   private void addSongs(Vector<String> songPaths, Vector<ID3Object> id3s)
   {
      for(int i = 0; i < songPaths.size(); i++)
      {
         String songPath = songPaths.get(i);
         ID3Object id3Object = id3s.get(i);
         songPaths.remove(i);
         id3s.remove(i);
         addSong(songPath, id3Object);
      }
   }

   public int getSongCount()
   {
      int tmp = _songCount;
      _songCount = 0;
      return tmp;
   }

   /**
    * Sanitizes strings for database entry.
    *
    * @param dirty - the dirty string
    * @return - clean string
    */
   private String sanitizeString(String dirty)
   {
      if(dirty == null)
      { return null; }
      return dirty.replace('\"', '\'').replace("\\", "//");
   }

   /**
    * Creates a node if it does not already exist.
    *
    * @param label
    * @param prop
    * @param val
    * @return True if a node is created or already exists.
    */
   public boolean createIfNotExists(String label, String prop, String val)
   {
      if(! nodeExists(label, prop, val) && val != null)
      {
         if(! val.trim().equals(""))
         {
            createNode(label, prop, val);
            return true;
         }
      }
      else if(val != null)
      {
         return true;
      }
      return false;
   }

   public void createNode(String... triples)
   {

      StringBuilder query = new StringBuilder();

      query.append("CREATE (" + triples[0] + ":" + triples[0] + " {");
      for(int i = 2; i < triples.length; i++)
      {
         if(i % 2 == 0)
         { query.append(triples[i - 1] + ":\"" + triples[i] + "\""); }
         if(i % 2 == 1 && i < triples.length - 1)
         { query.append(","); }
      }
      query.append("})");
      _session.run(query.toString());
   }


   /**
    * Adds a song's ID3 information to the database with the appropriate relationships.
    * Prevents duplicate node and relationships from being created.
    *
    * @param songPath - path to the song file
    * @param id3      - the ID3Object
    */
   public void addSong(String songPath, ID3Object id3)
   {

      boolean artistExists, albumExists, genreExists;

      String album = sanitizeString(id3.getAlbum());
      String artist = sanitizeString(id3.getArtist());
      String composer = sanitizeString(id3.getComposer());
      String comment = sanitizeString(id3.getComment());
      String discNo = sanitizeString(id3.getDiscNo());
      String songName = sanitizeString(id3.getTitle());
      String track = sanitizeString(id3.getTrack());
      String year = sanitizeString(id3.getYear());
      String genre = sanitizeString(id3.getGenre());
      String fileName = sanitizeString(songPath);


      // import song info  /////////
      if(! nodeExists(Label.SONGNAME, Property.FILENAME, fileName))
      {
         createNode(Label.SONGNAME,
                    Property.SONG_NAME, songName,
                    Property.TRACK_NUM, track,
                    Property.COMMENT, comment,
                    Property.DISC_NO, discNo,
                    Property.YEAR, year,
                    Property.COMPOSER_NAME, composer,
                    Property.FILENAME, fileName);
         _songCount++;
      }
      // import artist info //////////
      artistExists = createIfNotExists(Label.ARTIST, Property.ARTIST_NAME, artist);

      ///////// import album info ///////////////
      albumExists = createIfNotExists(Label.ALBUM, Property.ALBUM_NAME, album);

      ////////// import genre info ////////////////
      genreExists = createIfNotExists(Label.GENRE, Property.GENRE_NAME, genre);

      // create relationship ARTIST/GENRE
      if(artistExists && genreExists)
      {
         if(! relationshipExists(
                 Label.GENRE, Property.GENRE_NAME, genre,
                 Relation.HAS_ARTIST,
                 Label.ARTIST, Property.ARTIST_NAME, artist)
                 )
         {
            createRelationshipReciprocal(
                    Label.GENRE, Property.GENRE_NAME, genre,
                    Relation.HAS_ARTIST,
                    Label.ARTIST, Property.ARTIST_NAME, artist,
                    Relation.HAS_GENRE
            );
         }
      }
      // create relationship SONG/GENRE
      if(genreExists)
      {
         if(! relationshipExists(
                 Label.GENRE, Property.GENRE_NAME, genre,
                 Relation.HAS_SONG,
                 Label.SONGNAME, Property.SONG_NAME, songName)
                 )
         {
            createRelationshipReciprocal(
                    Label.GENRE, Property.GENRE_NAME, genre,
                    Relation.HAS_SONG,
                    Label.SONGNAME, Property.SONG_NAME, songName,
                    Relation.HAS_GENRE
            );
         }
      }
      // create relationship ALBUM/GENRE
      if(albumExists && genreExists)
      {
         if(! relationshipExists(
                 Label.GENRE, Property.GENRE_NAME, genre,
                 Relation.HAS_ALBUM,
                 Label.ALBUM, Property.ALBUM_NAME, album)
                 )
         {
            createRelationshipReciprocal(
                    Label.GENRE, Property.GENRE_NAME, genre,
                    Relation.HAS_ALBUM,
                    Label.ALBUM, Property.ALBUM_NAME, album,
                    Relation.HAS_GENRE
            );
         }
      }
      // create relationship ARTIST/SONG
      if(artistExists)
      {
         if(! relationshipExists(
                 Label.ARTIST, Property.ARTIST_NAME, artist,
                 Relation.HAS_SONG,
                 Label.SONGNAME, Property.SONG_NAME, songName)
                 )
         {
            createRelationshipReciprocal(
                    Label.ARTIST, Property.ARTIST_NAME, artist,
                    Relation.HAS_SONG,
                    Label.SONGNAME, Property.SONG_NAME, songName,
                    Relation.HAS_ARTIST);
         }
      }
      // create relationship ALBUM/ARTIST
      if(albumExists && artistExists)
      {
         if(! relationshipExists(
                 Label.ALBUM, Property.ALBUM_NAME, album, Relation.HAS_ARTIST,
                 Label.ARTIST, Property.ARTIST_NAME, artist)
                 )
         {
            createRelationshipReciprocal(
                    Label.ALBUM, Property.ALBUM_NAME, album, Relation.HAS_ARTIST,
                    Label.ARTIST, Property.ARTIST_NAME, artist, Relation.HAS_ALBUM);
         }
      }
      // create relationship ALBUM/SONG
      if(albumExists)
      {
         if(! relationshipExists(
                 Label.ALBUM, Property.ALBUM_NAME, album, Relation.HAS_SONG,
                 Label.SONGNAME, Property.SONG_NAME, songName)
                 )
         {
            createRelationshipReciprocal(
                    Label.ALBUM, Property.ALBUM_NAME, album, Relation.HAS_SONG,
                    Label.SONGNAME, Property.SONG_NAME, songName, Relation.HAS_ALBUM
            );
         }
      }
   }

   /**
    * Creates a reciprocal relationship between two nodes given properties.
    *
    * @param label1       - first node label
    * @param property1    - first node subject
    * @param value1       - first node subject value
    * @param relationship - relationship to second node
    * @param label2       - second node label
    * @param property2    - second node subject
    * @param value2       - second node value
    */
   public void createRelationshipReciprocal(String label1, String property1, String value1, String relationship,
                                            String label2, String property2, String value2, String relation2)
   {

      createRelationship(label1, property1, value1, relationship, label2, property2, value2);
      createRelationship(label2, property2, value2, relation2, label1, property1, value1);
   }

   /**
    * Creates a relationship from the first node to the second.
    *
    * @param label1       - first node label
    * @param property1    - first node subject
    * @param value1       - first node subject value
    * @param relationship - relationship to second node
    * @param label2       - second node label
    * @param property2    - second node subject
    * @param value2       - second node value
    */
   public void createRelationship(String label1, String property1, String value1, String relationship,
                                  String label2, String property2, String value2)
   {

      _session.run("MATCH  (one:" + label1 + " {" + property1 + ":\"" + value1 + "\"} )" +
                           "MATCH  (two:" + label2 + " {" + property2 + ":\"" + value2 + "\"} )" +
                           "CREATE (one)-[" + relationship + ":" + relationship + "]" +
                           "->(two)");
   }

   /**
    * Returns true if there exists a node with the given parameters
    *
    * @param label   - the node label
    * @param subject - the subject
    * @param value   - the subject value
    * @return - true if node exists
    */
   public boolean nodeExists(String label, String subject, String value)
   {

      return _session.run("MATCH (a:" + label + ") WHERE a." + subject + " = {" + subject + "} " +
                                  "RETURN a." + subject + " AS " + subject,
                          parameters(subject, value)).hasNext();
   }

   /**
    * Returns true if there exists a specific relationship between two nodes.
    *
    * @param label1       - first node label
    * @param property1    - first node subject
    * @param value1       - first node subject value
    * @param relationship - relationship to second node
    * @param label2       - second node label
    * @param property2    - second node subject
    * @param value2       - second node value
    * @return - true if that relationship exists
    */
   public boolean relationshipExists(String label1, String property1, String value1, String relationship,
                                     String label2, String property2, String value2)
   {

      String query = "MATCH (n:" + label1 + ")-[r:" + relationship + "]->(m:" + label2 + ") " +
                             "WHERE n." + property1 + " = \"" + value1 + "\" " +
                             "AND m." + property2 + " = \"" + value2 + "\" " +
                             //                "RETURN n." + property1 + ", m." + property2;
                             "RETURN SIGN(COUNT(r))";

      StatementResult result = _session.run(query);
      Record record = result.next();
      return record.get(0).asInt() > 0;

      //       return _session.run(query).hasNext();
   }
}
