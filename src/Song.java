import java.util.Scanner;

public class Song {
    private String songTitle;
    private String artist;
    private int playingTime;
    private String fileName;
    Scanner sc = new Scanner(System.in);

    public Song(String songTitle, String artist, int playingTime, String fileName) {
        this.songTitle = songTitle;
        this.artist = artist;
        this.playingTime = playingTime;
        this.fileName = fileName;
    }

    public String getSongTitle() {
        return this.songTitle;
    }

    public String getArtist() {
        return this.artist;
    }

    public int getPlayingTime() {
        return this.playingTime;
    }

    public String getFileName() {
        return this.fileName;
    }

    @Override
    public String toString() {
        return System.out.format("%-30s %-30s %-30s %-30s", this.songTitle, this.artist, this.playingTime, this.fileName).toString();
    }

}
