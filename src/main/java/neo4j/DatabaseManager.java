package neo4j;

import Utilities.OSUtil;
import guiInterface.Controller;
import javafx.scene.control.Alert;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


/**
 * Created by Grant. Manages a neo4J database object- constructor will create
 * database if it does not exist, or connect to existing database.
 *
 * @author Grant Callant
 */
public class DatabaseManager
{
   public  final static String  _URL               = "localhost";
   public  final static String  _USERNAME          = "iTunes";
   public  final static String  _PASSWORD          = "iTunes";
   public  final static int     _PORT              = 7687;
   private static final String  SEPARATOR          = OSUtil.getSeparator();
   private static final File    EXTERNAL_DIRECTORY = OSUtil.getExternalDirectory();
   private final static String  RESOURCES          = EXTERNAL_DIRECTORY.getAbsolutePath();
   private final        Logger  log4JLogger        = LoggerFactory.getLogger(this.getClass());
   private              Driver  driver             = null;
   private              Session databaseConnector  = null;
   private Log dbLogger;
   private static DatabaseManager INSTANCE = null;



   private DatabaseManager(String url, int port, String username, String password)
   {
      log4JLogger.info("Creating connection to database");
      driver = GraphDatabase.driver("bolt://"+url+":"+port, AuthTokens.basic(username, password));
      databaseConnector = driver.session();
      if(databaseConnector.isOpen())
      {
         log4JLogger.info("Connection successful to {}", databaseConnector);
      }
      else
      {
         log4JLogger.warn("Connection failed to {}", databaseConnector);
      }
   }

   @org.jetbrains.annotations.Contract (pure = true)
   public static DatabaseManager getInstance(String url, int port, String username, String password)
   {
      if(INSTANCE == null)
         INSTANCE = new DatabaseManager(url, port, username, password);
      return INSTANCE;
   }

   public static DatabaseManager getInstance(){
      if(INSTANCE == null)
         INSTANCE = new DatabaseManager(_URL, _PORT, _USERNAME, _PASSWORD);
      return INSTANCE;
   }

   public Session getDatabaseConnector()
   {
      return databaseConnector;
   }

   public void closeConnection()
   {
      if(databaseConnector.isOpen())
      {
         databaseConnector.close();
      }
      driver.close();
   }
}
