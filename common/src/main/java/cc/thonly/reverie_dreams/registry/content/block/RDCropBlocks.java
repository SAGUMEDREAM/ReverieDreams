package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.block.crop.*;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;

import java.util.ArrayList;
import java.util.List;

public class RDCropBlocks {
    public static CropBlockBundle.Entry CHILL = CropBlockBundle
            .create(ReverieDreams.id("chill"))
            .setFactory(ChillCropBlock::new)
            .setMaxAge(4)
            .setGain(RDIngredientItems.CHILI)
            .setModelType(CropBlockBundle.ModelType.CROSS)
            .build();
    public static CropBlockBundle.Entry CUCUMBER = CropBlockBundle
            .create(ReverieDreams.id("cucumber"))
            .setFactory(CucumberCrop::new)
            .setMaxAge(4)
            .setGain(RDIngredientItems.CUCUMBER)
            .setModelType(CropBlockBundle.ModelType.CROSS)
            .build();
    public static CropBlockBundle.Entry GRAPE = CropBlockBundle
            .create(ReverieDreams.id("grape"))
            .setFactory(GrapeCropBlock::new)
            .setMaxAge(5)
            .setGain(RDIngredientItems.GRAPE)
            .setModelType(CropBlockBundle.ModelType.CROP)
            .build();
    public static CropBlockBundle.Entry ONION = CropBlockBundle
            .create(ReverieDreams.id("onion"))
            .setFactory(OnionCropBlock::new)
            .setMaxAge(5)
            .setGain(RDIngredientItems.ONION)
            .setModelType(CropBlockBundle.ModelType.CROP)
            .build();
    public static CropBlockBundle.Entry RED_BEANS = CropBlockBundle
            .create(ReverieDreams.id("red_beans"))
            .setFactory(RedBeansCropBlock::new)
            .setMaxAge(4)
            .setGain(RDIngredientItems.RED_BEANS)
            .setModelType(CropBlockBundle.ModelType.CROSS)
            .build();
    public static CropBlockBundle.Entry TOMATO = CropBlockBundle
            .create(ReverieDreams.id("tomato"))
            .setFactory(TomatoCropBlock::new)
            .setMaxAge(3)
            .setGain(RDIngredientItems.TOMATO)
            .setModelType(CropBlockBundle.ModelType.CROSS)
            .build();
    public static CropBlockBundle.Entry TOON = CropBlockBundle
            .create(ReverieDreams.id("toon"))
            .setFactory(ToonCropBlock::new)
            .setMaxAge(3)
            .setGain(RDIngredientItems.TOON)
            .setModelType(CropBlockBundle.ModelType.CROSS)
            .build();
    public static CropBlockBundle.Entry WHITE_RADISH = CropBlockBundle
            .create(ReverieDreams.id("white_radish"))
            .setFactory(WhiteRadishCropBlock::new)
            .setMaxAge(3)
            .setGain(RDIngredientItems.WHITE_RADISH)
            .setModelType(CropBlockBundle.ModelType.CROP)
            .build();
    public static CropBlockBundle.Entry SWEET_POTATO = CropBlockBundle
            .create(ReverieDreams.id("sweet_potato"))
            .setFactory(SweetPotatoCropBlock::new)
            .setMaxAge(3)
            .setGain(RDIngredientItems.SWEET_POTATO)
            .setModelType(CropBlockBundle.ModelType.CROP)
            .build();
    public static CropBlockBundle.Entry BROCCOLI = CropBlockBundle
            .create(ReverieDreams.id("broccoli"))
            .setFactory(BroccoliCropBlock::new)
            .setMaxAge(4)
            .setGain(RDIngredientItems.BROCCOLI)
            .setModelType(CropBlockBundle.ModelType.CROSS)
            .build();
    public static CropBlockBundle.Entry SOY_BEANS = CropBlockBundle
            .create(ReverieDreams.id("soy_beans"))
            .setFactory(RedBeansCropBlock::new)
            .setMaxAge(4)
            .self()
            .setModelType(CropBlockBundle.ModelType.CROSS)
            .build();
    public static final List<CropBlockBundle.Entry> CHEST_DROPS = new ArrayList<>();
    public static final List<CropBlockBundle.Entry> GRASS_DROPS = new ArrayList<>();

    public static void initialize() {
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
