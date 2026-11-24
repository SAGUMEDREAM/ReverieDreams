package cc.thonly.reverie_dreams.client.audio.radio;

import net.minecraft.client.resources.sounds.*;
import net.minecraft.client.sounds.*;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.*;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;

import java.util.function.DoubleSupplier;

final class RadioSound extends AbstractTickableSoundInstance {
    private final Identifier audio;
    private final DoubleSupplier gain;

    RadioSound(Identifier audio, DoubleSupplier gain) {
        super(SoundEvent.createVariableRangeEvent(audio), SoundSource.RECORDS, RandomSource.create());
        this.audio = audio;
        this.gain = gain;
        volume = (float) gain.getAsDouble();
        relative = true;
        attenuation = Attenuation.NONE;
        looping = false;
    }

    @Override
    public WeighedSoundEvents resolve(SoundManager manager) {
        // Synthetic sound leaf: only our exact path is supplied by RadioSoundBufferMixin.
        sound = new Sound(audio, ConstantFloat.of(1), ConstantFloat.of(1), 1, Sound.Type.FILE, true, false, 16);
        var event = new WeighedSoundEvents(audio, null);
        event.addSound(sound);
        return event;
    }

    @Override
    public void tick() {
        volume = (float) gain.getAsDouble();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
