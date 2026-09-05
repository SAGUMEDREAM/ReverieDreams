package cc.thonly.reverie_dreams.registry.delegate;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.Objects;

public class SoundDelegate extends RegistryDelegate<SoundEvent> {

    private Identifier key;

    private SoundDelegate(RegistryDelegate<SoundEvent> delegate) {
        super(delegate);
        this.key = delegate.getRegistryId();
    }

    @Override
    public void bindKey(Identifier key) {
        this.key = key;
    }

    public static SoundDelegate of(
            RegistryDelegate<SoundEvent> delegate
    ) {
        return new SoundDelegate(delegate);
    }

    public SoundEvent asSoundEvent() {
        return this.get();
    }

    @Override
    public Identifier getRegistryId() {
        return this.key;
    }

    @Override
    public void bind(Holder<SoundEvent> holder) {
        Objects.requireNonNull(holder, "holder");

        if (this.holder != null) {
            throw new IllegalStateException(
                    "Sound delegate is already bound"
            );
        }

        this.holder = holder;
    }
}