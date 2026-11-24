package cc.thonly.reverie_dreams.registry.content.danmaku.soku;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Fighting moves share the pattern registry, but are not collectible spell cards. */
public final class SokuMoves {
    private SokuMoves() {
    }

    private static final List<SokuMove> MOVES = List.of(
            nearA("th105", false), nearA("th105", true),
            nearA("th123", false), nearA("th123", true));
    private static final Map<Identifier, SokuMove> BY_ID = MOVES.stream()
            .collect(Collectors.toUnmodifiableMap(SokuMove::id, Function.identity()));

    private static SokuMove nearA(String game, boolean chained) {
        return new SokuMove(ReverieDreams.id("soku/" + game + "/reimu/" + (chained ? "5aa" : "5a")),
                chained ? "近距離AA" : "近距離A", chained ? "近身 AA" : "近身 A",
                chained ? "Close AA" : "Close A", "entity.reverie_dreams.reimu", game,
                chained ? 320 : 300, () -> ReimuMelee.nearA(chained));
    }

    public static List<SokuMove> catalog() {
        return MOVES;
    }

    @Nullable
    public static SokuMove find(@Nullable Identifier id) {
        return id == null ? null : BY_ID.get(id);
    }

    public static void register(BiConsumer<Identifier, DanmakuPattern> registrar) {
        for (SokuMove move : MOVES) {
            registrar.accept(move.id(), move.factory().get());
        }
    }
}
