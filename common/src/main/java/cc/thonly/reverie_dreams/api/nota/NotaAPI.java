package cc.thonly.reverie_dreams.api.nota;

import cc.thonly.reverie_dreams.server.nota.player.SongPlayer;
import lombok.Setter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main class, contains methods for playing and adjusting songs for players.
 */
@SuppressWarnings({"unused", "LombokGetterMayBeUsed"})
public class NotaAPI {
	public static final String MOD_ID = "nota";
	public static final Logger LOGGER = LoggerFactory.getLogger("Nota");

	private static NotaAPI instance;
	@Setter
	private MinecraftServer server;

	Map<UUID, ArrayList<SongPlayer>> playingSongs = new ConcurrentHashMap<>();
	Map<UUID, Byte> playerVolume = new ConcurrentHashMap<>();

	@Setter
	boolean disabling = false;

	/**
	 * Returns true if a Player is currently receiving a song
	 *
	 * @param player entity entity
	 * @return is receiving a song
	 */
	public static boolean isReceivingSong(Player player) {
		return isReceivingSong(player.getUUID());
	}

	/**
	 * Returns true if a Player with specified UUID is currently receiving a song
	 *
	 * @param playerUuid entity's uuid
	 * @return is receiving a song
	 */
	public static boolean isReceivingSong(UUID playerUuid) {
		ArrayList<SongPlayer> songs = instance.playingSongs.get(playerUuid);
		return (songs != null && !songs.isEmpty());
	}

	/**
	 * Stops the song for a Player
	 *
	 * @param player entity entity
	 */
	public static void stopPlaying(Player player) {
		stopPlaying(player.getUUID());
	}

	/**
	 * Stops the song for a Player
	 *
	 * @param playerUuid entity's uuid
	 */
	public static void stopPlaying(UUID playerUuid) {
		ArrayList<SongPlayer> songs = instance.playingSongs.get(playerUuid);
		if(songs == null) {
			return;
		}
		for(SongPlayer songPlayer : songs) {
			songPlayer.removePlayer(playerUuid);
		}
	}

	/**
	 * Sets the volume for a given Player
	 *
	 * @param player entity entity
	 * @param volume volume
	 */
	public static void setPlayerVolume(Player player, byte volume) {
		setPlayerVolume(player.getUUID(), volume);
	}

	/**
	 * Sets the volume for a given Player
	 *
	 * @param playerUuid entity's uuid
	 * @param volume volume
	 */
	public static void setPlayerVolume(UUID playerUuid, byte volume) {
		instance.playerVolume.put(playerUuid, volume);
	}

	/**
	 * Gets the volume for a given Player
	 *
	 * @param player entity entity
	 * @return volume (byte)
	 */
	public static byte getPlayerVolume(Player player) {
		return getPlayerVolume(player.getUUID());
	}

	/**
	 * Gets the volume for a given Player
	 *
	 * @param playerUuid entity's uuid
	 * @return volume (byte)
	 */
	public static byte getPlayerVolume(UUID playerUuid) {
		if(instance.playerVolume.containsKey(playerUuid)) {
			return instance.playerVolume.get(playerUuid);
		}
		else {
			instance.playerVolume.put(playerUuid, (byte) 100);
			return 100;
		}
	}

	public static ArrayList<SongPlayer> getSongPlayersByPlayer(Player player) {
		return getSongPlayersByPlayer(player.getUUID());
	}

	public static ArrayList<SongPlayer> getSongPlayersByPlayer(UUID playerUuid) {
		return instance.playingSongs.get(playerUuid);
	}

	public static void setSongPlayersByPlayer(Player player, ArrayList<SongPlayer> songs) {
		setSongPlayersByPlayer(player.getUUID(), songs);
	}

	public static void setSongPlayersByPlayer(UUID playerUuid, ArrayList<SongPlayer> songs) {
		instance.playingSongs.put(playerUuid, songs);
	}

	public boolean isDisabling() {
		return this.disabling;
	}

	public static void setInstance(NotaAPI instance) {
		NotaAPI.instance = instance;
	}

	public static NotaAPI getAPI() {
		return NotaAPI.instance;
	}

	public MinecraftServer getServer() {
		return this.server;
	}


}
