package Values;

//links
//neo4j: https://neo4j.com/developer/guide-data-modeling/


import ID3.ID3Object;

import java.io.File;
import java.io.IOException;

/**
 * Created by Ryan on 1/31/2017.
 *
 * The idea behind labels, props, and relations
 *
 * We are using these to stay consistent with our data, as well as organize
 * the music into our database in a sensible fashion. I.e. customers are not
 * going to make query searches on disk or track numbers, so they would be
 * properties. However, someone could want to find artists from the 90s,
 * so year is a label. Relationships describe the connection between
 * the labels.
 *
 */
public class ValueTester {
    public static void main(String...args){
        File mediaFile = new File("src\\main\\resources\\Music\\Blind Melon - No Rain.mp3");
        ID3Object id3 = null;

        try {
            id3 = new ID3Object(mediaFile);
        } catch (IOException e) {
            System.err.println("Unable to read media file");
            System.exit(-1);
        }

        String song = id3.getTitle();
        String file = id3.getFile();
        String discNo = id3.getDiscNo();
        String track = id3.getTrack();

//        importAlbum();
//        importArtist();
//        importComment();
//        importComposer();
        importSong(song, file, discNo, track);
//        importYear();

        String fileQuery1 = querySong(song);
        String fileQuery2 = querySong("poop mcGoop");

//        Map<String, Object> params = MapUtil.map(song, song);
//        GraphDatabaseService graphDB = new GraphDatabaseService();
//        graphDB.executre(fileQuery1, params);

        System.out.println(fileQuery1);
        System.out.println(fileQuery2);
    }

    private static void importSong(String song, String file, String discNo, String track){
        StringBuilder query = new StringBuilder();

        query.append("CREATE (" + song + "):" + Label.SONG
                + " {" + Prop.NAME + ": " + song + ", " + Prop.FILENAME+ ": "
                + file);

        if(discNo != null){
            query.append(", " + Prop.DISC_NO + ": " + discNo);
        }
        if(track != null){
            query.append(", " + Prop.TRACK + ": " + track);
        }

        query.append("})");

        System.out.println(query.toString());
    }

    private static String querySong(String song){
        StringBuilder query = new StringBuilder();

        query.append("MATCH (" + song + ":" + Label.SONG + ")");
        query.append(" WHERE " + song + "." + Prop.NAME + " = {" + song + "}");
        query.append(" RETURN " + song + "." + Prop.FILENAME);

        return query.toString();
    }
}
