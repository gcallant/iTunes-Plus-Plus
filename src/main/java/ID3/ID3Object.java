package ID3;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
//import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;

public class ID3Object {
	private AudioFile _afile;
	private Tag _tag;
	
	public ID3Object(File mediaFile) throws IOException {
		try {
			_afile = AudioFileIO.read(mediaFile);
		} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
				| InvalidAudioFrameException e) {
			throw new IOException("Cannot read media file: " + mediaFile.getName());
		}
		_tag = _afile.getTag();
	}

	//-------------------------------------------------------------------------
	//Gets
	//-------------------------------------------------------------------------
	public String getAlbum(){
		String album = null;
		try{
			album = _tag.getFirst(FieldKey.ALBUM);
		} catch(KeyNotFoundException e){}
		
		return album;
	}

	public String getGenre(){
		String genre = null;
		try{
			genre = _tag.getFirst(FieldKey.GENRE);
		}catch (KeyNotFoundException e)
		{}
		return genre;
	}

	public LinkedList<String> getAll(){
		LinkedList<String> list = new LinkedList<>();
		Iterator<TagField> iterator = _tag.getFields();
		TagField field;
		
		while(iterator.hasNext())
		{
		    field = iterator.next();
		    list.add(_tag.getFirst(field.getId()));
		}
		
		return list;
	}
	
	public String getArtist(){
		String artist = null;
		try{
			artist = _tag.getFirst(FieldKey.ARTIST);
		} catch(KeyNotFoundException e){}
		
		return artist;
	}
	
//	public String getArtistSort(){
//		String artistSort = null;
//		try{
//			artistSort = _tag.getFirst(FieldKey.ARTIST_SORT);
//		} catch(KeyNotFoundException e){}
//		
//		return artistSort;
//	}
	
	public String getComment(){
		String comment = null;
		try{
			comment = _tag.getFirst(FieldKey.COMMENT);
		} catch(KeyNotFoundException e){}
		
		return comment;
	}
	
	public String getComposer(){
		String composer = null;
		try{
			composer = _tag.getFirst(FieldKey.COMPOSER);
		} catch(KeyNotFoundException e){}
		
		return composer;
	}
	
	public String getDiscNo(){
		String discNo = null;
		try{
			discNo = _tag.getFirst(FieldKey.DISC_NO);
		} catch(KeyNotFoundException e){}
		
		return discNo;
	}

	public String getFile(){
		return _afile.getFile().getAbsolutePath();
	}

	public String getTitle(){
		String title = null;
		try{
			title = _tag.getFirst(FieldKey.TITLE);
		} catch(KeyNotFoundException e){}
		
		return title;
	}
	
	public String getTrack(){
		String track = null;
		try{
			track = _tag.getFirst(FieldKey.TRACK);
		} catch(KeyNotFoundException e){}
		
		return track;
	}
	
	public String getYear(){
		String year = null;
		try{
			year = _tag.getFirst(FieldKey.YEAR);
		} catch(KeyNotFoundException e){}
		
		return year;
	}
	
	
	//-------------------------------------------------------------------------
	//Sets
	//-------------------------------------------------------------------------
	public void setAlbum(String album){
		try {
			_tag.setField(FieldKey.ALBUM, album);
			_afile.commit();
		} catch (KeyNotFoundException | FieldDataInvalidException e) {
			System.err.println("Invalid entry for 'Album': " + album);
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}
	
	public void setArtist(String artist){
		try {
			_tag.setField(FieldKey.ARTIST, artist);
			_afile.commit();
		} catch (KeyNotFoundException | FieldDataInvalidException e) {
			System.err.println("Invalid entry for 'Artist': " + artist);
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}
	
//	public void setArtistSort(String artists){
//		try {
//			_tag.setField(FieldKey.ARTIST_SORT, artists);
//			_afile.commit();
//		} catch (KeyNotFoundException | FieldDataInvalidException e) {
//			System.err.println("Invalid entry for 'ArtistSort': " + artists);
//		} catch (CannotWriteException e) {
//			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
//		}
//	}
	
	public void setComment(String comment){
		try {
			_tag.setField(FieldKey.COMMENT, comment);
			_afile.commit();
		} catch (KeyNotFoundException | FieldDataInvalidException e) {
			System.err.println("Invalid entry for 'Comment': " + comment);
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}
	
	public void setComposer(String composer){
		try {
			_tag.setField(FieldKey.COMPOSER, composer);
			_afile.commit();
		} catch (KeyNotFoundException | FieldDataInvalidException e) {
			System.err.println("Invalid entry for 'Composer': " + composer);
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}
	
	public void setDiscNo(String discNo){
		try {
			_tag.setField(FieldKey.DISC_NO, discNo);
			_afile.commit();
		} catch (KeyNotFoundException | FieldDataInvalidException e) {
			System.err.println("Invalid entry for 'DiscNo': " + discNo);
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}
	
	public void setTitle(String title){
		try {
			_tag.setField(FieldKey.TITLE, title);
			_afile.commit();
		} catch (KeyNotFoundException | FieldDataInvalidException e) {
			System.err.println("Invalid entry for 'Title': " + title);
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}
	
	public void setTrack(String track){
		try {
			_tag.setField(FieldKey.TRACK, track);
			_afile.commit();
		} catch (KeyNotFoundException | FieldDataInvalidException e) {
			System.err.println("Invalid entry for 'Track': " + track);
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}

	public void setYear(String year){
		try {
			_tag.setField(FieldKey.YEAR, year);
			_afile.commit();
		} catch (KeyNotFoundException | FieldDataInvalidException e) {
			System.err.println("Invalid entry for 'Year': " + year);
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}
	
	//-------------------------------------------------------------------------
	//Delete
	//-------------------------------------------------------------------------

	public void delAlbum(){
		try {
			_tag.deleteField(FieldKey.ALBUM);
			_afile.commit();
		} catch (KeyNotFoundException e) {
			System.err.println("Cannot remove 'Album'");
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}
	
	public void delArtist(){
		try {
			_tag.deleteField(FieldKey.ARTIST);
			_afile.commit();
		} catch (KeyNotFoundException e) {
			System.err.println("Cannot remove 'Artist'");
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}

//	public void delArtistSort(){
//		try {
//			_tag.deleteField(FieldKey.ARTIST_SORT);
//			_afile.commit();
//		} catch (KeyNotFoundException e) {
//			System.err.println("Cannot remove 'ArtistSort'");
//		} catch (CannotWriteException e) {
//			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
//		}
//	}
	
	public void delComment(){
		try {
			_tag.deleteField(FieldKey.COMMENT);
			_afile.commit();
		} catch (KeyNotFoundException e) {
			System.err.println("Cannot remove 'Comment'");
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}
	
	public void delComposer(){
		try {
			_tag.deleteField(FieldKey.COMPOSER);
			_afile.commit();
		} catch (KeyNotFoundException e) {
			System.err.println("Cannot remove 'Composer'");
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}
	
	public void delDiscNo(){
		try {
			_tag.deleteField(FieldKey.DISC_NO);
			_afile.commit();
		} catch (KeyNotFoundException e) {
			System.err.println("Cannot remove 'DiscNo'");
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}
	
	public void delTitle(){
		try {
			_tag.deleteField(FieldKey.TITLE);
			_afile.commit();
		} catch (KeyNotFoundException e) {
			System.err.println("Cannot remove 'Title' tag");
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}
	
	public void delTrack(){
		try {
			_tag.deleteField(FieldKey.TRACK);
			_afile.commit();
		} catch (KeyNotFoundException e) {
			System.err.println("Cannot remove 'Track' tag");
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}
	
	public void delYear(){
		try {
			_tag.deleteField(FieldKey.YEAR);
			_afile.commit();
		} catch (KeyNotFoundException e) {
			System.err.println("Cannot remove 'Year' tag");
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}
}
