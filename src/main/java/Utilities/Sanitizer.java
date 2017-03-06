package Utilities;

/**
 * Created by Ryan on 3/5/2017.
 * Sanitizes user input
 */
public class Sanitizer {

    /**
     * Sanitizes strings for database entry.
     *
     * @param dirty - the dirty string
     * @return - clean string
     */
    public static String sanitize(String dirty){
        if(dirty == null) return null;
        return dirty.replace('\"', '\'').replace("\\", "//");
    }
}
