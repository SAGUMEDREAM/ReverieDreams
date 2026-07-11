package cc.thonly.reverie_dreams.sound;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.ReverieDreamsRegistries;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unchecked")
public class RDSoundEvents {
    public static final List<Holder<SoundEvent>> SOUND_EVENTS = new LinkedList<>();
    public static final RegistrySupplier<SoundEvent> EMPTY = register("empty");
    public static final RegistrySupplier<SoundEvent> POINT = register("point");
    public static final RegistrySupplier<SoundEvent> BIU = register("biu");
    public static final RegistrySupplier<SoundEvent> SPELL_CARD = register("spell_card");
    public static final RegistrySupplier<SoundEvent> UP = register("up");
    public static final RegistrySupplier<SoundEvent> FIRE = register("fire");
    public static final RegistrySupplier<SoundEvent> BAGUA = register("bagua");
    public static final RegistrySupplier<SoundEvent> PHOTO = register("photo");
    public static final RegistrySupplier<SoundEvent> TICK_WAVE = register("tick_wave");
    public static final RegistrySupplier<SoundEvent> GRAZE = register("graze");
    public static final RegistrySupplier<SoundEvent> FUMO_0 = register("fumo/0");
    public static final RegistrySupplier<SoundEvent> FUMO_1 = register("fumo/1");
    public static final RegistrySupplier<SoundEvent> FUMO_2 = register("fumo/2");
    public static final RegistrySupplier<SoundEvent> FUMO_3 = register("fumo/3");
    public static Holder<SoundEvent>[] FUMO_SOUNDS = new Holder[]{};

    public static void initialize() {
        FUMO_SOUNDS = new Holder[]{FUMO_0, FUMO_1, FUMO_2, FUMO_3};
    }

    private static RegistrySupplier<SoundEvent> register(String name) {
        RegistrySupplier<SoundEvent> holder = ReverieDreamsRegistries.SOUND_EVENT.register(name, () -> SoundEvent.createVariableRangeEvent(ReverieDreams.id(name)));
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
