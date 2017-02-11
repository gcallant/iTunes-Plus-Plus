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
   private static final String  SEPARATOR          = OSUtil.getSeparator();
   private static final File    EXTERNAL_DIRECTORY = OSUtil.getExternalDirectory();
   private final static String  RESOURCES          = EXTERNAL_DIRECTORY.getAbsolutePath();
   private final        Logger  log4JLogger        = LoggerFactory.getLogger(this.getClass());
   private              Driver  driver             = null;
   private              Session databaseConnector  = null;
   private Log dbLogger;

   private DatabaseManager()
   {
      log4JLogger.info("Creating connection to database");
      driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("iTunes", "iTunes"));
      databaseConnector = driver.session();
      if(databaseConnector.isOpen())
      {
         log4JLogger.info("Connection successful to {}", databaseConnector);
      }
      else
      {
         Controller.showAlert("Oh no! An error occurred!", "Couldn't establish neo4J instance", Alert.AlertType.ERROR);
      }
   }

   @org.jetbrains.annotations.Contract (pure = true)
   public static DatabaseManager getInstance()
   {
      return DatabaseSingle.INSTANCE;
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

   private static class DatabaseSingle
   {
      private static final DatabaseManager INSTANCE = new DatabaseManager();
   }
}
