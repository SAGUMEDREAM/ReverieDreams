package nota.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class Playlist {
	ArrayList<Song> songs = new ArrayList<>();

	public Playlist(Song... songs) {
		if(songs.length == 0) {
			throw new IllegalArgumentException("Cannot create empty playlist");
		}
		checkNull(songs);
		this.songs.addAll(Arrays.asList(songs));
	}

	/**
	 * Add array of {@link Song} to playlist
	 *
	 * @param songs additional songs
	 */
	public void add(Song... songs) {
		if(songs.length == 0) {
			return;
		}
		checkNull(songs);
		this.songs.addAll(Arrays.asList(songs));
	}

	/**
	 * Insert array of {@link Song} at a specified index
	 *
	 * @param index insertion index
	 * @param songs additional songs
	 */
	public void insert(int index, Song... songs) {
		if(songs.length == 0) {
			return;
		}
		if(index > this.songs.size()) {
			throw new IllegalArgumentException("Index is higher than playlist size");
		}
		checkNull(songs);
		this.songs.addAll(index, Arrays.asList(songs));
	}

	private void checkNull(Song... songs) {
		List<Song> songList = Arrays.asList(songs);
		if(songList.contains(null)) {
			throw new IllegalArgumentException("Cannot add null to playlist");
		}
	}

	/**
	 * Removes songs from playlist
	 *
	 * @param songs songs to remove
	 * @throws IllegalArgumentException when you try to remove all {@link Song} from {@link Playlist}
	 */
	public void remove(Song... songs) {
		ArrayList<Song> songsTemp = new ArrayList<>(this.songs);
		songsTemp.removeAll(Arrays.asList(songs));
		if(songsTemp.size() > 0) {
			this.songs = songsTemp;
		}
		else {
			throw new IllegalArgumentException("Cannot remove all songs from playlist");
		}
	}

	/**
	 * Get {@link Song} in playlist at specified index
	 *
	 * @param songNumber - song index
	 * @return song
	 */
	public Song get(int songNumber) {
		return songs.get(songNumber);
	}

	/**
	 * Get number of {@link Song} in playlist
	 *
	 * @return songs array size
	 */
	public int getCount() {
		return songs.size();
	}

	/**
	 * Check whether {@link Song} is not last in playlist
	 *
	 * @param songNumber song index
	 * @return true if there is another {@link Song} after specified index
	 */
	public boolean hasNext(int songNumber) {
		return songs.size() > (songNumber + 1);
	}

	/**
	 * Check whether {@link Song} with specified index exists in playlist
	 *
	 * @param songNumber song index
	 * @return if index exist
	 */
	public boolean exist(int songNumber) {
		return songs.size() > songNumber;
	}

	/**
	 * Returns index of song.
	 *
	 * @param song song
	 * @return Index of song. -1 if song is not in playelist
	 */
	public int getIndex(Song song) {
		return songs.indexOf(song);
	}

	/**
	 * Check whether playlist contains song.
	 *
	 * @param song song
	 * @return if this playlist contains this song
	 */
	public boolean contains(Song song) {
		return songs.contains(song);
	}

	/**
	 * Returns list of Songs in Playlist
	 *
	 * @return songs array
	 */
	public ArrayList<Song> getSongList() {
		return (ArrayList<Song>) songs.clone();
	}
}
