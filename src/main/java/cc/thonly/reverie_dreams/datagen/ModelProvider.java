package cc.thonly.reverie_dreams.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.armor.WaterproofArmorMaterial;
import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.block.creator.CropBlockCreator;
import cc.thonly.reverie_dreams.block.creator.DecorativeBlockCreator;
import cc.thonly.reverie_dreams.block.creator.WoodCreator;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.item.builder.RoleCard;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.block.KitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.item.*;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static net.minecraft.client.data.models.BlockModelGenerators.createRotatedVariants;

@Slf4j
public class ModelProvider extends FabricModelProvider {
    private final Map<Block, TexturedModel> uniqueModels = ImmutableMap.<Block, TexturedModel>builder()
            .build();

    public ModelProvider(FabricDataOutput output) {
        super(output);
    }

//    private final BlockFamily SPIRITUAL_PLANKS =

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.createCraftingTableLike(RDBlocks.DANMAKU_CRAFTING_TABLE, Blocks.OAK_PLANKS, TextureMapping::craftingTable);
        this.registerSmithingTable(blockStateModelGenerator, RDBlocks.STRENGTH_TABLE);
        blockStateModelGenerator.createNonTemplateModelBlock(RDBlocks.GENSOKYO_ALTAR);
        blockStateModelGenerator.family(RDBlocks.MUSIC_BLOCK);

        this.registerWoodCreator(blockStateModelGenerator, RDWoodBlocks.SPIRITUAL);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, RDBlocks.ICE_SCALES);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, RDBlocks.DREAM_STONE);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, RDBlocks.DREAM_STONE_BRICK);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, RDBlocks.MOON_STONE);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, RDBlocks.MOON_STONE_BRICK);

        this.registerWoodCreator(blockStateModelGenerator, RDWoodBlocks.LEMON);
        blockStateModelGenerator.family(RDWoodBlocks.LEMON_FRUIT_LEAVES);
        this.registerWoodCreator(blockStateModelGenerator, RDWoodBlocks.GINKGO);
        blockStateModelGenerator.family(RDWoodBlocks.GINKGO_FRUIT_LEAVES);
        this.registerWoodCreator(blockStateModelGenerator, RDWoodBlocks.PEACH);
        blockStateModelGenerator.family(RDWoodBlocks.PEACH_FRUIT_LEAVES);

        blockStateModelGenerator.family(RDBlocks.MAGIC_ICE_BLOCK);
        blockStateModelGenerator.family(RDBlocks.POINT_BLOCK);
        blockStateModelGenerator.family(RDBlocks.POWER_BLOCK);
        blockStateModelGenerator.family(RDBlocks.SILVER_ORE);
        blockStateModelGenerator.family(RDBlocks.DREAM_CRYSTAL_ORE);
        blockStateModelGenerator.family(RDBlocks.SILVER_BLOCK);
//        this.registerChest(blockStateModelGenerator, ModBlocks.SILVER_CHEST_BLOCK.chestBlock(), ModBlocks.SILVER_BLOCK);
        blockStateModelGenerator.family(RDBlocks.DEEPSLATE_SILVER_ORE);
        blockStateModelGenerator.family(RDBlocks.ORB_ORE);
        blockStateModelGenerator.family(RDBlocks.DEEPSLATE_ORB_ORE);
        blockStateModelGenerator.family(RDBlocks.RED_ORB_BLOCK);
        blockStateModelGenerator.family(RDBlocks.YELLOW_ORB_BLOCK);
        blockStateModelGenerator.family(RDBlocks.BLUE_ORB_BLOCK);
        blockStateModelGenerator.family(RDBlocks.GREEN_ORB_BLOCK);
        blockStateModelGenerator.family(RDBlocks.PURPLE_ORB_BLOCK);

        blockStateModelGenerator.family(RDBlocks.DREAM_BLUE_BLOCK);
        blockStateModelGenerator.family(RDBlocks.DREAM_RED_BLOCK);
        this.registerRotatable(blockStateModelGenerator, RDBlocks.CASH_BOX_BLOCK);
        blockStateModelGenerator.createNonTemplateModelBlock(RDBlocks.ANTI_COLLISION_BARREL);
        blockStateModelGenerator.createNonTemplateModelBlock(RDBlocks.WHEEL_CHAIR);
        blockStateModelGenerator.createNonTemplateModelBlock(RDBlocks.WOODEN_BOX.chestBlock());

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
                int[] arr = ageProperty.getPossibleValues()
                        .stream()
                        .mapToInt(Integer::intValue)
                        .toArray();

                if (modelType == CropBlockCreator.ModelType.CROSS) {
                    blockStateModelGenerator.createCrossBlock(cropBlock, BlockModelGenerators.PlantType.NOT_TINTED, ageProperty, arr);
                } else if (modelType == CropBlockCreator.ModelType.CROP) {
                    blockStateModelGenerator.createCropBlock(cropBlock, ageProperty, arr);
                }
            } catch (Exception e) {
                log.error("Can't generate crop block model {}, cause by {}", id, e.getCause());
            }
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        // 调试
        itemModelGenerator.generateFlatItem(RDItems.BATTLE_STICK, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.OWNER_STICK, ModelTemplates.FLAT_HANDHELD_ITEM);

        // 图标
        itemModelGenerator.generateFlatItem(RDItems.ICON, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.FUMO_ICON, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.ROLE_ICON, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateItemWithTintedOverlay(RDItems.SPAWN_EGG, new Dye(16777215));
        itemModelGenerator.generateFlatItem(RDItems.DANMAKU, ModelTemplates.FLAT_ITEM);

        // 材料
        itemModelGenerator.generateFlatItem(RDItems.POINT, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.POWER, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DANMAKU_CORE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.UPGRADED_HEALTH_FRAGMENT, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.BOMB_FRAGMENT, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.RED_ORB, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.BLUE_ORB, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.YELLOW_ORB, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.GREEN_ORB, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.PURPLE_ORB, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.YIN_YANG_ORB, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SPEED_FEATHER, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_CRYSTAL_FRAGMENT, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.EMPTY_PHOTO, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.WATERPROOF_LEATHER, ModelTemplates.FLAT_ITEM);

        // 道具
        itemModelGenerator.generateFlatItem(RDItems.TOUHOU_HELPER, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.UPGRADED_HEALTH, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.BOMB, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.CROSSING_CHISEL, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.GAP_BALL, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.TIME_STOP_CLOCK, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.EARPHONE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.KOISHI_HAT, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.FUMO_LICENSE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.CURSED_DECOY_DOLl);
        itemModelGenerator.generateFlatItem(RDItems.VAISRAVANAS_PAGODA, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.DREAM_PILLOW);
        itemModelGenerator.declareCustomModelItem(RDItems.TENGU_CAMERA);
        itemModelGenerator.declareCustomModelItem(RDItems.HIMEKAIDOU_HATATES_PHONE);
        itemModelGenerator.generateFlatItem(RDItems.BAD_APPLE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SCARECROW, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.EXORCISM_PAPER, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.COPPER_COIN, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_COIN, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.GOLD_COIN, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SPELLCARD, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SATORI_EYE, ModelTemplates.FLAT_ITEM);

        // 武器
        itemModelGenerator.generateFlatItem(RDItems.HAKUREI_CANE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.BAGUA_FURNACE);
        itemModelGenerator.generateFlatItem(RDItems.WIND_BLESSING_CANE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.MAGIC_BROOM);
        itemModelGenerator.generateFlatItem(RDItems.KNIFE, ModelTemplates.FLAT_HANDHELD_ITEM);
//        itemModelGenerator.register(ModItems.GUNGNIR, Models.HANDHELD);
        itemModelGenerator.generateFlatItem(RDItems.LEVATIN, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.ROKANKEN, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.HAKUROKEN, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.PAPILIO_PATTERN_FAN, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.HORAI_DAMA_NO_EDA, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.YUKA_FLOWER_UMBRELLA);
        itemModelGenerator.generateFlatItem(RDItems.IBUKIHO, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SWORD_OF_HISOU, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAPLE_LEAF_FAN, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.MANPOZUCHI);
//        itemModelGenerator.register(ModItems.NUE_TRIDENT);
        itemModelGenerator.declareCustomModelItem(RDItems.TRUMPET_GUN);
        itemModelGenerator.generateFlatItem(RDItems.TREASURE_HUNTING_ROD, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.VIOLIN);
        itemModelGenerator.declareCustomModelItem(RDItems.KEYBOARD);
        itemModelGenerator.declareCustomModelItem(RDItems.TRUMPET);
        itemModelGenerator.generateFlatItem(RDItems.DEATH_SCYTHE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAID_HAIRBAND, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAID_UPPER_SKIRT, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAID_LOWER_SKIRT, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAID_SHOE, ModelTemplates.FLAT_ITEM);

        // 工具矿物类
        itemModelGenerator.generateFlatItem(RDItems.RAW_SILVER, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_INGOT, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_NUGGET, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_HELMET, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_CHESTPLATE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_LEGGINGS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_BOOTS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.ICE_SCALES, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_HELMET, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_CHESTPLATE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_LEGGINGS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_BOOTS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_HELMET, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_CHESTPLATE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_LEGGINGS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_BOOTS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateTrimmableItem(RDItems.WATER_PROOF_HAT, WaterproofArmorMaterial.REGISTRY_KEY, ItemModelGenerators.SLOT_HELMET, true);
        itemModelGenerator.generateTrimmableItem(RDItems.WATER_PROOF_CLOTHING, WaterproofArmorMaterial.REGISTRY_KEY, ItemModelGenerators.SLOT_CHESTPLATE, true);
        itemModelGenerator.generateTrimmableItem(RDItems.WATER_PROOF_LEGGINGS, WaterproofArmorMaterial.REGISTRY_KEY, ItemModelGenerators.SLOT_LEGGINS, true);
        itemModelGenerator.generateTrimmableItem(RDItems.WATER_PROOF_BOOTS, WaterproofArmorMaterial.REGISTRY_KEY, ItemModelGenerators.SLOT_BOOTS, true);

        // 符卡
        itemModelGenerator.generateFlatItem(RDItems.DANMAKU_SHAPE_CREATOR, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SPELL_CARD_TEMPLATE, ModelTemplates.FLAT_ITEM);

        // 角色卡
        itemModelGenerator.generateDyedItem(RDItems.ROLE_CARD, RoleCard.DEFAULT_COLOR.intValue());
        itemModelGenerator.generateFlatItem(RDItems.ROLE_ARCHIVE, ModelTemplates.FLAT_ITEM);

        // 唱片
        itemModelGenerator.generateFlatItem(RDItems.HR01_01, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.HR02_08, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.HR03_01, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.YV_FLOWER_CLOCK_AND_DREAMS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.GLOWING_NEEDLES_LITTLE_PEOPLE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.COOKIE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.BADAPPLE, ModelTemplates.FLAT_ITEM);

        this.generateGuiItemModels(itemModelGenerator);
        this.generateDanmakuItemModels(itemModelGenerator);
        this.generateHolder(itemModelGenerator);
        this.generateMIItem(itemModelGenerator);
    }

    public void generateMIBlock(BlockModelGenerators blockStateModelGenerator) {
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.COOKING_POT);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.CUTTING_BOARD);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.FRYING_PAN);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.GRILL);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.STEAMER);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.MYSTIA_COOKING_POT);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.MYSTIA_CUTTING_BOARD);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.MYSTIA_FRYING_PAN);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.MYSTIA_GRILL);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.MYSTIA_STEAMER);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.SUPER_COOKING_POT);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.SUPER_CUTTING_BOARD);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.SUPER_FRYING_PAN);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.SUPER_GRILL);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.SUPER_STEAMER);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.EXTREME_COOKING_POT);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.EXTREME_CUTTING_BOARD);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.EXTREME_FRYING_PAN);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.EXTREME_GRILL);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.EXTREME_STEAMER);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.NUKE_COOKING_POT);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.NUKE_CUTTING_BOARD);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.NUKE_FRYING_PAN);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.NUKE_GRILL);
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.NUKE_STEAMER);

        blockStateModelGenerator.createNonTemplateModelBlock(RDBlocks.ITEM_DISPLAY);

        blockStateModelGenerator.family(RDBlocks.BLACK_SALT_BLOCK);

        blockStateModelGenerator.createCrossBlockWithDefaultItem(RDWoodBlocks.UDUMBARA_FLOWER, BlockModelGenerators.PlantType.NOT_TINTED);
        blockStateModelGenerator.createCrossBlockWithDefaultItem(RDWoodBlocks.TREMELLA, BlockModelGenerators.PlantType.NOT_TINTED);
    }

    public void generateMIItem(ItemModelGenerators itemModelGenerator) {
        itemModelGenerator.generateFlatItem(RDItems.MYSTIA_ICON, ModelTemplates.FLAT_ITEM);
        for (Item item : RDIngredientItems.INGREDIENTS) {
            itemModelGenerator.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }
        for (Item item : RDFoodItems.FOOD_ITEMS) {
            itemModelGenerator.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }
        for (Item item : RDDrinkItems.DRINK_ITEMS) {
            itemModelGenerator.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }
    }

    public void generateHolder(ItemModelGenerators itemModelGenerator) {
        RDEntityHolderItems.HOLDERS.forEach(itemModelGenerator::declareCustomModelItem);
    }

    public void generateGuiItemModels(ItemModelGenerators itemModelGenerator) {
        for (Item item : RDGuiItems.getGuiItemList()) {
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

        blockStateModelGenerator.blockStateOutput.accept(BlockModelGenerators.createRotatedVariant(block, modelId));
    }

    private void registerGuiItem(ItemModelGenerators itemModelGenerator, Item item) {
//        Model guiSlotModel = item("custom_slot", TextureKey.LAYER0);
//        itemModelGenerator.register(item, guiSlotModel);
    }

    public void generateDanmakuItemModels(ItemModelGenerators itemModelGenerator) {
        for (Item item : RegistryHandlers.DANMAKU_TYPE.values()
                .stream()
                .filter(type -> !type.isDeleteFromList())
                .map(DanmakuType::getItem)
                .toList()) {
            generateTwoLayerDyedItem(itemModelGenerator, item);
        }
    }

    public final void generateTwoLayerDyedItem(ItemModelGenerators itemModelGenerator, Item item) {
        ResourceLocation resourceLocation = TextureMapping.getItemTexture(item);
        ResourceLocation resourceLocation2 = TextureMapping.getItemTexture(item, "_overlay");
        ResourceLocation resourceLocation3 = ModelTemplates.FLAT_ITEM.create(item, TextureMapping.layer0(resourceLocation), itemModelGenerator.modelOutput);
        ResourceLocation resourceLocation4 = ModelLocationUtils.getModelLocation(item, "_dyed");
        ModelTemplates.TWO_LAYERED_ITEM.create(resourceLocation4, TextureMapping.layered(resourceLocation, resourceLocation2), itemModelGenerator.modelOutput);
        itemModelGenerator.itemModelOutput.accept(item, ItemModelUtils.conditional(ItemModelUtils.hasComponent(DataComponents.DYED_COLOR), ItemModelUtils.tintedModel(resourceLocation4, new ItemTintSource[]{ItemModelGenerators.BLANK_LAYER, new Dye(0)}), ItemModelUtils.plainModel(resourceLocation3)));
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
        blockStateModelGenerator.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, ModelTemplates.CUBE.create(block, textureMap, blockStateModelGenerator.modelOutput)));

    }

    private void registerFumo(BlockModelGenerators blockStateModelGenerator, FumoType fumoType) {
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
        return new ModelTemplate(Optional.of(ReverieDreams.id("item/" + parent)), Optional.empty(), requiredTextureKeys);
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
