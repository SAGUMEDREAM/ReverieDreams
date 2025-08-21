package cc.thonly.reverie_dreams.sound;

import cc.thonly.reverie_dreams.Touhou;
import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class SoundEventInit {
    public static final List<SoundEvent> SOUND_EVENTS = new LinkedList<>();
    public static final SoundEvent POINT = registerSound("point");
    public static final SoundEvent BIU = registerSound("biu");
    public static final SoundEvent SPELL_CARD = registerSound("spell_card");
    public static final SoundEvent UP = registerSound("up");
    public static final SoundEvent FIRE = registerSound("fire");
    public static final SoundEvent BAGUA = registerSound("bagua");
    public static final SoundEvent PHOTO = registerSound("photo");
    public static final SoundEvent TICK_WAVE = registerSound("tick_wave");
    public static final SoundEvent FUMO_0 = registerSound("fumo/0");
    public static final SoundEvent FUMO_1 = registerSound("fumo/1");
    public static final SoundEvent FUMO_2 = registerSound("fumo/2");
    public static final SoundEvent FUMO_3 = registerSound("fumo/3");

    public static final SoundEvent[] FUMO_SOUNDS = {FUMO_0,FUMO_1,FUMO_2,FUMO_3};

    public static void init() {
    }

    public static SoundEvent randomFumo() {
        Random random = new Random();
        int index = random.nextInt(FUMO_SOUNDS.length);
        return FUMO_SOUNDS[index];
    }

    public static SoundEvent registerSound(String id) {
        Identifier identifier = Touhou.id(id);
        SoundEvent soundEvent = Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
//        SoundEvent soundEvent = SoundEvent.of(identifier);
        PolymerSoundEvent.registerOverlay(soundEvent);
        SOUND_EVENTS.add(soundEvent);
        return soundEvent;
    }

    protected static RegistryEntry.Reference<SoundEvent> registerReference(String id) {
        return registerReference(Touhou.id(id));
    }

    protected static RegistryEntry.Reference<SoundEvent> registerReference(Identifier id) {
        return registerReference(id, id);
    }

    protected static RegistryEntry.Reference<SoundEvent> registerReference(Identifier id, Identifier soundId) {
        SoundEvent soundEvent = SoundEvent.of(soundId);
        RegistryEntry.Reference<SoundEvent> soundEventReference = Registry.registerReference(Registries.SOUND_EVENT, id, soundEvent);
        RegistrySyncUtils.setServerEntry(Registries.SOUND_EVENT, soundEvent);
        return soundEventReference;
    }

    public static void playSound(Entity entity, SoundEvent sound, float volume, float pitch) {
        entity.playSound(sound, volume, pitch);
    }
}
