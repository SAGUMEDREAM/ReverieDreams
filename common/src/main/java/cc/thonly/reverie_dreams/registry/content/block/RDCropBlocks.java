package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.block.crop.*;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import net.blay09.mods.balm.world.level.block.BalmBlockRegistrar;

import java.util.ArrayList;
import java.util.List;

public class RDCropBlocks {
    public static CropBlockBundle.Entry CHILL;
    public static CropBlockBundle.Entry CUCUMBER;
    public static CropBlockBundle.Entry GRAPE;
    public static CropBlockBundle.Entry ONION;
    public static CropBlockBundle.Entry RED_BEANS;
    public static CropBlockBundle.Entry TOMATO;
    public static CropBlockBundle.Entry TOON;
    public static CropBlockBundle.Entry WHITE_RADISH;
    public static CropBlockBundle.Entry SWEET_POTATO;
    public static CropBlockBundle.Entry BROCCOLI;
    public static CropBlockBundle.Entry SOY_BEANS;
    public static final List<CropBlockBundle.Entry> CHEST_DROPS = new ArrayList<>();
    public static final List<CropBlockBundle.Entry> GRASS_DROPS = new ArrayList<>();

    public static void initialize(BalmBlockRegistrar registrar) {
        CHILL = CropBlockBundle
                .create(ReverieDreams.id("chill"))
                .setFactory(ChillCropBlock::new)
                .setMaxAge(4)
                .setGain(RDIngredientItems.CHILI)
                .setModelType(CropBlockBundle.ModelType.CROSS)
                .build(registrar);

        CUCUMBER = CropBlockBundle
                .create(ReverieDreams.id("cucumber"))
                .setFactory(CucumberCrop::new)
                .setMaxAge(4)
                .setGain(RDIngredientItems.CUCUMBER)
                .setModelType(CropBlockBundle.ModelType.CROSS)
                .build(registrar);

        GRAPE = CropBlockBundle
                .create(ReverieDreams.id("grape"))
                .setFactory(GrapeCropBlock::new)
                .setMaxAge(5)
                .setGain(RDIngredientItems.GRAPE)
                .setModelType(CropBlockBundle.ModelType.CROP)
                .build(registrar);

        ONION = CropBlockBundle
                .create(ReverieDreams.id("onion"))
                .setFactory(OnionCropBlock::new)
                .setMaxAge(5)
                .setGain(RDIngredientItems.ONION)
                .setModelType(CropBlockBundle.ModelType.CROP)
                .build(registrar);

        RED_BEANS = CropBlockBundle
                .create(ReverieDreams.id("red_beans"))
                .setFactory(RedBeansCropBlock::new)
                .setMaxAge(4)
                .setGain(RDIngredientItems.RED_BEANS)
                .setModelType(CropBlockBundle.ModelType.CROSS)
                .build(registrar);

        TOMATO = CropBlockBundle
                .create(ReverieDreams.id("tomato"))
                .setFactory(TomatoCropBlock::new)
                .setMaxAge(3)
                .setGain(RDIngredientItems.TOMATO)
                .setModelType(CropBlockBundle.ModelType.CROSS)
                .build(registrar);

        TOON = CropBlockBundle
                .create(ReverieDreams.id("toon"))
                .setFactory(ToonCropBlock::new)
                .setMaxAge(3)
                .setGain(RDIngredientItems.TOON)
                .setModelType(CropBlockBundle.ModelType.CROSS)
                .build(registrar);

        WHITE_RADISH = CropBlockBundle
                .create(ReverieDreams.id("white_radish"))
                .setFactory(WhiteRadishCropBlock::new)
                .setMaxAge(3)
                .setGain(RDIngredientItems.WHITE_RADISH)
                .setModelType(CropBlockBundle.ModelType.CROP)
                .build(registrar);

        SWEET_POTATO = CropBlockBundle
                .create(ReverieDreams.id("sweet_potato"))
                .setFactory(SweetPotatoCropBlock::new)
                .setMaxAge(3)
                .setGain(RDIngredientItems.SWEET_POTATO)
                .setModelType(CropBlockBundle.ModelType.CROP)
                .build(registrar);

        BROCCOLI = CropBlockBundle
                .create(ReverieDreams.id("broccoli"))
                .setFactory(BroccoliCropBlock::new)
                .setMaxAge(4)
                .setGain(RDIngredientItems.BROCCOLI)
                .setModelType(CropBlockBundle.ModelType.CROSS)
                .build(registrar);

        SOY_BEANS = CropBlockBundle
                .create(ReverieDreams.id("soy_beans"))
                .setFactory(RedBeansCropBlock::new)
                .setMaxAge(4)
                .self()
                .setModelType(CropBlockBundle.ModelType.CROSS)
                .build(registrar);

        CHEST_DROPS.clear();
        CHEST_DROPS.addAll(List.of(
                SWEET_POTATO, WHITE_RADISH, TOON, RED_BEANS, GRAPE
        ));

        GRASS_DROPS.clear();
        GRASS_DROPS.addAll(List.of(
                TOMATO, RED_BEANS, ONION, CUCUMBER, CHILL, BROCCOLI, SOY_BEANS
        ));
    }
}
