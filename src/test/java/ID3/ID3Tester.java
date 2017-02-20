package ID3;

import java.io.File;
import java.io.IOException;

public class ID3Tester {

	public static void main(String...args) {//C:\Users\Ryan\Documents\GitHub\iTunes-Plus-Plus\src\main\resources\Music
		File mediaFile = new File(".\\src\\main\\resources\\Music\\Blind Melon - No Rain.mp3");
		ID3Object id3 = null;
		
		try {
			id3 = new ID3Object(mediaFile);
		} catch (IOException e) {
			System.err.println("Unable to read media file");
			System.exit(-1);
		}
		
		System.out.println("Album: " + id3.getAlbum());
		System.out.println("Artist: " + id3.getArtist());
		System.out.println("Comment: " + id3.getComment());
		System.out.println("Composer: " + id3.getComposer());
		System.out.println("Title: " + id3.getTitle());
		System.out.println("Track: " + id3.getTrack());
		System.out.println("Year: " + id3.getYear());		
		System.out.println("ALL: " + id3.getAll());
		
		id3.setArtist("Steve Jobs");
//		id3.setArtistSort("Steve Jobs/Bill Gates/Linus Torvald");
		id3.setAlbum("MAC");
		id3.setComment("Hate this song");
		id3.delComposer();
		id3.delDiscNo();
		id3.setTitle("Rain");
		id3.delTrack();
		id3.delTrack();
		id3.setYear("1950");
		System.out.println("ALL: " + id3.getAll());

		id3.setArtist("Blind Melon");
//		id3.delArtistSort();
		id3.setAlbum("Blind Melon");
		id3.setComment("Good song");
		id3.delComposer();
		id3.setDiscNo("1");
		id3.setTitle("No Rain");
		id3.setTrack("1");
		id3.setYear("1991");
		System.out.println("ALL: " + id3.getAll());		
		
	}

}
