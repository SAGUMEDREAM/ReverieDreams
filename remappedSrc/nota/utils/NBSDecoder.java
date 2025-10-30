package nota.utils;

import nota.model.CustomInstrument;
import nota.model.Layer;
import nota.model.Note;
import nota.model.Song;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Utils for reading Note Block Studio data
 */
public class NBSDecoder {

	/**
	 * Parses a Song from a Note Block Studio project file (.nbs)
	 *
	 * @param songFile .nbs file
	 * @return Song object representing a Note Block Studio project
	 * @see Song
	 */
	public static Song parse(File songFile) {
		try {
			return parse(new FileInputStream(songFile), songFile);
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Parses a Song from an InputStream
	 *
	 * @param inputStream of a Note Block Studio project file (.nbs)
	 * @return Song object from the InputStream
	 * @see Song
	 */
	public static Song parse(InputStream inputStream) {
		return parse(inputStream, null); // Source is unknown -> no file
	}

	/**
	 * Parses a Song from an InputStream and a Note Block Studio project file (.nbs)
	 *
	 * @param inputStream of a .nbs file
	 * @param songFile    representing a .nbs file
	 * @return Song object representing the given .nbs file
	 * @see Song
	 */
	protected static Song parse(InputStream inputStream, File songFile) {
		HashMap<Integer, Layer> layerHashMap = new HashMap<Integer, Layer>();
		byte biggestInstrumentIndex = -1;
		boolean isStereo = false;
		try {
			DataInputStream dataInputStream = new DataInputStream(inputStream);
			short length = readShort(dataInputStream);
			int firstcustominstrument = 10; //Backward compatibility - most of songs with old structure are from 1.12
			int firstcustominstrumentdiff;
			int nbsversion = 0;
			if(length == 0) {
				nbsversion = dataInputStream.readByte();
				firstcustominstrument = dataInputStream.readByte();
				if(nbsversion >= 3) {
					length = readShort(dataInputStream);
				}
			}
			firstcustominstrumentdiff = InstrumentUtils.getCustomInstrumentFirstIndex() - firstcustominstrument;
			short songHeight = readShort(dataInputStream);
			String title = readString(dataInputStream);
			String author = readString(dataInputStream);
			String originalAuthor = readString(dataInputStream); // original author
			String description = readString(dataInputStream);
			float speed = readShort(dataInputStream) / 100f;
			dataInputStream.readBoolean(); // auto-save
			dataInputStream.readByte(); // auto-save duration
			dataInputStream.readByte(); // x/4ths, time signature
			readInt(dataInputStream); // minutes spent on project
			readInt(dataInputStream); // left clicks (why?)
			readInt(dataInputStream); // right clicks (why?)
			readInt(dataInputStream); // blocks added
			readInt(dataInputStream); // blocks removed
			readString(dataInputStream); // .mid/.schematic file name
			if(nbsversion >= 4) {
				dataInputStream.readByte(); // loop on/off
				dataInputStream.readByte(); // max loop count
				readShort(dataInputStream); // loop start tick
			}
			short tick = -1;
			while(true) {
				short jumpTicks = readShort(dataInputStream); // jumps till next tick
				//System.out.println("Jumps to next tick: " + jumpTicks);
				if(jumpTicks == 0) {
					break;
				}
				tick += jumpTicks;
				//System.out.println("Tick: " + tick);
				short layer = -1;
				while(true) {
					short jumpLayers = readShort(dataInputStream); // jumps till next layer
					if(jumpLayers == 0) {
						break;
					}
					layer += jumpLayers;
					//System.out.println("Layer: " + layer);
					byte instrument = dataInputStream.readByte();

					if(firstcustominstrumentdiff > 0 && instrument >= firstcustominstrument) {
						instrument += firstcustominstrumentdiff;
					}

					byte key = dataInputStream.readByte();
					byte velocity = 100;
					int panning = 100;
					short pitch = 0;
					if(nbsversion >= 4) {
						velocity = dataInputStream.readByte(); // note block velocity
						panning = 200 - dataInputStream.readUnsignedByte(); // note panning, 0 is right in nbs format
						pitch = readShort(dataInputStream); // note block pitch
					}

					if(panning != 100) {
						isStereo = true;
					}

					setNote(layer, tick,
							new Note(instrument /* instrument */, key/* note */, velocity, panning, pitch),
							layerHashMap);
				}
			}

			if(nbsversion > 0 && nbsversion < 3) {
				length = tick;
			}

			for(int i = 0; i < songHeight; i++) {
				Layer layer = layerHashMap.get(i);

				String name = readString(dataInputStream);
				if(nbsversion >= 4) {
					dataInputStream.readByte(); // layer lock
				}

				byte volume = dataInputStream.readByte();
				int panning = 100;
				if(nbsversion >= 2) {
					panning = 200 - dataInputStream.readUnsignedByte(); // layer stereo, 0 is right in nbs format
				}

				if(panning != 100) {
					isStereo = true;
				}

				if(layer != null) {
					layer.setName(name);
					layer.setVolume(volume);
					layer.setPanning(panning);
				}
			}
			//count of custom instruments
			byte customAmnt = dataInputStream.readByte();
			CustomInstrument[] customInstrumentsArray = new CustomInstrument[customAmnt];

			for(int index = 0; index < customAmnt; index++) {
				customInstrumentsArray[index] = new CustomInstrument((byte) index,
						readString(dataInputStream), readString(dataInputStream));
				dataInputStream.readByte();//pitch
				dataInputStream.readByte();//key
			}

			if(firstcustominstrumentdiff < 0) {
				ArrayList<CustomInstrument> customInstruments = getVersionCustomInstrumentsForSong(firstcustominstrument);
				customInstruments.addAll(Arrays.asList(customInstrumentsArray));
				customInstrumentsArray = customInstruments.toArray(customInstrumentsArray);
			}
			else {
				firstcustominstrument += firstcustominstrumentdiff;
			}

			return new Song(speed, layerHashMap, songHeight, length, title,
					author, originalAuthor, description, songFile, firstcustominstrument, customInstrumentsArray, isStereo);
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(EOFException e) {
			String file = "";
			if(songFile != null) {
				file = songFile.getName();
			}
			// TODO: logger here
//			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Song is corrupted: " + file);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<CustomInstrument> getVersionCustomInstrumentsForSong(int firstCustomInstrumentIndex) {
		ArrayList<CustomInstrument> instruments = new ArrayList<>();

		if(firstCustomInstrumentIndex == 16) {
			instruments.addAll(getVersionCustomInstruments(0.0114f));
		}

		return instruments;
	}

	public static ArrayList<CustomInstrument> getVersionCustomInstruments(float serverVersion) {
		ArrayList<CustomInstrument> instruments = new ArrayList<>();
		if(serverVersion == 0.0112f) {
			instruments.add(new CustomInstrument((byte) 0, "Guitar", "block.note_block.guitar.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Flute", "block.note_block.flute.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Bell", "block.note_block.bell.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Chime", "block.note_block.icechime.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Xylophone", "block.note_block.xylobone.ogg"));
			return instruments;
		}

		if(serverVersion == 0.0114f) {
			instruments.add(new CustomInstrument((byte) 0, "Iron Xylophone", "block.note_block.iron_xylophone.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Cow Bell", "block.note_block.cow_bell.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Didgeridoo", "block.note_block.didgeridoo.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Bit", "block.note_block.bit.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Banjo", "block.note_block.banjo.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Pling", "block.note_block.pling.ogg"));
			return instruments;
		}
		return instruments;
	}

	/**
	 * Sets a note at a tick in a song
	 *
	 * @param layerIndex
	 * @param ticks
	 * @param note
	 * @param layerHashMap
	 */
	private static void setNote(int layerIndex, int ticks, Note note, HashMap<Integer, Layer> layerHashMap) {
		Layer layer = layerHashMap.get(layerIndex);
		if(layer == null) {
			layer = new Layer();
			layerHashMap.put(layerIndex, layer);
		}
		layer.setNote(ticks, note);
	}

	private static short readShort(DataInputStream dataInputStream) throws IOException {
		int byte1 = dataInputStream.readUnsignedByte();
		int byte2 = dataInputStream.readUnsignedByte();
		return (short) (byte1 + (byte2 << 8));
	}

	private static int readInt(DataInputStream dataInputStream) throws IOException {
		int byte1 = dataInputStream.readUnsignedByte();
		int byte2 = dataInputStream.readUnsignedByte();
		int byte3 = dataInputStream.readUnsignedByte();
		int byte4 = dataInputStream.readUnsignedByte();
		return (byte1 + (byte2 << 8) + (byte3 << 16) + (byte4 << 24));
	}

	private static String readString(DataInputStream dataInputStream) throws IOException {
		int length = readInt(dataInputStream);
		StringBuilder builder = new StringBuilder(length);
		for(; length > 0; --length) {
			char c = (char) dataInputStream.readByte();
			if(c == (char) 0x0D) {
				c = ' ';
			}
			builder.append(c);
		}
		return builder.toString();
	}

}
