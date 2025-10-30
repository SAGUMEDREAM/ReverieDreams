package nota.model;

import nota.Nota;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * Version independent Spigot sounds.
 * <p>
 * Enum mapping to note names for different
 * Minecraft versions.
 *
 * @author NiklasEi
 * @see <a href="https://gist.github.com/NiklasEi/7bd0ffd136f8459df0940e4501d47a8a">https://gist.github.com/NiklasEi/7bd0ffd136f8459df0940e4501d47a8a</a>
 */
public enum Sound {

	NOTE_PIANO("NOTE_PIANO", "BLOCK_NOTE_HARP", "BLOCK_NOTE_BLOCK_HARP"),
	NOTE_BASS("NOTE_BASS", "BLOCK_NOTE_BASS", "BLOCK_NOTE_BLOCK_BASS"),
	NOTE_BASS_DRUM("NOTE_BASS_DRUM", "BLOCK_NOTE_BASEDRUM", "BLOCK_NOTE_BLOCK_BASEDRUM"),
	NOTE_SNARE_DRUM("NOTE_SNARE_DRUM", "BLOCK_NOTE_SNARE", "BLOCK_NOTE_BLOCK_SNARE"),
	NOTE_STICKS("NOTE_STICKS", "BLOCK_NOTE_HAT", "BLOCK_NOTE_BLOCK_HAT"),
	NOTE_BASS_GUITAR("NOTE_BASS_GUITAR", "BLOCK_NOTE_GUITAR", "BLOCK_NOTE_BLOCK_GUITAR"),
	NOTE_FLUTE("NOTE_FLUTE", "BLOCK_NOTE_FLUTE", "BLOCK_NOTE_BLOCK_FLUTE"),
	NOTE_BELL("NOTE_BELL", "BLOCK_NOTE_BELL", "BLOCK_NOTE_BLOCK_BELL"),
	NOTE_CHIME("NOTE_CHIME", "BLOCK_NOTE_CHIME", "BLOCK_NOTE_BLOCK_CHIME"),
	NOTE_XYLOPHONE("NOTE_XYLOPHONE", "BLOCK_NOTE_XYLOPHONE", "BLOCK_NOTE_BLOCK_XYLOPHONE"),
	NOTE_PLING("NOTE_PLING", "BLOCK_NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING"),
	NOTE_IRON_XYLOPHONE("BLOCK_NOTE_BLOCK_IRON_XYLOPHONE"),
	NOTE_COW_BELL("BLOCK_NOTE_BLOCK_COW_BELL"),
	NOTE_DIDGERIDOO("BLOCK_NOTE_BLOCK_DIDGERIDOO"),
	NOTE_BIT("BLOCK_NOTE_BLOCK_BIT"),
	NOTE_BANJO("BLOCK_NOTE_BLOCK_BANJO");

	String[] versionDependentNames;
	SoundEvent cached = null;
	static Map<String, SoundEvent> cachedSoundMap = new HashMap<>();

	Sound(String... versionDependentNames) {
		this.versionDependentNames = versionDependentNames;
	}

	/**
	 * Attempts to retrieve the org.bukkit.Sound equivalent of a version dependent enum name
	 *
	 * @param bukkitSoundName
	 * @return org.bukkit.Sound enum
	 */
	public static SoundEvent getFromBukkitName(String bukkitSoundName) {
		return NOTE_BANJO.getSound(bukkitSoundName);
	}

	private SoundEvent getSound() {
		if(cached != null) return cached;
		for(String name : versionDependentNames) {
			try {
				return cached = getSound(name);
			}
			catch(IllegalArgumentException ignore2) {
				// try next
			}
		}
		return null;
	}

	private SoundEvent getSound(String name) {
		switch(name) {
			case "NOTE_PIANO", "BLOCK_NOTE_HARP", "BLOCK_NOTE_BLOCK_HARP" -> {
				return bullshit(SoundEvents.NOTE_BLOCK_HARP);
			}
			case "NOTE_BASS", "BLOCK_NOTE_BASS", "BLOCK_NOTE_BLOCK_BASS" -> {
				return bullshit(SoundEvents.NOTE_BLOCK_BASS);
			}
			case "NOTE_BASS_DRUM", "BLOCK_NOTE_BASEDRUM", "BLOCK_NOTE_BLOCK_BASEDRUM" -> {
				return bullshit(SoundEvents.NOTE_BLOCK_BASEDRUM);
			}
			case "NOTE_SNARE_DRUM", "BLOCK_NOTE_SNARE", "BLOCK_NOTE_BLOCK_SNARE" -> {
				return bullshit(SoundEvents.NOTE_BLOCK_SNARE);
			}
			case "NOTE_STICKS", "BLOCK_NOTE_HAT", "BLOCK_NOTE_BLOCK_HAT" -> {
				return bullshit(SoundEvents.NOTE_BLOCK_HAT);
			}
			case "NOTE_BASS_GUITAR", "BLOCK_NOTE_GUITAR", "BLOCK_NOTE_BLOCK_GUITAR" -> {
				return bullshit(SoundEvents.NOTE_BLOCK_GUITAR);
			}
			case "NOTE_FLUTE", "BLOCK_NOTE_FLUTE", "BLOCK_NOTE_BLOCK_FLUTE" -> {
				return bullshit(SoundEvents.NOTE_BLOCK_FLUTE);
			}
			case "NOTE_BELL", "BLOCK_NOTE_BELL", "BLOCK_NOTE_BLOCK_BELL" -> {
				return bullshit(SoundEvents.NOTE_BLOCK_BELL);
			}
			case "NOTE_XYLOPHONE", "BLOCK_NOTE_XYLOPHONE", "BLOCK_NOTE_BLOCK_XYLOPHONE" -> {
				return bullshit(SoundEvents.NOTE_BLOCK_XYLOPHONE);
			}
			case "NOTE_PLING", "BLOCK_NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING" -> {
				return bullshit(SoundEvents.NOTE_BLOCK_PLING);
			}
			case "BLOCK_NOTE_BLOCK_IRON_XYLOPHONE" -> {
				return bullshit(SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE);
			}
			case "BLOCK_NOTE_BLOCK_COW_BELL" -> {
				return bullshit(SoundEvents.NOTE_BLOCK_COW_BELL);
			}
			case "BLOCK_NOTE_BLOCK_DIDGERIDOO" -> {
				return bullshit(SoundEvents.NOTE_BLOCK_DIDGERIDOO);
			}
			case "BLOCK_NOTE_BLOCK_BIT" -> {
				return bullshit(SoundEvents.NOTE_BLOCK_BIT);
			}
			case "BLOCK_NOTE_BLOCK_BANJO" -> {
				return bullshit(SoundEvents.NOTE_BLOCK_BANJO);
			}
		}
		return bullshit(SoundEvents.NOTE_BLOCK_BASS);
	}

	private SoundEvent bullshit(Holder.Reference<SoundEvent> reference) {
		return Nota.getAPI().getServer().registryAccess().lookupOrThrow(Registries.SOUND_EVENT).getValue(reference.key());
	}

	/**
	 * Get the bukkit sound for current server version
	 * <p>
	 * Caches sound on first call
	 *
	 * @return corresponding {@link SoundEvent}
	 */
	public SoundEvent bukkitSound() {
		if(getSound() != null) {
			return getSound();
		}
		throw new IllegalArgumentException("Found no valid sound name for " + this.name());
	}

	static {
		// Cache sound access.
		for(Sound sound : values())
			for(String soundName : sound.versionDependentNames)
				cachedSoundMap.put(soundName.toUpperCase(), sound.getSound());
	}
}
