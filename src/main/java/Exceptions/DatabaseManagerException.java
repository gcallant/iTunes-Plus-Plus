package Exceptions;

import guiInterface.Controller;
import javafx.scene.control.Alert;

/**
 * Created by Grant
 *
 * @author Grant Callant
 */
public class DatabaseManagerException extends Exception
{
   public DatabaseManagerException(String message)
   {
      //No Body
   }

   public DatabaseManagerException(String message, Throwable cause)
   {

   }

   public DatabaseManagerException()
   {
      //No Body
   }

   public DatabaseManagerException(Throwable cause)
   {
      //No Body
   }

   public DatabaseManagerException(String header, String message, Alert.AlertType alertType)
   {
      Controller.showAlert(header, message, alertType);
   }

   public DatabaseManagerException notClosable(String message, Throwable cause)
   {
      return new DatabaseManagerException(message, cause);
   }
}