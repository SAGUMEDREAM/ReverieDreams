package cc.thonly.reverie_dreams.recipe;

import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

@Setter
@Getter
public abstract class BaseRecipe {
    private Identifier id;
    private Integer rawId;
    private boolean isVirtual;
    public abstract ItemStackTemplateWrapper getOutput();

    public static <T extends BaseRecipe> StreamCodec<RegistryFriendlyByteBuf, T> forStreamCodec(Codec<T> codec) {
        return StreamCodec.of(
                // encode
                (buf, value) -> {
                    var tag = codec.encodeStart(buf.registryAccess().createSerializationContext(NbtOps.INSTANCE), value)
                            .result()
                            .orElseThrow(() -> new RuntimeException("Encode failed"));

                    buf.writeNbt(tag);
                },

                // decode
                (buf) -> {
                    CompoundTag tag = buf.readNbt();

                    return codec.parse(buf.registryAccess().createSerializationContext(NbtOps.INSTANCE), tag)
                            .result()
                            .orElseThrow(() -> new RuntimeException("Decode failed"));
                }
        );
    }
}
