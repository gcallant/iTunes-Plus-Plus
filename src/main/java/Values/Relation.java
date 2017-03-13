package Values;

/**
 * Created by Ryan on 1/30/2017.
 */
public class Relation {
    public final static String HAS_ALBUM = "hasAlbum";
    public final static String HAS_ARTIST = "hasArtist";
    public final static String HAS_GENRE = "hasGenre";
    public final static String HAS_SONG = "hasSong";

    public static String getRelationship(String label){
        switch (label){
            case Label.ALBUM :
                return HAS_ALBUM;
            case Label.ARTIST :
                return HAS_ARTIST;
            case Label.GENRE :
                return HAS_GENRE;
            case Label.SONGNAME :
                return HAS_SONG;
            default:
                return null;

        }
    }
}
 