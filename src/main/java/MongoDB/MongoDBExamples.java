package MongoDB;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoNamespace;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;


//http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.birt.doc%2Fbirt%2Fcon-HowToSpecifyConnectionInformationForMongoDBDataSource.html

public class MongoDBExamples {

	private final static String DATABASE = "mongo-db-test";
	private final static String USER = "root";
	
	public static void main(String...args) {
		//Get Database
		System.out.println("Get database...");
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		MongoClient mongo = new MongoClient(new ServerAddress(ip, 27000));
		MongoNamespace.checkDatabaseNameValidity(DATABASE);
		MongoDatabase db = mongo.getDatabase(DATABASE);
		db.createCollection("songs");
		
		//Display all databases
		System.out.println("Display all DBs...");
		MongoIterable<String> dbs = mongo.listDatabaseNames();
		for(String dbName : dbs){
			System.out.println(dbName);
		}
		
		//Display all collections
		System.out.println("Display all collections...");
		MongoIterable<String> tables = db.listCollectionNames();
		for(String collection : tables){
			System.out.println(collection);
		}

		//Insertion
		MongoCollection<Document> table = db.getCollection(USER);
		Document doc = new Document();
		doc.put("name",  "ryan");
		doc.put("age",  30);
		doc.put("date-created", new Date());
		table.insertOne(doc);
		
		//Update example
		Document query = new Document();
		query.put("name", "ryan");
		Document newDoc = new Document();
		newDoc.put("name", "Ryan");
		Document updateName = new Document();
		updateName.put("$set", newDoc);
		table.updateOne(query, updateName);
		
		//Find example
		Document search = new Document();
		search.put("name", "Ryan");
		MongoCursor<Document> cursor = table.find(search).iterator();
		while(cursor.hasNext()){
			System.out.println(cursor.next().toJson());
		}
		
		//Delete example
		Document toDelete = new Document();
		toDelete.put("name", "Ryan");
		table.deleteOne(toDelete);
		
		mongo.close();
	}

}
