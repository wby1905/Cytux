package cufe.wby.cytux.Class;

import java.io.Serializable;

public class Music implements Serializable {

    private String name;
    private final String path;
    private String artist;
    private int albumId;
    private float bpm = 0;
    private long duration;
    private long size;

    public Music(String name, String path, String artist, int albumId, long duration, long size) {
        this.name = name;
        this.path = path;
        this.artist = artist;
        this.albumId = albumId;
        this.duration = duration;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public int getAlbumId() {
        return albumId;
    }

    public String getPath() {
        return path;
    }

    public String getArtist() {
        return artist;
    }

    public float getBpm() {
        return bpm;
    }

    public long getDuration() { return  duration; }

    public long getSize() { return  size; }

    public void setBpm(float bpm) {
        this.bpm = bpm;
    }

}
