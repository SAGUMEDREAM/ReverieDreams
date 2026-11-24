package cc.thonly.reverie_dreams.registry.content.danmaku.soku;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

/** Only reviewed executable moves belong here; PAT candidates stay in the research index. */
public record SokuMove(Identifier id, String japaneseName, String chineseName, String englishName,
                       String characterKey, String game, int action, Supplier<DanmakuPattern> factory) {
}
