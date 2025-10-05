package cc.thonly.reverie_dreams.server.player;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
public class NameComponent implements PlayerComponent<NameComponent> {
    public static final Codec<NameComponent> CODEC = RecordCodecBuilder.create(x -> x.group(
            TextCodecs.CODEC.optionalFieldOf("Name", Text.literal("Player")).forGetter(NameComponent::getName),
            Codec.STRING.optionalFieldOf("RealName", "Player").forGetter(NameComponent::getPlayerName)
    ).apply(x, NameComponent::new));
    private Text name;
    @Nullable
    private String playerName;
    private ServerPlayerEntity player;

    public NameComponent(Text name) {
        this.name = name;
    }

    public NameComponent(Text name, @Nullable String playerName) {
        this(name);
        this.playerName = playerName;
    }

    @Override
    public void onLoad() {
        this.name = this.player.getName();
        GameProfile gameProfile = this.player.getGameProfile();
        this.playerName = gameProfile.getName();
    }

    @Override
    public void setPlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public Codec<NameComponent> getCodec() {
        return CODEC;
    }

    public static class NameComponentInitializer implements PlayerComponentInitializer<NameComponent> {

        @Override
        public PlayerComponent<NameComponent> create(ServerPlayerEntity player) {
            return new NameComponent(Text.literal("Player"));
        }

        @Override
        public Codec<NameComponent> getCodec() {
            return CODEC;
        }
    }
}
