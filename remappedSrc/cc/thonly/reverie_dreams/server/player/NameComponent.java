package cc.thonly.reverie_dreams.server.player;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
public class NameComponent implements PlayerComponent<NameComponent> {
    public static final Codec<NameComponent> CODEC = RecordCodecBuilder.create(x -> x.group(
            ComponentSerialization.CODEC.optionalFieldOf("Name", Component.literal("Player")).forGetter(NameComponent::getName),
            Codec.STRING.optionalFieldOf("RealName", "Player").forGetter(NameComponent::getPlayerName)
    ).apply(x, NameComponent::new));
    private Component name;
    @Nullable
    private String playerName;
    private ServerPlayer player;

    public NameComponent(Component name) {
        this.name = name;
    }

    public NameComponent(Component name, @Nullable String playerName) {
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
    public void setPlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public Codec<NameComponent> getCodec() {
        return CODEC;
    }

    public static class NameComponentInitializer implements PlayerComponentInitializer<NameComponent> {

        @Override
        public PlayerComponent<NameComponent> create(ServerPlayer player) {
            return new NameComponent(Component.literal("Player"));
        }

        @Override
        public Codec<NameComponent> getCodec() {
            return CODEC;
        }
    }
}
