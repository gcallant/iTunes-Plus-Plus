package Values;

/**
 * Created by Ryan on 1/30/2017.
 */
public class Property {
    public final static String ALBUM_NAME = "albumName";
    public final static String ARTIST_NAME = "artistName";
    public final static String COMMENT = "commentText";
    public final static String COMPOSER_NAME = "composerName";
    public final static String DISC_NO = "discNum";
    public final static String FILENAME = "fileLocation";
    public final static String GENRE_NAME = "genreName";
    public final static String SONG_NAME = "songName";
    public final static String TRACK_NUM = "trackNumber";
    public final static String YEAR = "year";

    public static String getPropNameByLabel(String label){
        switch(label){
            case Label.ALBUM :
                return Property.ALBUM_NAME;
            case Label.ARTIST :
                return Property.ARTIST_NAME;
            case Label.GENRE :
                return Property.GENRE_NAME;
            default :
                return Property.SONG_NAME;
        }
    }
}
