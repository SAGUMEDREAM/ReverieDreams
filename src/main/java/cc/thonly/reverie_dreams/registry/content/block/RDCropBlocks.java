package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.creator.CropBlockCreator;
import cc.thonly.reverie_dreams.block.crop.*;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;

import java.util.ArrayList;
import java.util.List;

public class RDCropBlocks {
    public static final CropBlockCreator.Instance CHILL = CropBlockCreator
            .createCreator(ReverieDreams.id("chill"))
            .setFactory(ChillCropBlock::new)
            .setMaxAge(4)
            .setGain(RDIngredientItems.CHILI)
            .setModelType(CropBlockCreator.ModelType.CROSS)
            .build();
    public static final CropBlockCreator.Instance CUCUMBER = CropBlockCreator
            .createCreator(ReverieDreams.id("cucumber"))
            .setFactory(CucumberCrop::new)
            .setMaxAge(4)
            .setGain(RDIngredientItems.CUCUMBER)
            .setModelType(CropBlockCreator.ModelType.CROSS)
            .build();
    public static final CropBlockCreator.Instance GRAPE = CropBlockCreator
            .createCreator(ReverieDreams.id("grape"))
            .setFactory(GrapeCropBlock::new)
            .setMaxAge(5)
            .setGain(RDIngredientItems.GRAPE)
            .setModelType(CropBlockCreator.ModelType.CROP)
            .build();
    public static final CropBlockCreator.Instance ONION = CropBlockCreator
            .createCreator(ReverieDreams.id("onion"))
            .setFactory(OnionCropBlock::new)
            .setMaxAge(5)
            .setGain(RDIngredientItems.ONION)
            .setModelType(CropBlockCreator.ModelType.CROP)
            .build();
    public static final CropBlockCreator.Instance RED_BEANS = CropBlockCreator
            .createCreator(ReverieDreams.id("red_beans"))
            .setFactory(RedBeansCropBlock::new)
            .setMaxAge(4)
            .setGain(RDIngredientItems.RED_BEANS)
            .setModelType(CropBlockCreator.ModelType.CROSS)
            .build();
    public static final CropBlockCreator.Instance TOMATO = CropBlockCreator
            .createCreator(ReverieDreams.id("tomato"))
            .setFactory(TomatoCropBlock::new)
            .setMaxAge(3)
            .setGain(RDIngredientItems.TOMATO)
            .setModelType(CropBlockCreator.ModelType.CROSS)
            .build();
    public static final CropBlockCreator.Instance TOON = CropBlockCreator
            .createCreator(ReverieDreams.id("toon"))
            .setFactory(ToonCropBlock::new)
            .setMaxAge(3)
            .setGain(RDIngredientItems.TOON)
            .setModelType(CropBlockCreator.ModelType.CROSS)
            .build();
    public static final CropBlockCreator.Instance WHITE_RADISH = CropBlockCreator
            .createCreator(ReverieDreams.id("white_radish"))
            .setFactory(WhiteRadishCropBlock::new)
            .setMaxAge(3)
            .setGain(RDIngredientItems.WHITE_RADISH)
            .setModelType(CropBlockCreator.ModelType.CROP)
            .build();
    public static final CropBlockCreator.Instance SWEET_POTATO = CropBlockCreator
            .createCreator(ReverieDreams.id("sweet_potato"))
            .setFactory(SweetPotatoCropBlock::new)
            .setMaxAge(3)
            .setGain(RDIngredientItems.SWEET_POTATO)
            .setModelType(CropBlockCreator.ModelType.CROP)
            .build();
    public static final List<CropBlockCreator.Instance> CHEST_DROPS = new ArrayList<>(List.of(SWEET_POTATO, WHITE_RADISH, TOON, RED_BEANS, GRAPE));
    public static final CropBlockCreator.Instance BROCCOLI = CropBlockCreator
            .createCreator(ReverieDreams.id("broccoli"))
            .setFactory(BroccoliCropBlock::new)
            .setMaxAge(4)
            .setGain(RDIngredientItems.BROCCOLI)
            .setModelType(CropBlockCreator.ModelType.CROSS)
            .build();
    public static final CropBlockCreator.Instance SOY_BEANS = CropBlockCreator
            .createCreator(ReverieDreams.id("soy_beans"))
            .setFactory(RedBeansCropBlock::new)
            .setMaxAge(4)
            .self()
            .setModelType(CropBlockCreator.ModelType.CROSS)
            .build();
    public static final List<CropBlockCreator.Instance> GRASS_DROPS = new ArrayList<>(List.of(TOMATO, RED_BEANS, ONION, CUCUMBER, CHILL, BROCCOLI, SOY_BEANS));

    public static void registerBlocks() {

    }
}
