package neo4j;

/**
 * Created by Ryan on 2/19/2017.
 */
public class EditRequest {
    public String key;
    public String label;
    public String prop;
    public String value;

    public EditRequest(String key, String label, String prop, String value){
        this.key = key;
        this.label = label;
        this.prop = prop;
        this.value = value;
    }
}
