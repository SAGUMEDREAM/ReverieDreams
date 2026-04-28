package cc.thonly.reverie_dreams.sound;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.blay09.mods.balm.core.BalmRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unchecked")
public class RDSoundEvents {
    public static final List<Holder<SoundEvent>> SOUND_EVENTS = new LinkedList<>();
    public static Holder<SoundEvent> EMPTY;
    public static Holder<SoundEvent> POINT;
    public static Holder<SoundEvent> BIU;
    public static Holder<SoundEvent> SPELL_CARD;
    public static Holder<SoundEvent> UP;
    public static Holder<SoundEvent> FIRE;
    public static Holder<SoundEvent> BAGUA;
    public static Holder<SoundEvent> PHOTO;
    public static Holder<SoundEvent> TICK_WAVE;
    public static Holder<SoundEvent> GRAZE;
    public static Holder<SoundEvent> FUMO_0;
    public static Holder<SoundEvent> FUMO_1;
    public static Holder<SoundEvent> FUMO_2;
    public static Holder<SoundEvent> FUMO_3;

    public static Holder<SoundEvent>[] FUMO_SOUNDS = new Holder[]{};

    public static void initialize(BalmRegistrar.Scoped<SoundEvent> registry) {
        EMPTY = register(registry, "empty");
        POINT = register(registry, "point");
        BIU = register(registry, "biu");
        SPELL_CARD = register(registry, "spell_card");
        UP = register(registry, "up");
        FIRE = register(registry, "fire");
        BAGUA = register(registry, "bagua");
        PHOTO = register(registry, "photo");
        TICK_WAVE = register(registry, "tick_wave");
        GRAZE = register(registry, "graze");
        FUMO_0 = register(registry, "fumo/0");
        FUMO_1 = register(registry, "fumo/1");
        FUMO_2 = register(registry, "fumo/2");
        FUMO_3 = register(registry, "fumo/3");
        FUMO_SOUNDS = new Holder[]{FUMO_0, FUMO_1, FUMO_2, FUMO_3};
    }

    private static Holder<SoundEvent> register(BalmRegistrar.Scoped<SoundEvent> registry, String name) {
        Holder<SoundEvent> holder =
                registry.register(name, SoundEvent::createVariableRangeEvent);

        SOUND_EVENTS.add(holder);
        return holder;
    }

    public static Holder<SoundEvent> getRandomFumoSound() {
        return FUMO_SOUNDS[ReverieDreams.RD.nextInt(FUMO_SOUNDS.length - 1)];
    }

    public static void playSound(Entity entity, SoundEvent sound, float volume, float pitch) {
        entity.playSound(sound, volume, pitch);
    }
}
