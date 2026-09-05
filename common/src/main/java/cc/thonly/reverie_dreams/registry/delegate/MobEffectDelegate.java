package cc.thonly.reverie_dreams.registry.delegate;

import cc.thonly.reverie_dreams.registry.DeferredDelegateRegister;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;

import java.util.Objects;

public class MobEffectDelegate extends RegistryDelegate<MobEffect> {
    private Identifier key;

    private MobEffectDelegate(Holder<MobEffect> holder) {
        super(holder);
        if (holder instanceof DeferredDelegateRegister.Entry<MobEffect> entry) {
            this.key = entry.getRegistryId();
        }
    }

    @Override
    public void bindKey(Identifier key) {
        this.key = key;
    }

    @Override
    public Identifier getRegistryId() {
        return this.key;
    }

    public static MobEffectDelegate of(
            RegistryDelegate<MobEffect> delegate
    ) {
        return new MobEffectDelegate(delegate);
    }

    public static MobEffectDelegate of(
            Holder<MobEffect> holder
    ) {
        return new MobEffectDelegate(holder);
    }

    /**
     * 获取当前已经绑定的 Holder。
     */
    public Holder<MobEffect> builtInHolder() {
        return this;
    }

    @Override
    public void bind(Holder<MobEffect> holder) {
        Objects.requireNonNull(holder, "holder");

        if (this.holder != null) {
            throw new IllegalStateException(
                    "Mob effect delegate is already bound"
            );
        }

        this.holder = holder;
    }
}