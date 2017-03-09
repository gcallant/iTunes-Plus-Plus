package Utilities;

import Values.Property;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.*;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

//import org.jaudiotagger.audio.AudioHeader;

public class ID3Object {
	private AudioFile _afile;
	private Tag _tag;
	
	public ID3Object(File mediaFile) throws IOException {
      Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
		try {
			_afile = AudioFileIO.read(mediaFile);
		} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
				| InvalidAudioFrameException e) {
			throw new IOException("Cannot read media file: " + mediaFile.getName());
		}
		_tag = _afile.getTag();
	}

	public static ID3Object findBySongID(Session session, String songID)
	throws IOException{
		StringBuilder query = new StringBuilder();

		query.append("MATCH (n) WHERE ID(n)=").append(songID).append(" ");
		query.append("RETURN n.").append(Property.FILENAME);

		StatementResult result = session.run(query.toString());
		Record record = result.next();
		File path = new File(record.get(0).asString());

		return new ID3Object(path);
	}

//-----------------------------------------------------------------------------
//Gets
//-----------------------------------------------------------------------------
	public String getAlbum(){
		String album = null;
		try{
			album = _tag.getFirst(FieldKey.ALBUM);
		} catch(KeyNotFoundException | NullPointerException e){}
		
		return album;
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
		} catch(KeyNotFoundException | NullPointerException e){}
		
		return artist;
	}

	public String getComment(){
		String comment = null;
		try{
			comment = _tag.getFirst(FieldKey.COMMENT);
		} catch(KeyNotFoundException | NullPointerException e){}
		
		return comment;
	}
	
	public String getComposer(){
		String composer = null;
		try{
			composer = _tag.getFirst(FieldKey.COMPOSER);
		} catch(KeyNotFoundException | NullPointerException e){}
		
		return composer;
	}
	
	public String getDiscNo(){
		String discNo = null;
		try{
			discNo = _tag.getFirst(FieldKey.DISC_NO);
		} catch(KeyNotFoundException | NullPointerException e){}
		
		return discNo;
	}

	public String getFile(){
		return _afile.getFile().getAbsolutePath();
	}

	public String getGenre(){
		String genre = null;
		try{
			genre = _tag.getFirst(FieldKey.GENRE);
		} catch(KeyNotFoundException | NullPointerException e){}

		return genre;
	}

	public String getTitle(){
		String title = null;
		try{
			title = _tag.getFirst(FieldKey.TITLE);
		} catch(KeyNotFoundException | NullPointerException e){}
		
		return title;
	}
	
	public String getTrack(){
		String track = null;
		try{
			track = _tag.getFirst(FieldKey.TRACK);
		} catch(KeyNotFoundException | NullPointerException e){}
		
		return track;
	}
	
	public String getYear(){
		String year = null;
		try{
			year = _tag.getFirst(FieldKey.YEAR);
		} catch(KeyNotFoundException | NullPointerException e){}
		
		return year;
	}

//-----------------------------------------------------------------------------
//Sets
//-----------------------------------------------------------------------------
	public void setAlbum(String album){
		if(album == null) return;
		if(album.equals("")){
			delAlbum();
			return;
		}
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
		if(artist == null) return;
		if(artist.equals("")){
			delArtist();
			return;
		}
		try {
			_tag.setField(FieldKey.ARTIST, artist);
			_afile.commit();
		} catch (KeyNotFoundException | FieldDataInvalidException e) {
			System.err.println("Invalid entry for 'Artist': " + artist);
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}

	public void setComment(String comment){
		if(comment == null) return;
		if(comment.equals("")){
			delComment();
			return;
		}
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
		if(composer == null) return;
		if(composer.equals("")){
			delComposer();
			return;
		}
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
		if(discNo == null) return;
		if(discNo.equals("")){
			delDiscNo();
			return;
		}
		try {
			_tag.setField(FieldKey.DISC_NO, discNo);
			_afile.commit();
		} catch (KeyNotFoundException | FieldDataInvalidException e) {
			System.err.println("Invalid entry for 'DiscNo': " + discNo);
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}

	public void setGenre(String genre){
		if(genre == null) return;
		if(genre.equals("")){
			delGenre();
			return;
		}
		try {
		_tag.setField(FieldKey.GENRE, genre);
		_afile.commit();
		} catch (KeyNotFoundException | FieldDataInvalidException e) {
			System.err.println("Invalid entry for 'Genre': " + genre);
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}

	public void setTitle(String title){
		if(title == null) return;
		if(title.equals("")){
			delTitle();
			return;
		}
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
		if(track == null) return;
		if(track.equals("")){
			delTrack();
			return;
		}
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
		if(year == null) return;
		if(year.equals("")){
			delYear();
			return;
		}
		try {
			_tag.setField(FieldKey.YEAR, year);
			_afile.commit();
		} catch (KeyNotFoundException | FieldDataInvalidException e) {
			System.err.println("Invalid entry for 'Year': " + year);
		} catch (CannotWriteException e) {
			System.err.println("Unable to update audiofile: " + _afile.getFile().getName());
		}
	}
	
//-----------------------------------------------------------------------------
//Delete
//-----------------------------------------------------------------------------
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

	public void delGenre(){
		try {
			_tag.deleteField(FieldKey.GENRE);
			_afile.commit();
		} catch (KeyNotFoundException e) {
			System.err.println("Cannot remove 'Genre'");
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
