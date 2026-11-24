package cc.thonly.reverie_dreams.fabric.datagen.lang;

import cc.thonly.reverie_dreams.world.RDBuiltInGameRules;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinBiomes;
import cc.thonly.reverie_dreams.world.gen.biome.MapleForestRegion;
import cc.thonly.reverie_dreams.world.gensokyo.bamboo.BambooForestBootstrap;
import cc.thonly.reverie_dreams.world.moon.MoonBiomes;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder;
import net.minecraft.util.Util;

public interface  WorldLang {
    static void add(TranslationBuilder builder) {
        builder.add(Util.makeDescriptionId("biome", MapleForestRegion.MAPLE_FOREST.identifier()), "枫树森林");
        builder.add(Util.makeDescriptionId("biome", RDBuiltinBiomes.GENSOKYO_PLAINS.identifier()), "幻想乡平原");
        builder.add("biome.reverie_dreams.gensokyo_forest", "幻想乡森林");
        builder.add("biome.reverie_dreams.gensokyo_jungle", "幻想乡丛林");
        builder.add("biome.reverie_dreams.gensokyo_birch_forest", "幻想乡白桦林");
        builder.add("biome.reverie_dreams.gensokyo_taiga", "幻想乡针叶林");
        builder.add("biome.reverie_dreams.gensokyo_cherry_grove", "幻想乡樱花林");
        builder.add("biome.reverie_dreams.gensokyo_dark_forest", "幻想乡黑森林");
        builder.add("biome.reverie_dreams.gensokyo_magic_forest", "魔法森林");
        builder.add(Util.makeDescriptionId("biome", RDBuiltinBiomes.MAGIC_FOREST.identifier()), "主世界魔法森林");
        builder.add("biome.reverie_dreams.gensokyo_maple_forest", "幻想乡枫树林");
        builder.add(Util.makeDescriptionId("biome", RDBuiltinBiomes.THE_MOON.identifier()), "月面高地");
        builder.add(Util.makeDescriptionId("biome", MoonBiomes.BASALT_MARE.identifier()), "玄武月海");
        builder.add(Util.makeDescriptionId("biome", MoonBiomes.SHADOW_ICE_CRATER.identifier()), "阴影冰坑");
        builder.add(Util.makeDescriptionId("biome", RDBuiltinBiomes.MOON_SEA.identifier()), "月海境界");
        builder.add(RDBuiltInGameRules.DO_GHOST.value().getDescriptionId(), "允许幽灵自动生成");
        builder.add(RDBuiltInGameRules.FREE_CHOICE_OF_ROLE.value().getDescriptionId(), "自由选择角色");
        builder.add(Util.makeDescriptionId("biome", RDBuiltinBiomes.GENSOKYO_PHANTOM_MEADOW.identifier()),"幻草原");
        builder.add(Util.makeDescriptionId("biome", RDBuiltinBiomes.GENSOKYO_MUENZUKA.identifier()),"无缘塚");
        builder.add(Util.makeDescriptionId("biome", RDBuiltinBiomes.GENSOKYO_GARDEN_OF_THE_SUN.identifier()),"太阳花田");
        builder.add(Util.makeDescriptionId("biome", BambooForestBootstrap.BIOME.identifier()),"迷途竹林");
    }

}
