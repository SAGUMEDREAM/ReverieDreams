package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.TextDisplay.class)
public interface TextDisplayAccessor {
    @Invoker("setText")
    void reverie_dreams$setText(Component text);

    @Invoker("setLineWidth")
    void reverie_dreams$setLineWidth(int width);

    @Invoker("setFlags")
    void reverie_dreams$setFlags(byte flags);

    @Invoker("setTextOpacity")
    void reverie_dreams$setTextOpacity(byte opacity);

    @Invoker("setBackgroundColor")
    void reverie_dreams$setBackgroundColor(int color);
}
