package Values;

import neo4j.EditRequest;

import java.util.LinkedList;

/**
 * Created by Ryan on 3/4/2017.
 *
 * Property set is pretty much a struct that holds a property and its
 * corresponding value. Has a static method.
 */
public class PropertySet{
    public String prop;
    public String val;

    public PropertySet(String prop, String val){
        this.prop = prop;
        this.val = val;
    }

    /**
     * Creates a list of properties and their newly requested values.
     * Used within Editor.
     * @param req Request parameters to change property values
     * @return
     * @see neo4j.Editor
     */
    public static LinkedList<PropertySet> populateSongProperties(EditRequest req){
        LinkedList<PropertySet> list = new LinkedList<>();
        if(req.comment != null){
            list.add(new PropertySet(Property.COMMENT, req.comment));
        }
        if(req.composer != null){
            list.add(new PropertySet(Property.COMPOSER_NAME, req.composer));
        }
        if(req.discNo != null){
            list.add(new PropertySet(Property.DISC_NO, req.discNo));
        }
        if(req.title != null){
            list.add(new PropertySet(Property.SONG_NAME, req.title));
        }
        if(req.track != null){
            list.add(new PropertySet(Property.TRACK_NUM, req.track));
        }
        if(req.year != null){
            list.add(new PropertySet(Property.YEAR, req.year));
        }
        return list;
    }
}
