package cc.thonly.reverie_dreams.fabric.datagen.tag;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractCustomRegistryTagProvider;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpells;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 为每名东方角色生成一张符卡标签：tag 的 registry 为 danmaku_pattern，
 * path 即角色短键（如 reverie_dreams:yuyuko），内容为该角色的全部已实现符卡。
 * 供日后按角色从标签中抽取符卡（registry.getHolder(tag) 遍历）。
 */
public class SpellCardCharacterTagProvider extends AbstractCustomRegistryTagProvider<DanmakuPattern, DanmakuPattern> {
    public SpellCardCharacterTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(BuiltInRegistryProviders.DANMAKU_PATTERN, output, future);
    }

    @Override
    public void addTags(HolderLookup.Provider provider) {
        Map<String, TagAppender<DanmakuPattern, DanmakuPattern>> appenders = new HashMap<>();
        for (var entry : BuiltInRegistryProviders.DANMAKU_PATTERN.streamIdToValue().toList()) {
            Identifier id = entry.getKey();
            String characterKey = TouhouSpells.characterKeyOf(id);
            if (characterKey == null) {
                continue;
            }
            // entity.reverie_dreams.yuyuko -> yuyuko
            // item.reverie_dreams.spellcard.character.inaba_tewi -> inaba_tewi
            String characterPath = characterKey.substring(characterKey.lastIndexOf('.') + 1);
            var appender = appenders.computeIfAbsent(characterPath, path -> {
                TagKey<DanmakuPattern> tag = TagKey.create(BuiltInRegistryProviders.DANMAKU_PATTERN.key(), ReverieDreams.id(path));
                return valueLookupBuilder(tag);
            });
            appender.add(entry.getValue());
        }
    }

    @Override
    public String getName() {
        return "Spell Card Character Tag Provider";
    }
}
