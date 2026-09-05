package cc.thonly.reverie_dreams.sound;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import cc.thonly.reverie_dreams.registry.delegate.RegistryDelegate;
import cc.thonly.reverie_dreams.registry.delegate.SoundDelegate;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unchecked")
public class RDSoundEvents {
    public static final List<Holder<SoundEvent>> SOUND_EVENTS = new LinkedList<>();
    public static final SoundDelegate EMPTY = register("empty");
    public static final SoundDelegate POINT = register("point");
    public static final SoundDelegate BIU = register("biu");
    public static final SoundDelegate SPELL_CARD = register("spell_card");
    public static final SoundDelegate UP = register("up");
    public static final SoundDelegate FIRE = register("fire");
    public static final SoundDelegate BAGUA = register("bagua");
    public static final SoundDelegate PHOTO = register("photo");
    public static final SoundDelegate TICK_WAVE = register("tick_wave");
    public static final SoundDelegate GRAZE = register("graze");
    public static final SoundDelegate FUMO_0 = register("fumo/0");
    public static final SoundDelegate FUMO_1 = register("fumo/1");
    public static final SoundDelegate FUMO_2 = register("fumo/2");
    public static final SoundDelegate FUMO_3 = register("fumo/3");
    public static Holder<SoundEvent>[] FUMO_SOUNDS = new Holder[]{};

    public static void initialize() {
        FUMO_SOUNDS = new Holder[]{FUMO_0, FUMO_1, FUMO_2, FUMO_3};
    }

    private static SoundDelegate register(String name) {
        RegistryDelegate<SoundEvent> holder = MCBuiltInRegistries.SOUND_EVENT.register(name, () -> SoundEvent.createVariableRangeEvent(ReverieDreams.id(name)));
        SOUND_EVENTS.add(holder);
        return SoundDelegate.of(holder);
    }

    public static Holder<SoundEvent> getRandomFumoSound() {
        return FUMO_SOUNDS[ReverieDreams.RD.nextInt(FUMO_SOUNDS.length - 1)];
    }

    public static void playSound(Entity entity, SoundEvent sound, float volume, float pitch) {
        entity.playSound(sound, volume, pitch);
    }
}
