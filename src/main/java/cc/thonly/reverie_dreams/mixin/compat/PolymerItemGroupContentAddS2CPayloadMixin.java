package cc.thonly.reverie_dreams.mixin.compat;

import eu.pb4.polymer.core.impl.networking.payloads.s2c.PolymerItemGroupContentAddS2CPayload;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;

@Mixin(value = PolymerItemGroupContentAddS2CPayload.class,remap = false)
public abstract class PolymerItemGroupContentAddS2CPayloadMixin {
    /**
     * @author
     * @reason
     */
    @Overwrite

    public static PolymerItemGroupContentAddS2CPayload read(ContextByteBuf buf) {
        try {
            return new PolymerItemGroupContentAddS2CPayload(buf.readIdentifier(),
                    ItemStack.OPTIONAL_LIST_PACKET_CODEC.decode(buf),
                    ItemStack.OPTIONAL_LIST_PACKET_CODEC.decode(buf)
            );
        } catch (Exception err) {
            err.printStackTrace();
            return new PolymerItemGroupContentAddS2CPayload(buf.readIdentifier(),new ArrayList<>(),new ArrayList<>());
        }
    }
}
