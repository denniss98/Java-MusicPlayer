import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ProgressBar;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class MusicPLayer extends Thread {

    private static String file_extension = ".mp3";
    private static String songFolder;
    private static String songListFilePath;
    private final ProgressBar progress = new ProgressBar();
    private ChangeListener<Duration> progressChangeListener;
    public MediaView mediaView;
    public static int index = 0;
    public static List<MediaPlayer> players = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException {
        // Initiliazies a JFXPanel to use Media Player
        JFXPanel fxPanel = new JFXPanel();
        //List of song names taken from data file
        List<Song> songList = new ArrayList<Song>();
        MusicPLayer mp = new MusicPLayer();
        mp.addArgs(args[0], args[1], songList);

        mp.createMediaPlayer(songList);

        try {
            mp.mediaView = new MediaView(players.get(index));

            for (int i = 0; i < players.size(); i++) {
                MediaPlayer player = players.get(i);
                MediaPlayer nextPlayer = players.get((i + 1) % players.size());
                player.setOnEndOfMedia(() -> {
                    final MediaPlayer curPlayer = mp.mediaView.getMediaPlayer();
                    nextPlayer.seek(Duration.ZERO);
                    if (nextPlayer != curPlayer) {
                        try {
                            curPlayer.currentTimeProperty().removeListener(mp.progressChangeListener);
                        } catch (Exception e) {
                        }
                    }
                    mp.mediaView.setMediaPlayer(nextPlayer);
                    nextPlayer.play();
                });
            }
            mp.mediaView.setMediaPlayer(players.get(index));
            // Plays newly created playlist
            mp.mediaView.getMediaPlayer().play();
        } catch (NullPointerException ex) {
            System.out.println("Song is over! Press 8 to play next");
        }

        String input;
        do {
            System.out.println("1. Add song to song queue");
            System.out.println("2. Search song by artist name");
            System.out.println("3. Add song (specific position)");
            System.out.println("4. View songs in playlist");
            System.out.println("5. Reorder songs");
            System.out.println("6. Delete songs");
            System.out.println("7. Play/Pause");
            System.out.println("8. Play next song");
            System.out.print("Enter choice:>");
            Scanner sc = new Scanner(System.in);
            input = sc.nextLine();

            switch (input) {

                case "0":
                    System.out.println("Exiting Program...");
                    break;

                case "1":
                    mp.addSong(songList);
                    break;

                case "2":
                    mp.searchSong(songList);
                    break;

                case "3":

                    mp.addSongAtIndex(songList);
                    break;

                case "4":
                    // formats data
                    System.out.println(songList.size());
                    System.out.format("%-30s %-30s %-30s %-30s", "Song Title", "Artist", "Playtime", "Filename");
                    for (Song song : songList) {
                        System.out.println(song.toString());
                    }
                    break;
                    
                case "5":
                    Collections.shuffle(songList);
                    mp.createMediaPlayer(songList);
                    break;
                case "6":
                    // formats data
                    mp.deleteSong(songList);
                    break;

                case "7":

                    mp.playPause(mp.mediaView);

                    break;

                case "8":
                    try {
                        mp.playPause(mp.mediaView);
                        index += 1;
                        mp.mediaView.setMediaPlayer(players.get(index));
                        mp.mediaView.getMediaPlayer().play();
                    } catch (IndexOutOfBoundsException ex) {
                        System.out.println("Queue has ended, rerun the program to play again!");
                        mp.index = 0;
                    }
                    break;
            }
        } while (!input.equals("0"));
    }

    private void setCurrentlyPlaying(final MediaPlayer newPlayer) {
        progress.setProgress(0);
        progressChangeListener = (observableValue, oldValue, newValue)
                -> progress.setProgress(
                        1.0 * newPlayer.getCurrentTime().toMillis() / newPlayer.getTotalDuration().toMillis()
                );
        newPlayer.currentTimeProperty().addListener(progressChangeListener);
    }

    // Pauses and Plays the playlist
    public void playPause(MediaView mediaview) {
        String status = mediaview.getMediaPlayer().getStatus().toString();
        if (mediaview.getMediaPlayer().getStatus().toString() == "PAUSED") {
            mediaview.getMediaPlayer().play();
        } else {
            mediaview.getMediaPlayer().pause();
        }
    }

    // Creates new media player
    public void createMediaPlayer(List<Song> songList) {
        File dir = new File(songFolder);
        HashSet<String> hs = new HashSet<String>();
        for (String file : dir.list()) {
            if (file.endsWith(file_extension)) {
                hs.add(file.toLowerCase());
            }
        }
        for (Song song : songList) {
            if (hs.contains(song.getFileName().toLowerCase())) {
                Media hit = new Media(new File(songFolder).toURI()+""+song.getFileName());
                players.add(new MediaPlayer(hit));
            }
        }
    }

    // Populates song list
    public void addArgs(String arg1, String arg2, List<Song> songList) throws FileNotFoundException {
        songFolder = arg1;
        songListFilePath = arg2;
        File songListFile = new File(arg2);
        Scanner in = new Scanner(songListFile);
        while (in.hasNextLine()) {
            String[] splitArray = in.nextLine().split("\t");
            Song song = new Song(splitArray[0].toLowerCase(), splitArray[1].toLowerCase(), Integer.parseInt(splitArray[2]), splitArray[3].toLowerCase());
            songList.add(song);
        }
    }

    // Adds new song to playlist
    public void addSong(List<Song> songList) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the Song title");
        String title = sc.nextLine();
        System.out.println("Enter the Artist name");
        String artist = sc.nextLine();
        System.out.println("Enter the playtime(seconds)");
        int playtime = Integer.parseInt(sc.nextLine());
        System.out.println("Enter the mp3 filename(e.g. mockingbird.mp3)");
        String filename = sc.nextLine();
        Song song = new Song(title, artist, playtime, filename);
        songList.add(song);
        Media hit = new Media(new File(songFolder).toURI()+""+song.getFileName());
        players.add(new MediaPlayer(hit));
    }

    // Adds a new song at the specified index to the list
    public void addSongAtIndex(List<Song> songList) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the Song title");
        String title = sc.nextLine();
        System.out.println("Enter the Artist name");
        String artist = sc.nextLine();
        System.out.println("Enter the playtime");
        int playtime = Integer.parseInt(sc.nextLine());
        System.out.println("Enter the mp3 filename");
        String filename = sc.nextLine();
        System.out.println("Enter the index at which you want to add song");
        int index = Integer.parseInt(sc.nextLine());
        Song song = new Song(title, artist, playtime, filename);
        songList.add(index, song);
        Media hit = new Media(new File(songFolder).toURI()+""+song.getFileName());
        players.add(index, new MediaPlayer(hit));
    }

    // Deletes a song
    public void deleteSong(List<Song> songList) {
        Song removedSong = null;
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the Song title to be deleted");
        String title = sc.nextLine();
        Iterator<Song> it = songList.iterator();
        while (it.hasNext()) {
            removedSong = it.next();
            String titles = removedSong.getSongTitle();
            if (removedSong.getSongTitle().equalsIgnoreCase(title)) {
                break;
            }
        }
        songList.remove(removedSong);

        Iterator<MediaPlayer> mepl = players.iterator();
        MediaPlayer mediapl = null;
        while (mepl.hasNext()) {
            mediapl = mepl.next();
            String[] source = mediapl.getMedia().getSource().split("/");
            if (removedSong.getFileName().equalsIgnoreCase(source[source.length - 1])) {
                break;
            }
        }
        players.remove(mediapl);
    }

    // Searches fora song
    public void searchSong(List<Song> songList) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the artist name to search for the song");
        String artist = sc.nextLine();
        System.out.format("%-30s %-30s %-30s %-30s", "Song Title", "Artist", "Playtime", "Filename");
        System.out.println();
        for (Song song : songList) {
            if (song.getArtist().equalsIgnoreCase(artist)) {
                System.out.format("%-30s %-30s %-30s %-30s", song.getSongTitle(), song.getArtist(), song.getPlayingTime(), song.getFileName());
                System.out.println();
            }
        }
    }
}
