package cc.thonly.reverie_dreams.data.danmaku.trajectory;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum PlayerTrajectoryType {
    RAMP_SHOT("ramp_shot", "渐速箭", "Accelerating Shot"),
    LOB_SHOT("lob_shot", "抛弧弹", "Lob Shot"),
    BEAT_BURST("beat_burst", "三拍连珠", "Triple Beat"),
    RANGE_CONVERGENCE("range_convergence", "定距会射", "Converging Shot"),
    WEAVE_SHOT("weave_shot", "蛇行弹", "Weaving Shot", BuiltInLootTables.ABANDONED_MINESHAFT),
    RETURN_ARC("return_arc", "回翔弹", "Return Arc", BuiltInLootTables.SHIPWRECK_TREASURE),
    CORKSCREW_SHOT("corkscrew_shot", "螺旋钻", "Corkscrew Shot", BuiltInLootTables.END_CITY_TREASURE),
    WALL_RICOCHET("wall_ricochet", "折壁弹", "Wall Ricochet", BuiltInLootTables.TRIAL_CHAMBERS_SUPPLY),
    NEEDLE_PIERCE("needle_pierce", "穿针弹", "Piercing Needle", BuiltInLootTables.PILLAGER_OUTPOST),
    STAR_FORK("star_fork", "星屑分岔", "Star Fork"),
    PLUNGE_SHOT("plunge_shot", "落星弹", "Plunging Star"),
    GROUND_SKIMMER("ground_skimmer", "贴地游弹", "Ground Skimmer"),
    PROXIMITY_SEED("proximity_seed", "警戒伏弹", "Sentry Seed"),
    LANE_SWEEP("lane_sweep", "阶梯扫射", "Lane Sweep"),
    SHORT_GUIDANCE("short_guidance", "短程导引", "Short Guidance"),
    INTERCEPT_SHOT("intercept_shot", "截弹针", "Intercepting Needle");

    private static final Map<Identifier, PlayerTrajectoryType> BY_ID = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(PlayerTrajectoryType::id, Function.identity()));

    private final Identifier id;
    private final String chineseName;
    private final String englishName;
    @Nullable
    private final ResourceKey<LootTable> lootTable;

    PlayerTrajectoryType(String path, String chineseName, String englishName) {
        this(path, chineseName, englishName, null);
    }

    PlayerTrajectoryType(String path, String chineseName, String englishName, @Nullable ResourceKey<LootTable> lootTable) {
        this.id = ReverieDreams.id(path);
        this.chineseName = chineseName;
        this.englishName = englishName;
        this.lootTable = lootTable;
    }

    public Identifier id() {
        return this.id;
    }

    public String chineseName() {
        return this.chineseName;
    }

    public String englishName() {
        return this.englishName;
    }

    @Nullable
    public ResourceKey<LootTable> lootTable() {
        return this.lootTable;
    }

    public static Component displayName(Identifier id) {
        PlayerTrajectoryType type = BY_ID.get(id);
        return type == null
                ? Component.translatable(id.toLanguageKey())
                : Component.translatableWithFallback(id.toLanguageKey(), type.englishName());
    }

    public static boolean isPlayerTrajectory(Identifier id) {
        return BY_ID.containsKey(id);
    }
}
