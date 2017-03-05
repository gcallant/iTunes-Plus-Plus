package neo4j;

import Utilities.ID3Object;
import Values.Label;
import Values.Property;
import Values.Relation;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Ryan on 2/19/2017.
 *
 * EditRequest is based on a selected song. Given to Editor class to edit
 * the values of nodes, properties, and relationships.
 */
public class EditRequest {
    public String ID; //the ID of selected song
    public String label;
    public String prop;
    public String value;

    public String album;
    public String artist;
    public String composer;
    public String comment;
    public String songName;
    public String track;
    public String year;
    public String genre;
    public String fileName;

    public EditRequest(){}

    public EditRequest(String ID, String label, String prop, String value){
        this.ID = ID;
        this.label = label;
        this.prop = prop;
        this.value = value;
    }

    public ID3Object getID3(Session session){
        if(!label.equals(Label.SONGNAME)){
            throw new IllegalArgumentException("Label must be a song name");
        }
        ID3Object id3 = null;
        StringBuilder query = new StringBuilder();

        query.append("MATCH (n) WHERE ID(n)=").append(ID).append(" ");
        query.append("RETURN n.").append(Property.FILENAME);

        StatementResult result = session.run(query.toString());
        Record record = result.next();
        File path = new File(record.get(0).asString());

        try{
            id3 = new ID3Object(path);
        }catch(IOException e){
            System.err.println("Cannot open id3 tag...");
        }

        return id3;
    }
}
