package cc.thonly.reverie_dreams.sound;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class SoundEventInit {
    public static final List<SoundEvent> SOUND_EVENTS = new LinkedList<>();
    public static final Holder.Reference<SoundEvent> EMPTY = registerReference("empty");
    public static final SoundEvent POINT = registerSound("point");
    public static final SoundEvent BIU = registerSound("biu");
    public static final SoundEvent SPELL_CARD = registerSound("spell_card");
    public static final SoundEvent UP = registerSound("up");
    public static final SoundEvent FIRE = registerSound("fire");
    public static final SoundEvent BAGUA = registerSound("bagua");
    public static final SoundEvent PHOTO = registerSound("photo");
    public static final SoundEvent TICK_WAVE = registerSound("tick_wave");
    public static final SoundEvent GRAZE = registerSound("graze");
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
        Identifier identifier = ReverieDreams.id(id);
        SoundEvent soundEvent = Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, SoundEvent.createVariableRangeEvent(identifier));
        SOUND_EVENTS.add(soundEvent);
        return soundEvent;
    }

    protected static Holder.Reference<SoundEvent> registerReference(String id) {
        return registerReference(ReverieDreams.id(id));
    }

    protected static Holder.Reference<SoundEvent> registerReference(Identifier id) {
        return registerReference(id, id);
    }

    protected static Holder.Reference<SoundEvent> registerReference(Identifier id, Identifier soundId) {
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(soundId);
        Holder.Reference<SoundEvent> ref = Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id, soundEvent);
        SOUND_EVENTS.add(ref.value());
        return ref;
    }

    public static void playSound(Entity entity, SoundEvent sound, float volume, float pitch) {
        entity.playSound(sound, volume, pitch);
    }
}
