package cc.thonly.reverie_dreams.entity.base;

import cc.thonly.reverie_dreams.networking.FakePlayerNetworkHandler;
import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;

public class FakePlayer extends ServerPlayer {
    public static final UUID DEFAULT_UUID = UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77");
    private static final GameProfile DEFAULT_PROFILE;
    private static final Map<FakePlayerKey, FakePlayer> FAKE_PLAYER_MAP;

    public static FakePlayer get(ServerLevel world) {
        return get(world, DEFAULT_PROFILE);
    }

    public static FakePlayer get(ServerLevel world, GameProfile profile) {
        Objects.requireNonNull(world, "World may not be null.");
        Objects.requireNonNull(profile, "Game profile may not be null.");
        return FAKE_PLAYER_MAP.computeIfAbsent(new FakePlayerKey(world, profile), (key) -> new FakePlayer(key.world, key.profile));
    }

    protected FakePlayer(ServerLevel world, GameProfile profile) {
        super(world.getServer(), world, profile, ClientInformation.createDefault());
        this.connection = new FakePlayerNetworkHandler(this);
    }

    public void tick() {
    }

    public void updateOptions(ClientInformation settings) {
    }

    public void awardStat(Stat<?> stat, int amount) {
    }

    public void resetStat(Stat<?> stat) {
    }

    public boolean isInvulnerableTo(ServerLevel world, DamageSource damageSource) {
        return true;
    }

    public @Nullable PlayerTeam getTeam() {
        return null;
    }

    public void startSleeping(BlockPos pos) {
    }

    public boolean startRiding(Entity entity, boolean force, boolean emitEvent) {
        return false;
    }

    public void openTextEdit(SignBlockEntity sign, boolean front) {
    }

    public OptionalInt openMenu(@Nullable MenuProvider factory) {
        return OptionalInt.empty();
    }

    public void openHorseInventory(AbstractHorse horse, Container inventory) {
    }

    static {
        DEFAULT_PROFILE = new GameProfile(DEFAULT_UUID, "[Minecraft]");
        FAKE_PLAYER_MAP = (new MapMaker()).weakValues().makeMap();
    }

    private static record FakePlayerKey(ServerLevel world, GameProfile profile) {
    }
}
