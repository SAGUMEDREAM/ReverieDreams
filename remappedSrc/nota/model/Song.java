package nota.model;

import nota.utils.NBSDecoder;

import java.io.File;
import java.util.HashMap;

/**
 * Represents a Note Block Studio project
 * Can be parsed from .nbs file with {@link NBSDecoder}
 */
public class Song implements Cloneable {
	HashMap<Integer, Layer> layerHashMap;
	short songHeight;
	short length;
	String title;
	File path;
	String author;
	String originalAuthor;
	String description;
	float speed;
	float delay;
	CustomInstrument[] customInstruments;

	int firstCustomInstrumentIndex;
	boolean isStereo;

	/**
	 * Create Song instance by copying other Song parameters
	 *
	 * @param other song
	 */
	public Song(Song other) {
		this(other.getSpeed(), other.getLayerHashMap(), other.getSongHeight(),
				other.getLength(), other.getTitle(), other.getAuthor(), other.getOriginalAuthor(),
				other.getDescription(), other.getPath(), other.getFirstCustomInstrumentIndex(), other.getCustomInstruments(), other.isStereo);
	}

	public Song(float speed, HashMap<Integer, Layer> layerHashMap,
				short songHeight, final short length, String title, String author, String originalAuthor,
				String description, File path, int firstCustomInstrumentIndex, boolean isStereo) {
		this(speed, layerHashMap, songHeight, length, title, author, originalAuthor, description, path, firstCustomInstrumentIndex, new CustomInstrument[0], isStereo);
	}

	public Song(float speed, HashMap<Integer, Layer> layerHashMap,
				short songHeight, final short length, String title, String author, String originalAuthor,
				String description, File path, int firstCustomInstrumentIndex, CustomInstrument[] customInstruments, boolean isStereo) {
		this.speed = speed;
		delay = 20 / speed;
		this.layerHashMap = layerHashMap;
		this.songHeight = songHeight;
		this.length = length;
		this.title = title;
		this.author = author;
		this.originalAuthor = originalAuthor;
		this.description = description;
		this.path = path;
		this.firstCustomInstrumentIndex = firstCustomInstrumentIndex;
		this.customInstruments = customInstruments;
		this.isStereo = isStereo;
	}

	/**
	 * Gets all Layers in this Song and their index
	 *
	 * @return HashMap of Layers and their index
	 */
	public HashMap<Integer, Layer> getLayerHashMap() {
		return layerHashMap;
	}

	/**
	 * Gets the Song's height
	 *
	 * @return Song height
	 */
	public short getSongHeight() {
		return songHeight;
	}

	/**
	 * Gets the length in ticks of this Song
	 *
	 * @return length of this Song
	 */
	public short getLength() {
		return length;
	}

	/**
	 * Gets the title / name of this Song
	 *
	 * @return title of the Song
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Gets the author of the Song
	 *
	 * @return author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Gets the original author of the Song
	 *
	 * @return author
	 */
	public String getOriginalAuthor() {
		return originalAuthor;
	}

	/**
	 * Returns the File from which this Song is sourced
	 *
	 * @return file of this Song
	 */
	public File getPath() {
		return path;
	}

	/**
	 * Gets the description of this Song
	 *
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the speed (ticks per second) of this Song
	 *
	 * @return speed
	 */
	public float getSpeed() {
		return speed;
	}

	/**
	 * Gets the delay of this Song
	 *
	 * @return delay
	 */
	public float getDelay() {
		return delay;
	}

	/**
	 * Gets the CustomInstruments made for this Song
	 *
	 * @return array of CustomInstruments
	 * @see CustomInstrument
	 */
	public CustomInstrument[] getCustomInstruments() {
		return customInstruments;
	}

	@Override
	public Song clone() {
		return new Song(this);
	}

	public int getFirstCustomInstrumentIndex() {
		return firstCustomInstrumentIndex;
	}

	/**
	 * Returns true if song has at least one stereo {@link Note} or {@link Layer} in nbs file
	 *
	 * @return boolean
	 */
	public boolean isStereo() {
		return isStereo;
	}
}
