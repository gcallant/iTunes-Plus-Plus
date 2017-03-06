package neo4j;

import Utilities.ID3Object;

/**
 * Created by Ryan on 2/19/2017.
 *
 * EditRequest is based on a selected song. Given to Editor class to edit
 * the values of nodes, properties, and relationships.
 */
public class EditRequest {
    private EditRequest original;
    public String album;
    public String artist;
    public String composer;
    public String comment;
    public String discNo;
    public String title;
    public String track;
    public String year;
    public String genre;
    public String filename;

    public EditRequest(){}
    public EditRequest(EditRequest original){
        if(original == null) this.original = original;
        sync();
    }
    public EditRequest(ID3Object id3){
        original = new EditRequest();
        original.album = id3.getAlbum();
        original.artist = id3.getArtist();
        original.composer = id3.getComposer();
        original.comment = id3.getComment();
        original.discNo = id3.getDiscNo();
        original.title = id3.getTitle();
        original.track = id3.getTrack();
        original.year = id3.getYear();
        original.genre = id3.getGenre();
        original.filename = id3.getFile();
        sync();
    }

    public EditRequest getOriginal(){return original;}

    public boolean sameAlbum(){
        if(original == null) {
            return false;
        } else if((original.album != null && this.album == null)
                || (original.album == null && album != null)){
            return false;
        } else if(original.album == null && album == null){
            return true;
        } else {
            return original.album.equals(album);
        }
    }

    public boolean sameArtist(){
        if(original == null) {
            return false;
        } else if((original.artist != null && this.artist == null)
                || (original.artist == null && artist != null)){
            return false;
        } else if(original.artist == null && artist == null){
            return true;
        } else {
            return original.artist.equals(artist);
        }
    }

    public boolean sameGenre(){
        if(original == null) {
            return false;
        } else if((original.genre != null && genre == null)
                || (original.genre == null && genre != null)){
            return false;
        } else if(original.genre == null && genre == null){
            return true;
        } else {
            return original.genre.equals(genre);
        }
    }

    /**
     * Updates old filename to the current version. Used
     * in cases where the filename has to be a specific
     * format and the old node is called to remove
     * stale information.
     */
    public void updateOriginalFilename(){
        original.filename = filename;
    }

    private void sync(){
        album = original.album;
        artist = original.artist;
        composer = original.composer;
        comment = original.comment;
        discNo = original.discNo;
        title = original.title;
        track = original.track;
        year = original.year;
        genre = original.genre;
        filename = original.filename;
    }
}
