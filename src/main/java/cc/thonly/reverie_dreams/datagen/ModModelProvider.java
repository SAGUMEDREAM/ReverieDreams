package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.*;
import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.entity.ModEntityHolders;
import cc.thonly.reverie_dreams.fumo.Fumo;
import cc.thonly.reverie_dreams.fumo.Fumos;
import cc.thonly.reverie_dreams.item.ModGuiItems;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.item.prop.RoleCard;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.state.SixteenDirection;
import cc.thonly.reverie_dreams.util.CropAgeModelProvider;
import cc.thonly.reverie_dreams.util.CropAgeUtil;
import cc.thonly.reverie_dreams.block.PolymerCropCreator;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.data.*;
import net.minecraft.client.render.model.json.ModelVariant;
import net.minecraft.client.render.model.json.ModelVariantOperator;
import net.minecraft.client.render.model.json.WeightedVariant;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.AxisRotation;
import net.minecraft.util.math.Direction;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

import static net.minecraft.client.data.BlockStateModelGenerator.modelWithYRotation;

@Slf4j
public class ModModelProvider extends FabricModelProvider {
    private final Map<Block, TexturedModel> uniqueModels = ImmutableMap.<Block, TexturedModel>builder()
            .build();

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

//    private final BlockFamily SPIRITUAL_PLANKS =

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerCubeWithCustomTextures(ModBlocks.DANMAKU_CRAFTING_TABLE, Blocks.OAK_PLANKS, TextureMap::frontSideWithCustomBottom);
        this.registerSmithingTable(blockStateModelGenerator, ModBlocks.STRENGTH_TABLE);
        blockStateModelGenerator.registerSimpleState(ModBlocks.GENSOKYO_ALTAR);
        blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.MUSIC_BLOCK);

        this.registerWoodCreator(blockStateModelGenerator, ModBlocks.SPIRITUAL);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, ModBlocks.ICE_SCALES);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, ModBlocks.DREAM_STONE);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, ModBlocks.DREAM_STONE_BRICK);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, ModBlocks.MOON_STONE);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, ModBlocks.MOON_STONE_BRICK);
        this.registerWoodCreator(blockStateModelGenerator, MIBlocks.LEMON);
        blockStateModelGenerator.registerCubeAllModelTexturePool(MIBlocks.LEMON_FRUIT_LEAVES);
        this.registerWoodCreator(blockStateModelGenerator, MIBlocks.GINKGO);
        blockStateModelGenerator.registerCubeAllModelTexturePool(MIBlocks.GINKGO_FRUIT_LEAVES);
        this.registerWoodCreator(blockStateModelGenerator, MIBlocks.PEACH);
        blockStateModelGenerator.registerCubeAllModelTexturePool(MIBlocks.PEACH_FRUIT_LEAVES);

        blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.MAGIC_ICE_BLOCK);
        blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.POINT_BLOCK);
        blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.POWER_BLOCK);
        blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.SILVER_ORE);
        blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.DREAM_CRYSTAL_ORE);
        blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.SILVER_BLOCK);
        blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.DEEPSLATE_SILVER_ORE);
        blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.ORB_ORE);
        blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.DEEPSLATE_ORB_ORE);
        blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.RED_ORB_BLOCK);
        blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.YELLOW_ORB_BLOCK);
        blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.BLUE_ORB_BLOCK);
        blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.GREEN_ORB_BLOCK);
        blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.PURPLE_ORB_BLOCK);

        blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.DREAM_BLUE_BLOCK);
        blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.DREAM_RED_BLOCK);
        blockStateModelGenerator.registerSimpleState(ModBlocks.MARISA_HAT_BLOCK);

        for (Fumo instance : Fumos.getView()) {
            blockStateModelGenerator.registerSimpleState(instance.block());
//            blockStateModelGenerator.registerSimpleState(instance.block());
        }

        this.generateCropBlockModel(blockStateModelGenerator);
        this.generateMIBlock(blockStateModelGenerator);
    }

    public void generateCropBlockModel(BlockStateModelGenerator blockStateModelGenerator) {
        Set<Map.Entry<Identifier, PolymerCropCreator.Instance>> views = PolymerCropCreator.getViews();
        for (Map.Entry<Identifier, PolymerCropCreator.Instance> view : views) {
            Identifier id = null;
            try {
                PolymerCropCreator.Instance instance = view.getValue();
                id = instance.getIdentifier();
                AbstractCropBlock cropBlock = instance.getCropBlock();
                PolymerCropCreator.ModelType modelType = instance.getModelType();
                IntProperty ageProperty = cropBlock.getAgeProperty();
                CropAgeModelProvider provider = instance.getProvider();

                if (modelType == PolymerCropCreator.ModelType.CROSS) {
                    blockStateModelGenerator.registerTintableCrossBlockStateWithStages(cropBlock, BlockStateModelGenerator.CrossType.NOT_TINTED, ageProperty, CropAgeUtil.toArray(ageProperty));
                } else if (modelType == PolymerCropCreator.ModelType.CROP) {
                    blockStateModelGenerator.registerCrop(cropBlock, ageProperty, provider.toArray());
                }
            } catch (Exception e) {
                log.error("Can't generate crop block model {}, cause by {}", id, e.getCause());
            }
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        // 调试
        itemModelGenerator.register(ModItems.BATTLE_STICK, Models.HANDHELD);
        itemModelGenerator.register(ModItems.OWNER_STICK, Models.HANDHELD);

        // 图标
        itemModelGenerator.register(ModItems.ICON, Models.GENERATED);
        itemModelGenerator.register(ModItems.FUMO_ICON, Models.GENERATED);
        itemModelGenerator.register(ModItems.ROLE_ICON, Models.GENERATED);
        itemModelGenerator.registerWithDyeableOverlay(ModItems.SPAWN_EGG);
        itemModelGenerator.register(ModItems.DANMAKU, Models.GENERATED);

        // 材料
        itemModelGenerator.register(ModItems.POINT, Models.GENERATED);
        itemModelGenerator.register(ModItems.POWER, Models.GENERATED);
        itemModelGenerator.register(ModItems.UPGRADED_HEALTH_FRAGMENT, Models.GENERATED);
        itemModelGenerator.register(ModItems.BOMB_FRAGMENT, Models.GENERATED);
        itemModelGenerator.register(ModItems.RED_ORB, Models.GENERATED);
        itemModelGenerator.register(ModItems.BLUE_ORB, Models.GENERATED);
        itemModelGenerator.register(ModItems.YELLOW_ORB, Models.GENERATED);
        itemModelGenerator.register(ModItems.GREEN_ORB, Models.GENERATED);
        itemModelGenerator.register(ModItems.PURPLE_ORB, Models.GENERATED);
        itemModelGenerator.register(ModItems.YIN_YANG_ORB, Models.GENERATED);
        itemModelGenerator.register(ModItems.SPEED_FEATHER, Models.GENERATED);
        itemModelGenerator.register(ModItems.DREAM_CRYSTAL_FRAGMENT, Models.GENERATED);

        // 道具
        itemModelGenerator.register(ModItems.TOUHOU_HELPER, Models.GENERATED);
        itemModelGenerator.register(ModItems.UPGRADED_HEALTH, Models.GENERATED);
        itemModelGenerator.register(ModItems.BOMB, Models.GENERATED);
        itemModelGenerator.register(ModItems.CROSSING_CHISEL, Models.GENERATED);
        itemModelGenerator.register(ModItems.GAP_BALL, Models.GENERATED);
        itemModelGenerator.register(ModItems.TIME_STOP_CLOCK, Models.GENERATED);
        itemModelGenerator.register(ModItems.EARPHONE, Models.GENERATED);
        itemModelGenerator.register(ModItems.KOISHI_HAT, Models.GENERATED);
        itemModelGenerator.register(ModItems.FUMO_LICENSE, Models.GENERATED);
        itemModelGenerator.register(ModItems.CURSED_DECOY_DOLl);
        itemModelGenerator.register(ModItems.VAISRAVANAS_PAGODA, Models.GENERATED);
        itemModelGenerator.register(ModItems.DREAM_PILLOW);
        itemModelGenerator.register(ModItems.TENGU_CAMERA);
        itemModelGenerator.register(ModItems.BAD_APPLE, Models.GENERATED);

        // 武器
        itemModelGenerator.register(ModItems.HAKUREI_CANE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.BAGUA_FURNACE);
        itemModelGenerator.register(ModItems.WIND_BLESSING_CANE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.MAGIC_BROOM);
        itemModelGenerator.register(ModItems.KNIFE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.GUNGNIR, Models.HANDHELD);
        itemModelGenerator.register(ModItems.LEVATIN, Models.HANDHELD);
        itemModelGenerator.register(ModItems.ROKANKEN, Models.HANDHELD);
        itemModelGenerator.register(ModItems.HAKUROKEN, Models.HANDHELD);
        itemModelGenerator.register(ModItems.PAPILIO_PATTERN_FAN, Models.HANDHELD);
        itemModelGenerator.register(ModItems.HORAI_DAMA_NO_EDA, Models.GENERATED);
        itemModelGenerator.register(ModItems.IBUKIHO, Models.HANDHELD);
        itemModelGenerator.register(ModItems.SWORD_OF_HISOU, Models.HANDHELD);
        itemModelGenerator.register(ModItems.MAPLE_LEAF_FAN, Models.HANDHELD);
        itemModelGenerator.register(ModItems.MANPOZUCHI);
//        itemModelGenerator.register(ModItems.NUE_TRIDENT);
        itemModelGenerator.register(ModItems.TRUMPET_GUN);
        itemModelGenerator.register(ModItems.TREASURE_HUNTING_ROD, Models.HANDHELD);
        itemModelGenerator.register(ModItems.VIOLIN);
        itemModelGenerator.register(ModItems.KEYBOARD);
        itemModelGenerator.register(ModItems.TRUMPET);
        itemModelGenerator.register(ModItems.DEATH_SCYTHE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.MAID_HAIRBAND, Models.GENERATED);
        itemModelGenerator.register(ModItems.MAID_UPPER_SKIRT, Models.GENERATED);
        itemModelGenerator.register(ModItems.MAID_LOWER_SKIRT, Models.GENERATED);
        itemModelGenerator.register(ModItems.MAID_SHOE, Models.GENERATED);

        // 工具矿物类
        itemModelGenerator.register(ModItems.RAW_SILVER, Models.GENERATED);
        itemModelGenerator.register(ModItems.SILVER_INGOT, Models.GENERATED);
        itemModelGenerator.register(ModItems.SILVER_NUGGET, Models.GENERATED);
        itemModelGenerator.register(ModItems.SILVER_SWORD, Models.HANDHELD);
        itemModelGenerator.register(ModItems.SILVER_AXE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.SILVER_PICKAXE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.SILVER_SHOVEL, Models.HANDHELD);
        itemModelGenerator.register(ModItems.SILVER_HOE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.SILVER_HELMET, Models.HANDHELD);
        itemModelGenerator.register(ModItems.SILVER_CHESTPLATE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.SILVER_LEGGINGS, Models.HANDHELD);
        itemModelGenerator.register(ModItems.SILVER_BOOTS, Models.HANDHELD);
        itemModelGenerator.register(ModItems.ICE_SCALES, Models.GENERATED);
        itemModelGenerator.register(ModItems.MAGIC_ICE_SWORD, Models.HANDHELD);
        itemModelGenerator.register(ModItems.MAGIC_ICE_AXE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.MAGIC_ICE_PICKAXE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.MAGIC_ICE_SHOVEL, Models.HANDHELD);
        itemModelGenerator.register(ModItems.MAGIC_ICE_HOE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.MAGIC_ICE_HELMET, Models.HANDHELD);
        itemModelGenerator.register(ModItems.MAGIC_ICE_CHESTPLATE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.MAGIC_ICE_LEGGINGS, Models.HANDHELD);
        itemModelGenerator.register(ModItems.MAGIC_ICE_BOOTS, Models.HANDHELD);

        // 符卡
        itemModelGenerator.register(ModItems.SPELL_CARD_TEMPLATE, Models.GENERATED);

        // 角色卡
        itemModelGenerator.registerDyeable(ModItems.ROLE_CARD, RoleCard.DEFAULT_COLOR.intValue());
        itemModelGenerator.register(ModItems.ROLE_ARCHIVE, Models.GENERATED);

        // 唱片
        itemModelGenerator.register(ModItems.HR01_01, Models.GENERATED);
        itemModelGenerator.register(ModItems.HR02_08, Models.GENERATED);
        itemModelGenerator.register(ModItems.HR03_01, Models.GENERATED);
        itemModelGenerator.register(ModItems.MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS, Models.GENERATED);
        itemModelGenerator.register(ModItems.YV_FLOWER_CLOCK_AND_DREAMS, Models.GENERATED);
        itemModelGenerator.register(ModItems.GLOWING_NEEDLES_LITTLE_PEOPLE, Models.GENERATED);
        itemModelGenerator.register(ModItems.COOKIE, Models.GENERATED);
        itemModelGenerator.register(ModItems.BADAPPLE, Models.GENERATED);

        // 测试物品
//        itemModelGenerator.registerWithDyeableOverlay(ModItems.TEST_COLOR_DANMAKU_ITEM);

//        itemModelGenerator.register(ModItems.EMPTY_SPELL_CARD, Models.GENERATED);

        // 调试
//        itemModelGenerator.register(ModItems.DEBUG_DANMAKU_ITEM, Models.GENERATED);
//        itemModelGenerator.register(ModItems.DEBUG_SPELL_CARD_ITEM, Models.GENERATED);
//        itemModelGenerator.register(ModItems.DEBUG_SPELL_CARD_ITEM2, Models.GENERATED);

        this.generateGuiItemModels(itemModelGenerator);
        this.generateDanmakuItemModels(itemModelGenerator);
        this.generateHolder(itemModelGenerator);
        this.generateMIItem(itemModelGenerator);
    }

    public void generateMIBlock(BlockStateModelGenerator blockStateModelGenerator) {
        this.registerRotatable(blockStateModelGenerator, MIBlocks.COOKING_POT);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.CUTTING_BOARD);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.FRYING_PAN);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.GRILL);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.STEAMER);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.MYSTIA_COOKING_POT);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.MYSTIA_CUTTING_BOARD);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.MYSTIA_FRYING_PAN);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.MYSTIA_GRILL);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.MYSTIA_STEAMER);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.SUPER_COOKING_POT);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.SUPER_CUTTING_BOARD);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.SUPER_FRYING_PAN);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.SUPER_GRILL);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.SUPER_STEAMER);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.EXTREME_COOKING_POT);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.EXTREME_CUTTING_BOARD);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.EXTREME_FRYING_PAN);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.EXTREME_GRILL);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.EXTREME_STEAMER);

        blockStateModelGenerator.registerSimpleState(MIBlocks.ITEM_DISPLAY);

        blockStateModelGenerator.registerCubeAllModelTexturePool(MIBlocks.BLACK_SALT_BLOCK);

        blockStateModelGenerator.registerTintableCross(MIBlocks.UDUMBARA_FLOWER, BlockStateModelGenerator.CrossType.NOT_TINTED);
        blockStateModelGenerator.registerTintableCross(MIBlocks.TREMELLA, BlockStateModelGenerator.CrossType.NOT_TINTED);
    }

    public void generateMIItem(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(MIItems.MYSTIA_ICON, Models.GENERATED);
        for (Item item : MIItems.INGREDIENTS) {
            itemModelGenerator.register(item, Models.GENERATED);
        }
        for (Item item : MIItems.FOOD_ITEMS) {
            itemModelGenerator.register(item, Models.GENERATED);
        }
        for (Item item : MIItems.DRINK_ITEMS) {
            itemModelGenerator.register(item, Models.GENERATED);
        }
    }

    public void generateHolder(ItemModelGenerator itemModelGenerator) {
        ModEntityHolders.HOLDERS.forEach(itemModelGenerator::register);
    }

    public void generateGuiItemModels(ItemModelGenerator itemModelGenerator) {
        for (Item item : ModGuiItems.getGuiItemList()) {
            this.registerGuiItem(itemModelGenerator, item);
        }
    }

    public final void registerRotatable(BlockStateModelGenerator blockStateModelGenerator, Block block) {
        Identifier id = Registries.BLOCK.getId(block);
        Identifier modelId = Identifier.of(id.getNamespace(), "block/" + id.getPath());

        ModelVariant modelVariant = new ModelVariant(modelId);

        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockModelDefinitionCreator.of(block, modelWithYRotation(modelVariant))
        );
    }

    private void registerGuiItem(ItemModelGenerator itemModelGenerator, Item item) {
//        Model guiSlotModel = item("custom_slot", TextureKey.LAYER0);
//        itemModelGenerator.register(item, guiSlotModel);
    }

    public void generateDanmakuItemModels(ItemModelGenerator itemModelGenerator) {
        for (Item item : RegistryManager.DANMAKU_TYPE.values()
                .stream()
                .filter(type -> !DanmakuTypes.UNLIST.contains(type))
                .map(DanmakuType::getItem)
                .toList()) {
            itemModelGenerator.registerWithDyeableOverlay(item);
        }
    }

    private void registerSmithingTable(BlockStateModelGenerator blockStateModelGenerator, Block block) {
        TextureMap textureMap = new TextureMap()
                .put(TextureKey.PARTICLE, TextureMap.getSubId(block, "_front"))
                .put(TextureKey.DOWN, TextureMap.getSubId(block, "_bottom"))
                .put(TextureKey.UP, TextureMap.getSubId(block, "_top"))
                .put(TextureKey.NORTH, TextureMap.getSubId(block, "_front"))
                .put(TextureKey.SOUTH, TextureMap.getSubId(block, "_front"))
                .put(TextureKey.EAST, TextureMap.getSubId(block, "_side"))
                .put(TextureKey.WEST, TextureMap.getSubId(block, "_side"));
        blockStateModelGenerator.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, BlockStateModelGenerator.createWeightedVariant(Models.CUBE.upload(block, textureMap, blockStateModelGenerator.modelCollector))));

    }

    private static Model item(String parent, TextureKey... requiredTextureKeys) {
        return new Model(Optional.of(Touhou.id("item/" + parent)), Optional.empty(), requiredTextureKeys);
    }

    private void registerWoodCreator(BlockStateModelGenerator blockStateModelGenerator, WoodCreator creator) {
        blockStateModelGenerator.createLogTexturePool(creator.log()).log(creator.log()).wood(creator.wood());
        blockStateModelGenerator.createLogTexturePool(creator.strippedLog()).log(creator.strippedLog()).wood(creator.strippedWood());
        blockStateModelGenerator.registerCubeAllModelTexturePool(creator.leaves());
        blockStateModelGenerator.registerTintableCross(creator.sapling(), BlockStateModelGenerator.CrossType.NOT_TINTED);
        this.registerFamily(blockStateModelGenerator, creator.getBlockFamily());
    }

    private void registerDecorativeBlockCreator(BlockStateModelGenerator blockStateModelGenerator, DecorativeBlockCreator creator) {
        blockStateModelGenerator.registerCubeAllModelTexturePool(creator.block()).stairs(creator.stair()).slab(creator.slab()).wall(creator.wall());
    }

    private void registerFamily(BlockStateModelGenerator generator, BlockFamily family) {
        TexturedModel texturedModel = this.uniqueModels.getOrDefault(family.getBaseBlock(), TexturedModel.CUBE_ALL.get(family.getBaseBlock()));
        generator.new BlockTexturePool(texturedModel.getTextures()).base(family.getBaseBlock(), texturedModel.getModel()).family(family);
    }

    @Override
    public String getName() {
        return "Touhou Model Provider";
    }
}
