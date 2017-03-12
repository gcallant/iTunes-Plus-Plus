package neo4j;

import Utilities.FileHandler;
import Utilities.ID3Object;
import Utilities.Sanitizer;
import Utilities.SharedQueue;
import Values.Label;
import Values.Property;
import Values.PropertySet;
import Values.Relation;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.io.File;
import java.io.IOException;

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
   private Session _session;

   public Importer(Session session)
   {
      _session = session;
   }

   /**
    * Retrieves all files from all folders recursively from given file root.
    * Searches for file extension.
    *
    * Note: May improve performance to use two queues - one to grab filepaths
    * that are turned into ID3Objects, and one to grab ID3Objects and store
    * them into database.
    *
    * @param root          - file path
    */
   public void addFolderRecursively(String root)
   {
      SharedQueue<String> importList = new SharedQueue<>(10_000);

      //Grabs all files that match supported media extensions and
      //store them into importList
      Thread fileGrabber = new Thread(new Runnable() {
         @Override public void run() {
            FileHandler.getAllMusicFiles(new File(root),importList);
            importList.stop();
         }
      });
      fileGrabber.start();

      //Grabs all file paths from importList, create ID3Objects,
      //and store into database
      Thread dbImporter = new Thread(new Runnable() {
         @Override public void run() {
            while(!importList.isEmpty() || !importList.isStopped()){
               String filepath = importList.dequeue();
               try {
                  ID3Object id3 = new ID3Object(new File(filepath));
                  addSong(id3);
               }catch(IOException e){
                  System.err.println("Unable to create ID3Object from '"
                          + filepath + "'");
               }
            }
         }
      });
      dbImporter.start();

      while(dbImporter.isAlive()) {
         try {
            dbImporter.join();
         } catch (InterruptedException e){}
      }
   }

   public int getSongCount()
   {
      int tmp = _songCount;
      _songCount = 0;
      return tmp;
   }

   /**
    * Creates a node if it does not already exist.
    *
    * @param label Node label
    * @param prop Node property
    * @param val Node value
    * @return True if a node is created or already exists.
    */
   public int createIfNotExists(Finder finder, String label, String prop, String val)
   {
      if(val == null) return -1;
      if(val.trim().length() == 0) return -1;

      int ID = finder.findIDByProperty(label,
                  new PropertySet(prop,val));

      return ID >= 0 ? ID : createNode(label, prop, val);
   }

   private int createNode(String... triples)
   {
      StringBuilder query = new StringBuilder();

      query.append("CREATE (n:").append(triples[0]).append(" {");
      for(int i = 2; i < triples.length; i++)
      {
         if(i % 2 == 0){
            query.append(triples[i - 1]).append(":\"")
            .append(triples[i]).append("\"");
         } else if(i < triples.length - 1) {
            query.append(",");
         }
      }
      query.append("}) return id(n)");
      StatementResult result = _session.run(query.toString());

      return result.next().get(0).asInt();
   }

   /**
    * Adds a song's ID3 information to the database with the appropriate relationships.
    * Prevents duplicate node and relationships from being created.
    *
    * @param id3      - the ID3Object
    */
   private void addSong(ID3Object id3)
   {
      String fileName = Sanitizer.sanitize(id3.getFile());
      if(nodeExists(Label.SONGNAME, Property.FILENAME, fileName)) {
         return;
      }

      int artistID, albumID, genreID, songID;
      Finder finder = new Finder(_session);

      String album = Sanitizer.sanitize(id3.getAlbum());
      String artist = Sanitizer.sanitize(id3.getArtist());
      String genre = Sanitizer.sanitize(id3.getGenre());

      String composer = Sanitizer.sanitize(id3.getComposer());
      String comment = Sanitizer.sanitize(id3.getComment());
      String discNo = Sanitizer.sanitize(id3.getDiscNo());
      String songName = Sanitizer.sanitize(id3.getTitle());
      String track = Sanitizer.sanitize(id3.getTrack());
      String year = Sanitizer.sanitize(id3.getYear());

      songID = createNode(Label.SONGNAME,
                 Property.SONG_NAME, songName,
                 Property.TRACK_NUM, track,
                 Property.COMMENT, comment,
                 Property.DISC_NO, discNo,
                 Property.YEAR, year,
                 Property.COMPOSER_NAME, composer,
                 Property.FILENAME, fileName);

      _songCount++;

      artistID = createIfNotExists(finder,
              Label.ARTIST, Property.ARTIST_NAME, artist);
      albumID = createIfNotExists(finder,
              Label.ALBUM, Property.ALBUM_NAME, album);
      genreID = createIfNotExists(finder,
              Label.GENRE, Property.GENRE_NAME, genre);

      createAllRelationships(albumID,artistID,genreID,songID);
   }

   void createAllRelationships(int albumID,int artistID,
                                       int genreID, int songID){
      // create relationship ARTIST/GENRE
      if(artistID >= 0 && genreID >= 0) {
            createRelationship(
                    Label.GENRE, genreID, Relation.HAS_ARTIST,
                    Label.ARTIST, artistID, Relation.HAS_GENRE
            );
      }
      // create relationship SONG/GENRE
      if(genreID >= 0) {
            createRelationship(
                    Label.GENRE, genreID, Relation.HAS_SONG,
                    Label.SONGNAME, songID, Relation.HAS_GENRE
            );
      }
      // create relationship ALBUM/GENRE
      if(albumID >= 0 && genreID >= 0) {
            createRelationship(
                    Label.GENRE, genreID, Relation.HAS_ALBUM,
                    Label.ALBUM, albumID, Relation.HAS_GENRE
            );
      }
      // create relationship ARTIST/SONG
      if(artistID >= 0) {
            createRelationship(
                    Label.ARTIST, artistID, Relation.HAS_SONG,
                    Label.SONGNAME, songID, Relation.HAS_ARTIST);
      }
      // create relationship ALBUM/ARTIST
      if(albumID >= 0 && artistID >= 0) {
            createRelationship(
                    Label.ALBUM, albumID, Relation.HAS_ARTIST,
                    Label.ARTIST, artistID, Relation.HAS_ALBUM);
      }
      // create relationship ALBUM/SONG
      if(albumID >= 0) {
         createRelationship(
                 Label.ALBUM, albumID, Relation.HAS_SONG,
                 Label.SONGNAME, songID, Relation.HAS_ALBUM
         );
      }
   }

   /**
    * Creates a relationship from the first node to the second.
    *
    * @param label1       - first node label
    * @param ID1          - ID of first node
    * @param relation1    - relationship to second node
    * @param label2       - second node label
    * @param ID2          - ID of second node
    * @param relation2    - relationship to first node
    */
   private void createRelationship(String label1, int ID1, String relation1,
                                  String label2, int ID2, String relation2)
   {
      _session.run("MATCH (one:"+label1+") MATCH (two:"+label2+")"+
                           " WHERE id(one)="+ID1+" AND id(two)="+ID2+
                           " MERGE (one)-[:" + relation1 + "]" +
                           "->(two) MERGE (two)-[:"+relation2+"]->(one)");
   }

   /**
    * Returns true if there exists a node with the given parameters
    *
    * @param label   - the node label
    * @param subject - the subject
    * @param value   - the subject value
    * @return - true if node exists
    */
   private boolean nodeExists(String label, String subject, String value)
   {

      return _session.run("MATCH (a:" + label + ") WHERE a." + subject + " = {" + subject + "} " +
                                  "RETURN a." + subject + " AS " + subject,
                          parameters(subject, value)).hasNext();
   }

   /**
    * Returns true if there exists a specific relationship between two nodes.
    *
    * @param label1       - first node label
    * @param ID1          - ID of first node
    * @param relationship - relationship to second node
    * @param label2       - second node label
    * @param ID2          - ID of second node
    * @return - true if that relationship exists
    */
   public boolean relationshipExists(String label1, int ID1, String relationship,
                                     String label2, int ID2)
   {
      String query = "MATCH (n:" + label1 + ")-[r:" + relationship + "]->(m:" + label2 + ") " +
                             "WHERE id(n)=" + ID1 + " AND " + "id(m)=" + ID2 + " RETURN SIGN(COUNT(r))";

      StatementResult result = _session.run(query);
      return result.next().get(0).asInt() > 0;
   }
}
