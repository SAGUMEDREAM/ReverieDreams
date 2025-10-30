package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.CropBlockCreator;
import cc.thonly.reverie_dreams.block.DecorativeBlockCreator;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.block.WoodCreator;
import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.entity.ModEntityHolders;
import cc.thonly.reverie_dreams.fumo.Fumo;
import cc.thonly.reverie_dreams.fumo.Fumos;
import cc.thonly.reverie_dreams.item.ModGuiItems;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.item.builder.RoleCard;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.state.ModBlockStateTemplates;
import cc.thonly.reverie_dreams.state.SixteenDirection;
import cc.thonly.reverie_dreams.util.block.CropAgeModelProvider;
import cc.thonly.reverie_dreams.util.block.CropAgeUtil;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.*;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.BlockModelGenerators.BlockFamilyProvider;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static net.minecraft.client.data.models.BlockModelGenerators.createRotatedVariants;

@Slf4j
public class ModModelProvider extends FabricModelProvider {
    private final Map<Block, TexturedModel> uniqueModels = ImmutableMap.<Block, TexturedModel>builder()
            .build();

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

//    private final BlockFamily SPIRITUAL_PLANKS =

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.createCraftingTableLike(ModBlocks.DANMAKU_CRAFTING_TABLE, Blocks.OAK_PLANKS, TextureMapping::craftingTable);
        this.registerSmithingTable(blockStateModelGenerator, ModBlocks.STRENGTH_TABLE);
        blockStateModelGenerator.createNonTemplateModelBlock(ModBlocks.GENSOKYO_ALTAR);
        blockStateModelGenerator.family(ModBlocks.MUSIC_BLOCK);

        this.registerWoodCreator(blockStateModelGenerator, ModBlocks.SPIRITUAL);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, ModBlocks.ICE_SCALES);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, ModBlocks.DREAM_STONE);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, ModBlocks.DREAM_STONE_BRICK);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, ModBlocks.MOON_STONE);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, ModBlocks.MOON_STONE_BRICK);
        this.registerWoodCreator(blockStateModelGenerator, MIBlocks.LEMON);
        blockStateModelGenerator.family(MIBlocks.LEMON_FRUIT_LEAVES);
        this.registerWoodCreator(blockStateModelGenerator, MIBlocks.GINKGO);
        blockStateModelGenerator.family(MIBlocks.GINKGO_FRUIT_LEAVES);
        this.registerWoodCreator(blockStateModelGenerator, MIBlocks.PEACH);
        blockStateModelGenerator.family(MIBlocks.PEACH_FRUIT_LEAVES);

        blockStateModelGenerator.family(ModBlocks.MAGIC_ICE_BLOCK);
        blockStateModelGenerator.family(ModBlocks.POINT_BLOCK);
        blockStateModelGenerator.family(ModBlocks.POWER_BLOCK);
        blockStateModelGenerator.family(ModBlocks.SILVER_ORE);
        blockStateModelGenerator.family(ModBlocks.DREAM_CRYSTAL_ORE);
        blockStateModelGenerator.family(ModBlocks.SILVER_BLOCK);
//        this.registerChest(blockStateModelGenerator, ModBlocks.SILVER_CHEST_BLOCK.chestBlock(), ModBlocks.SILVER_BLOCK);
        blockStateModelGenerator.family(ModBlocks.DEEPSLATE_SILVER_ORE);
        blockStateModelGenerator.family(ModBlocks.ORB_ORE);
        blockStateModelGenerator.family(ModBlocks.DEEPSLATE_ORB_ORE);
        blockStateModelGenerator.family(ModBlocks.RED_ORB_BLOCK);
        blockStateModelGenerator.family(ModBlocks.YELLOW_ORB_BLOCK);
        blockStateModelGenerator.family(ModBlocks.BLUE_ORB_BLOCK);
        blockStateModelGenerator.family(ModBlocks.GREEN_ORB_BLOCK);
        blockStateModelGenerator.family(ModBlocks.PURPLE_ORB_BLOCK);

        blockStateModelGenerator.family(ModBlocks.DREAM_BLUE_BLOCK);
        blockStateModelGenerator.family(ModBlocks.DREAM_RED_BLOCK);
        blockStateModelGenerator.createNonTemplateModelBlock(ModBlocks.MARISA_HAT_BLOCK);
        this.registerRotatable(blockStateModelGenerator, ModBlocks.CASH_BOX_BLOCK);
        blockStateModelGenerator.createNonTemplateModelBlock(ModBlocks.ANTI_COLLISION_BARREL);
        blockStateModelGenerator.createNonTemplateModelBlock(ModBlocks.WHEEL_CHAIR);
        blockStateModelGenerator.createNonTemplateModelBlock(ModBlocks.WOODEN_BOX.chestBlock());

        for (Fumo fumoType : Fumos.getView()) {
            this.registerFumo(blockStateModelGenerator, fumoType);
        }

        this.generateCropBlockModel(blockStateModelGenerator);
        this.generateMIBlock(blockStateModelGenerator);
    }

    public void generateCropBlockModel(BlockModelGenerators blockStateModelGenerator) {
        Set<Map.Entry<ResourceLocation, CropBlockCreator.Instance>> views = CropBlockCreator.getViews();
        for (Map.Entry<ResourceLocation, CropBlockCreator.Instance> view : views) {
            ResourceLocation id = null;
            try {
                CropBlockCreator.Instance instance = view.getValue();
                id = instance.getIdentifier();
                AbstractCropBlock cropBlock = instance.getCropBlock();
                CropBlockCreator.ModelType modelType = instance.getModelType();
                IntegerProperty ageProperty = cropBlock.getAgeProperty();
                CropAgeModelProvider provider = instance.getProvider();

                if (modelType == CropBlockCreator.ModelType.CROSS) {
                    blockStateModelGenerator.createCrossBlock(cropBlock, BlockModelGenerators.PlantType.NOT_TINTED, ageProperty, CropAgeUtil.toArray(ageProperty));
                } else if (modelType == CropBlockCreator.ModelType.CROP) {
                    blockStateModelGenerator.createCropBlock(cropBlock, ageProperty, provider.toArray());
                }
            } catch (Exception e) {
                log.error("Can't generate crop block model {}, cause by {}", id, e.getCause());
            }
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        // 调试
        itemModelGenerator.generateFlatItem(ModItems.BATTLE_STICK, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.OWNER_STICK, ModelTemplates.FLAT_HANDHELD_ITEM);

        // 图标
        itemModelGenerator.generateFlatItem(ModItems.ICON, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.FUMO_ICON, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.ROLE_ICON, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateTwoLayerDyedItem(ModItems.SPAWN_EGG);
        itemModelGenerator.generateFlatItem(ModItems.DANMAKU, ModelTemplates.FLAT_ITEM);

        // 材料
        itemModelGenerator.generateFlatItem(ModItems.POINT, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.POWER, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.DANMAKU_CORE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.UPGRADED_HEALTH_FRAGMENT, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.BOMB_FRAGMENT, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.RED_ORB, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.BLUE_ORB, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.YELLOW_ORB, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.GREEN_ORB, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.PURPLE_ORB, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.YIN_YANG_ORB, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.SPEED_FEATHER, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.DREAM_CRYSTAL_FRAGMENT, ModelTemplates.FLAT_ITEM);

        // 道具
        itemModelGenerator.generateFlatItem(ModItems.TOUHOU_HELPER, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.UPGRADED_HEALTH, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.BOMB, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.CROSSING_CHISEL, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.GAP_BALL, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.TIME_STOP_CLOCK, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.EARPHONE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.KOISHI_HAT, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.FUMO_LICENSE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.declareCustomModelItem(ModItems.CURSED_DECOY_DOLl);
        itemModelGenerator.generateFlatItem(ModItems.VAISRAVANAS_PAGODA, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.declareCustomModelItem(ModItems.DREAM_PILLOW);
        itemModelGenerator.declareCustomModelItem(ModItems.TENGU_CAMERA);
        itemModelGenerator.generateFlatItem(ModItems.BAD_APPLE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.EXORCISM_PAPER, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.COPPER_COIN, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.SILVER_COIN, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.GOLD_COIN, ModelTemplates.FLAT_ITEM);

        // 武器
        itemModelGenerator.generateFlatItem(ModItems.HAKUREI_CANE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.declareCustomModelItem(ModItems.BAGUA_FURNACE);
        itemModelGenerator.generateFlatItem(ModItems.WIND_BLESSING_CANE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.declareCustomModelItem(ModItems.MAGIC_BROOM);
        itemModelGenerator.generateFlatItem(ModItems.KNIFE, ModelTemplates.FLAT_HANDHELD_ITEM);
//        itemModelGenerator.register(ModItems.GUNGNIR, Models.HANDHELD);
        itemModelGenerator.generateFlatItem(ModItems.LEVATIN, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.ROKANKEN, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.HAKUROKEN, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.PAPILIO_PATTERN_FAN, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.HORAI_DAMA_NO_EDA, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.IBUKIHO, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.SWORD_OF_HISOU, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.MAPLE_LEAF_FAN, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.declareCustomModelItem(ModItems.MANPOZUCHI);
//        itemModelGenerator.register(ModItems.NUE_TRIDENT);
        itemModelGenerator.declareCustomModelItem(ModItems.TRUMPET_GUN);
        itemModelGenerator.generateFlatItem(ModItems.TREASURE_HUNTING_ROD, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.declareCustomModelItem(ModItems.VIOLIN);
        itemModelGenerator.declareCustomModelItem(ModItems.KEYBOARD);
        itemModelGenerator.declareCustomModelItem(ModItems.TRUMPET);
        itemModelGenerator.generateFlatItem(ModItems.DEATH_SCYTHE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.MAID_HAIRBAND, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.MAID_UPPER_SKIRT, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.MAID_LOWER_SKIRT, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.MAID_SHOE, ModelTemplates.FLAT_ITEM);

        // 工具矿物类
        itemModelGenerator.generateFlatItem(ModItems.RAW_SILVER, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.SILVER_INGOT, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.SILVER_NUGGET, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.SILVER_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.SILVER_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.SILVER_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.SILVER_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.SILVER_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.SILVER_HELMET, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.SILVER_CHESTPLATE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.SILVER_LEGGINGS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.SILVER_BOOTS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.ICE_SCALES, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.MAGIC_ICE_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.MAGIC_ICE_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.MAGIC_ICE_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.MAGIC_ICE_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.MAGIC_ICE_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.MAGIC_ICE_HELMET, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.MAGIC_ICE_CHESTPLATE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.MAGIC_ICE_LEGGINGS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.MAGIC_ICE_BOOTS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.DREAM_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.DREAM_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.DREAM_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.DREAM_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.DREAM_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.DREAM_HELMET, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.DREAM_CHESTPLATE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.DREAM_LEGGINGS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.DREAM_BOOTS, ModelTemplates.FLAT_ITEM);

        // 符卡
        itemModelGenerator.generateFlatItem(ModItems.DANMAKU_SHAPE_CREATOR, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.SPELL_CARD_TEMPLATE, ModelTemplates.FLAT_ITEM);

        // 角色卡
        itemModelGenerator.generateDyedItem(ModItems.ROLE_CARD, RoleCard.DEFAULT_COLOR.intValue());
        itemModelGenerator.generateFlatItem(ModItems.ROLE_ARCHIVE, ModelTemplates.FLAT_ITEM);

        // 唱片
        itemModelGenerator.generateFlatItem(ModItems.HR01_01, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.HR02_08, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.HR03_01, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.YV_FLOWER_CLOCK_AND_DREAMS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.GLOWING_NEEDLES_LITTLE_PEOPLE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.COOKIE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.BADAPPLE, ModelTemplates.FLAT_ITEM);

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

    public void generateMIBlock(BlockModelGenerators blockStateModelGenerator) {
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
        this.registerRotatable(blockStateModelGenerator, MIBlocks.NUKE_COOKING_POT);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.NUKE_CUTTING_BOARD);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.NUKE_FRYING_PAN);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.NUKE_GRILL);
        this.registerRotatable(blockStateModelGenerator, MIBlocks.NUKE_STEAMER);

        blockStateModelGenerator.createNonTemplateModelBlock(MIBlocks.ITEM_DISPLAY);

        blockStateModelGenerator.family(MIBlocks.BLACK_SALT_BLOCK);

        blockStateModelGenerator.createCrossBlockWithDefaultItem(MIBlocks.UDUMBARA_FLOWER, BlockModelGenerators.PlantType.NOT_TINTED);
        blockStateModelGenerator.createCrossBlockWithDefaultItem(MIBlocks.TREMELLA, BlockModelGenerators.PlantType.NOT_TINTED);
    }

    public void generateMIItem(ItemModelGenerators itemModelGenerator) {
        itemModelGenerator.generateFlatItem(MIItems.MYSTIA_ICON, ModelTemplates.FLAT_ITEM);
        for (Item item : MIItems.INGREDIENTS) {
            itemModelGenerator.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }
        for (Item item : MIItems.FOOD_ITEMS) {
            itemModelGenerator.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }
        for (Item item : MIItems.DRINK_ITEMS) {
            itemModelGenerator.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }
    }

    public void generateHolder(ItemModelGenerators itemModelGenerator) {
        ModEntityHolders.HOLDERS.forEach(itemModelGenerator::declareCustomModelItem);
    }

    public void generateGuiItemModels(ItemModelGenerators itemModelGenerator) {
        for (Item item : ModGuiItems.getGuiItemList()) {
            this.registerGuiItem(itemModelGenerator, item);
        }
    }

    public final void registerChest(BlockModelGenerators blockStateModelGenerator, Block block, Block topBottom) {
        TextureMapping textureMap = new TextureMapping()
                .put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "_front"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(topBottom))
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(topBottom));
        blockStateModelGenerator.createPumpkinVariant(
                block,
                textureMap
        );
    }

    public final void registerRotatable(BlockModelGenerators blockStateModelGenerator, Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        ResourceLocation modelId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath());

        Variant modelVariant = new Variant(modelId);

        blockStateModelGenerator.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block, createRotatedVariants(modelVariant))
        );
    }

    private void registerGuiItem(ItemModelGenerators itemModelGenerator, Item item) {
//        Model guiSlotModel = item("custom_slot", TextureKey.LAYER0);
//        itemModelGenerator.register(item, guiSlotModel);
    }

    public void generateDanmakuItemModels(ItemModelGenerators itemModelGenerator) {
        for (Item item : RegistryManager.DANMAKU_TYPE.values()
                .stream()
                .filter(type -> !DanmakuTypes.UNLIST.contains(type))
                .map(DanmakuType::getItem)
                .toList()) {
            itemModelGenerator.generateTwoLayerDyedItem(item);
        }
    }

    private void registerSmithingTable(BlockModelGenerators blockStateModelGenerator, Block block) {
        TextureMapping textureMap = new TextureMapping()
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_front"))
                .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(block, "_bottom"))
                .put(TextureSlot.UP, TextureMapping.getBlockTexture(block, "_top"))
                .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(block, "_front"))
                .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(block, "_front"))
                .put(TextureSlot.EAST, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.WEST, TextureMapping.getBlockTexture(block, "_side"));
        blockStateModelGenerator.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(ModelTemplates.CUBE.create(block, textureMap, blockStateModelGenerator.modelOutput))));

    }

    private void registerFumo(BlockModelGenerators blockStateModelGenerator, Fumo fumoType) {
        Block block = fumoType.block();
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
//        blockStateModelGenerator.blockStateCollector.accept(new BlockModelDefinitionCreator() {
//            @Override
//            public Block getBlock() {
//                return block;
//            }
//
//            @Override
//            public BlockModelDefinition createBlockModelDefinition() {
//                return new BlockModelDefinition();
//            }
//        });
//        VariantsBlockModelDefinitionCreator.Empty creator = VariantsBlockModelDefinitionCreator.of(block);
//
//        for (SixteenDirection sixteenDirection : SixteenDirection.values()) {
//            float yaw = sixteenDirection.getYaw();
//            creator.with(BlockStateVariantMap.models(ModBlockStateTemplates.FACING_16)
//                    .register(sixteenDirection, ));
//        }
//        BlockStateModelGenerator.createSingletonBlockState(block, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(block)));

        blockStateModelGenerator.createNonTemplateModelBlock(block);
    }

    private static ModelTemplate item(String parent, TextureSlot... requiredTextureKeys) {
        return new ModelTemplate(Optional.of(Touhou.id("item/" + parent)), Optional.empty(), requiredTextureKeys);
    }

    private void registerWoodCreator(BlockModelGenerators blockStateModelGenerator, WoodCreator creator) {
        blockStateModelGenerator.woodProvider(creator.log()).logWithHorizontal(creator.log()).wood(creator.wood());
        blockStateModelGenerator.woodProvider(creator.strippedLog()).logWithHorizontal(creator.strippedLog()).wood(creator.strippedWood());
        blockStateModelGenerator.family(creator.leaves());
        blockStateModelGenerator.createCrossBlockWithDefaultItem(creator.sapling(), BlockModelGenerators.PlantType.NOT_TINTED);
        this.registerFamily(blockStateModelGenerator, creator.getBlockFamily());
    }

    private void registerDecorativeBlockCreator(BlockModelGenerators blockStateModelGenerator, DecorativeBlockCreator creator) {
        blockStateModelGenerator.family(creator.block()).stairs(creator.stair()).slab(creator.slab()).wall(creator.wall());
    }

    private void registerFamily(BlockModelGenerators generator, BlockFamily family) {
        TexturedModel texturedModel = this.uniqueModels.getOrDefault(family.getBaseBlock(), TexturedModel.CUBE.get(family.getBaseBlock()));
        generator.new BlockFamilyProvider(texturedModel.getMapping()).fullBlock(family.getBaseBlock(), texturedModel.getTemplate()).generateFor(family);
    }

    @Override
    public String getName() {
        return "Touhou Model Provider";
    }
}
