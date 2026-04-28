package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.armor.WaterproofArmorMaterial;
import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.block.bundle.DecorativeBlockBundle;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.block.kitchen.AbstractKitchenwareBlock;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.item.builder.RoleCard;
import cc.thonly.reverie_dreams.mixin.accessor.BlockModelGeneratorsAccessor;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.block.KitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.item.*;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jspecify.annotations.NonNull;

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

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.createCraftingTableLike(RDBlocks.DANMAKU_CRAFTING_TABLE.asBlock(), Blocks.OAK_PLANKS, TextureMapping::craftingTable);
        this.registerSmithingTable(blockStateModelGenerator, RDBlocks.STRENGTH_TABLE.asBlock());
        blockStateModelGenerator.createNonTemplateModelBlock(RDBlocks.GENSOKYO_ALTAR.asBlock());
        blockStateModelGenerator.family(RDBlocks.MUSIC_BLOCK.asBlock());

        this.registerWoodBundle(blockStateModelGenerator, RDWoodBlocks.SPIRITUAL_BUNDLE);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, RDBlocks.ICE_SCALES);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, RDBlocks.DREAM_STONE);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, RDBlocks.DREAM_STONE_BRICK);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, RDBlocks.MOON_STONE);
        this.registerDecorativeBlockCreator(blockStateModelGenerator, RDBlocks.MOON_STONE_BRICK);

        this.registerWoodBundle(blockStateModelGenerator, RDWoodBlocks.LEMON_BUNDLE);
        blockStateModelGenerator.family(RDWoodBlocks.LEMON_FRUIT_LEAVES.asBlock());
        this.registerWoodBundle(blockStateModelGenerator, RDWoodBlocks.GINKGO_BUNDLE);
        blockStateModelGenerator.family(RDWoodBlocks.GINKGO_FRUIT_LEAVES.asBlock());
        this.registerWoodBundle(blockStateModelGenerator, RDWoodBlocks.PEACH_BUNDLE);
        blockStateModelGenerator.family(RDWoodBlocks.PEACH_FRUIT_LEAVES.asBlock());

        blockStateModelGenerator.family(RDBlocks.MAGIC_ICE_BLOCK.asBlock());
        blockStateModelGenerator.family(RDBlocks.POINT_BLOCK.asBlock());
        blockStateModelGenerator.family(RDBlocks.POWER_BLOCK.asBlock());
        blockStateModelGenerator.family(RDBlocks.SILVER_ORE.asBlock());
        blockStateModelGenerator.family(RDBlocks.DREAM_CRYSTAL_ORE.asBlock());
        blockStateModelGenerator.family(RDBlocks.SILVER_BLOCK.asBlock());
//        this.registerChest(blockStateModelGenerator, ModBlocks.SILVER_CHEST_BLOCK.chestBlock(), ModBlocks.SILVER_BLOCK);
        blockStateModelGenerator.family(RDBlocks.DEEPSLATE_SILVER_ORE.asBlock());
        blockStateModelGenerator.family(RDBlocks.ORB_ORE.asBlock());
        blockStateModelGenerator.family(RDBlocks.DEEPSLATE_ORB_ORE.asBlock());
        blockStateModelGenerator.family(RDBlocks.RED_ORB_BLOCK.asBlock());
        blockStateModelGenerator.family(RDBlocks.YELLOW_ORB_BLOCK.asBlock());
        blockStateModelGenerator.family(RDBlocks.BLUE_ORB_BLOCK.asBlock());
        blockStateModelGenerator.family(RDBlocks.GREEN_ORB_BLOCK.asBlock());
        blockStateModelGenerator.family(RDBlocks.PURPLE_ORB_BLOCK.asBlock());

        blockStateModelGenerator.family(RDBlocks.DREAM_BLUE_BLOCK.asBlock());
        blockStateModelGenerator.family(RDBlocks.DREAM_RED_BLOCK.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDBlocks.CASH_BOX_BLOCK.asBlock());
        blockStateModelGenerator.createNonTemplateModelBlock(RDBlocks.ANTI_COLLISION_BARREL.asBlock());
        blockStateModelGenerator.createNonTemplateModelBlock(RDBlocks.WHEEL_CHAIR.asBlock());
        blockStateModelGenerator.createNonTemplateModelBlock(RDBlocks.WOODEN_BOX.chestBlock().asBlock());

        this.generateCropBlockModel(blockStateModelGenerator);
        this.generateKitchenBlock(blockStateModelGenerator);
    }

    public void generateCropBlockModel(BlockModelGenerators blockStateModelGenerator) {
        Set<Map.Entry<Identifier, CropBlockBundle.Entry>> views = CropBlockBundle.getViews();
        for (Map.Entry<Identifier, CropBlockBundle.Entry> view : views) {
            Identifier id = null;
            try {
                CropBlockBundle.Entry entry = view.getValue();
                id = entry.getIdentifier();
                AbstractCropBlock cropBlock = (AbstractCropBlock) entry.getCropBlock().asBlock();
                CropBlockBundle.ModelType modelType = entry.getModelType();
                IntegerProperty ageProperty = cropBlock.getAgeProperty();
                int[] arr = ageProperty.getPossibleValues()
                        .stream()
                        .mapToInt(Integer::intValue)
                        .toArray();

                if (modelType == CropBlockBundle.ModelType.CROSS) {
                    blockStateModelGenerator.createCrossBlock(cropBlock, BlockModelGenerators.PlantType.NOT_TINTED, ageProperty, arr);
                } else if (modelType == CropBlockBundle.ModelType.CROP) {
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
        itemModelGenerator.generateFlatItem(RDItems.BATTLE_STICK.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.OWNER_STICK.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);

        // 图标
        itemModelGenerator.generateFlatItem(RDItems.ICON.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.FUMO_ICON.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.ROLE_ICON.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateTwoLayerDyedItem(RDItems.SPAWN_EGG.asItem());
        itemModelGenerator.generateFlatItem(RDItems.DANMAKU.asItem(), ModelTemplates.FLAT_ITEM);

        // 材料
        itemModelGenerator.generateFlatItem(RDItems.POINT.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.POWER.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DANMAKU_CORE.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.UPGRADED_HEALTH_FRAGMENT.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.BOMB_FRAGMENT.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.RED_ORB.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.BLUE_ORB.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.YELLOW_ORB.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.GREEN_ORB.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.PURPLE_ORB.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.YIN_YANG_ORB.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SPEED_FEATHER.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_CRYSTAL_FRAGMENT.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.EMPTY_PHOTO.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.WATERPROOF_LEATHER.asItem(), ModelTemplates.FLAT_ITEM);

        // 道具
        itemModelGenerator.generateFlatItem(RDItems.GUIDEBOOK.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.UPGRADED_HEALTH.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.BOMB.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.CROSSING_CHISEL.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.GAP_BALL.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.TIME_STOP_CLOCK.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.EARPHONE.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.KOISHI_HAT.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.FUMO_LICENSE.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.CURSED_DECOY_DOLl.asItem());
        itemModelGenerator.generateFlatItem(RDItems.VAISRAVANAS_PAGODA.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.DREAM_PILLOW.asItem());
        itemModelGenerator.declareCustomModelItem(RDItems.TENGU_CAMERA.asItem());
        itemModelGenerator.declareCustomModelItem(RDItems.HIMEKAIDOU_HATATES_PHONE.asItem());
        itemModelGenerator.generateFlatItem(RDItems.BAD_APPLE.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SCARECROW.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.EXORCISM_PAPER.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.COPPER_COIN.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_COIN.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.GOLD_COIN.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SPELLCARD.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SATORI_EYE.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.WEAPON_OF_THE_MOON.asItem());

        // 武器
        itemModelGenerator.generateFlatItem(RDItems.HAKUREI_CANE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.BAGUA_FURNACE.asItem());
        itemModelGenerator.generateFlatItem(RDItems.WIND_BLESSING_CANE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.MAGIC_BROOM.asItem());
        itemModelGenerator.generateFlatItem(RDItems.KNIFE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
//        itemModelGenerator.generateSpear(RDItems.GUNGNIR);
        itemModelGenerator.generateFlatItem(RDItems.LEVATIN.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.ROKANKEN.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.HAKUROKEN.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.PAPILIO_PATTERN_FAN.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.HORAI_DAMA_NO_EDA.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.YUKA_FLOWER_UMBRELLA.asItem());
        itemModelGenerator.generateFlatItem(RDItems.IBUKIHO.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SWORD_OF_HISOU.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAPLE_LEAF_FAN.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.MANPOZUCHI.asItem());
//        itemModelGenerator.register(ModItems.NUE_TRIDENT);
        itemModelGenerator.declareCustomModelItem(RDItems.TRUMPET_GUN.asItem());
        itemModelGenerator.generateFlatItem(RDItems.TREASURE_HUNTING_ROD.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.VIOLIN.asItem());
        itemModelGenerator.declareCustomModelItem(RDItems.KEYBOARD.asItem());
        itemModelGenerator.declareCustomModelItem(RDItems.TRUMPET.asItem());
        itemModelGenerator.generateFlatItem(RDItems.DEATH_SCYTHE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAID_HAIRBAND.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAID_UPPER_SKIRT.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAID_LOWER_SKIRT.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAID_SHOE.asItem(), ModelTemplates.FLAT_ITEM);

        // 工具矿物类
        itemModelGenerator.generateFlatItem(RDItems.RAW_SILVER.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_INGOT.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_NUGGET.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_SWORD.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_AXE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_PICKAXE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_SHOVEL.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_HOE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateSpear(RDItems.SILVER_SPEAR.asItem());
        itemModelGenerator.generateFlatItem(RDItems.SILVER_HELMET.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_CHESTPLATE.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_LEGGINGS.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_BOOTS.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.ICE_SCALES.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_SWORD.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_AXE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_PICKAXE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_SHOVEL.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_HOE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateSpear(RDItems.MAGIC_ICE_SPEAR.asItem());
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_HELMET.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_CHESTPLATE.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_LEGGINGS.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAGIC_ICE_BOOTS.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_SWORD.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_AXE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_PICKAXE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_SHOVEL.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_HOE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateSpear(RDItems.DREAM_SPEAR.asItem());
        itemModelGenerator.generateFlatItem(RDItems.DREAM_HELMET.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_CHESTPLATE.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_LEGGINGS.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_BOOTS.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateTrimmableItem(RDItems.WATER_PROOF_HAT.asItem(), WaterproofArmorMaterial.REGISTRY_KEY, ItemModelGenerators.TRIM_PREFIX_HELMET, true);
        itemModelGenerator.generateTrimmableItem(RDItems.WATER_PROOF_CLOTHING.asItem(), WaterproofArmorMaterial.REGISTRY_KEY, ItemModelGenerators.TRIM_PREFIX_CHESTPLATE, true);
        itemModelGenerator.generateTrimmableItem(RDItems.WATER_PROOF_LEGGINGS.asItem(), WaterproofArmorMaterial.REGISTRY_KEY, ItemModelGenerators.TRIM_PREFIX_LEGGINGS, true);
        itemModelGenerator.generateTrimmableItem(RDItems.WATER_PROOF_BOOTS.asItem(), WaterproofArmorMaterial.REGISTRY_KEY, ItemModelGenerators.TRIM_PREFIX_BOOTS, true);

        // 符卡
        itemModelGenerator.generateFlatItem(RDItems.DANMAKU_SHAPE_CREATOR.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SPELL_CARD_TEMPLATE.asItem(), ModelTemplates.FLAT_ITEM);

        // 角色卡
        itemModelGenerator.generateDyedItem(RDItems.ROLE_CARD.asItem(), RoleCard.DEFAULT_COLOR.intValue());
        itemModelGenerator.generateFlatItem(RDItems.ROLE_ARCHIVE.asItem(), ModelTemplates.FLAT_ITEM);

        // 唱片
        itemModelGenerator.generateFlatItem(RDItems.HR01_01.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.HR02_08.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.HR03_01.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.YV_FLOWER_CLOCK_AND_DREAMS.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.GLOWING_NEEDLES_LITTLE_PEOPLE.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.COOKIE.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.BADAPPLE.asItem(), ModelTemplates.FLAT_ITEM);

        this.generateGuiItemModels(itemModelGenerator);
        this.generateDanmakuItemModels(itemModelGenerator);
        this.generateHolder(itemModelGenerator);
        this.generateMystiaItem(itemModelGenerator);
    }

    public void generateKitchenBlock(BlockModelGenerators blockStateModelGenerator) {
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.COOKING_POT.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.CUTTING_BOARD.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.FRYING_PAN.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.GRILL.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.STEAMER.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.MYSTIA_COOKING_POT.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.MYSTIA_CUTTING_BOARD.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.MYSTIA_FRYING_PAN.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.MYSTIA_GRILL.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.MYSTIA_STEAMER.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.SUPER_COOKING_POT.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.SUPER_CUTTING_BOARD.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.SUPER_FRYING_PAN.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.SUPER_GRILL.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.SUPER_STEAMER.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.EXTREME_COOKING_POT.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.EXTREME_CUTTING_BOARD.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.EXTREME_FRYING_PAN.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.EXTREME_GRILL.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.EXTREME_STEAMER.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.NUKE_COOKING_POT.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.NUKE_CUTTING_BOARD.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.NUKE_FRYING_PAN.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.NUKE_GRILL.asBlock());
        this.registerRotatable(blockStateModelGenerator, KitchenBlocks.NUKE_STEAMER.asBlock());

        blockStateModelGenerator.createNonTemplateModelBlock(RDBlocks.FOOD_DISPLAY.asBlock());

        blockStateModelGenerator.family(RDBlocks.BLACK_SALT_BLOCK.asBlock());

        blockStateModelGenerator.createCrossBlockWithDefaultItem(RDWoodBlocks.UDUMBARA_FLOWER.asBlock(), BlockModelGenerators.PlantType.NOT_TINTED);
        blockStateModelGenerator.createCrossBlockWithDefaultItem(RDWoodBlocks.TREMELLA.asBlock(), BlockModelGenerators.PlantType.NOT_TINTED);
    }

    public void generateMystiaItem(ItemModelGenerators itemModelGenerator) {
        itemModelGenerator.generateFlatItem(RDItems.MYSTIA_ICON.asItem(), ModelTemplates.FLAT_ITEM);
        for (DeferredItem item : RDIngredientItems.INGREDIENTS) {
            itemModelGenerator.generateFlatItem(item.asItem(), ModelTemplates.FLAT_ITEM);
        }
        for (DeferredItem item : RDFoodItems.FOOD_ITEMS) {
            itemModelGenerator.generateFlatItem(item.asItem(), ModelTemplates.FLAT_ITEM);
        }
        for (DeferredItem item : RDDrinkItems.DRINK_ITEMS) {
            itemModelGenerator.generateFlatItem(item.asItem(), ModelTemplates.FLAT_ITEM);
        }
    }

    public void generateHolder(ItemModelGenerators itemModelGenerator) {
        RDEntityHolderItems.HOLDERS.stream().map(ItemLike::asItem).forEach(itemModelGenerator::declareCustomModelItem);
    }

    public void generateGuiItemModels(ItemModelGenerators itemModelGenerator) {
        for (Holder<Item> item : RDGuiItems.getGuiItemList()) {
            this.registerGuiItem(itemModelGenerator, item.value());
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
//        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
//        Identifier modelId = Identifier.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath());
//
//        Variant modelVariant = new Variant(modelId);
        blockStateModelGenerator
                .blockStateOutput
                .accept(MultiVariantGenerator
                        .dispatch(block, BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(block)))
                        .with(BlockModelGeneratorsAccessor.getRotationHorizontalFacing())
                );
//        blockStateModelGenerator.blockStateOutput.accept(
//                MultiVariantGenerator.multiVariant(block)
//                        .with(
//                                PropertyDispatch.property(AbstractKitchenwareBlock.FACING)
//                                        .select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, modelId))
//                                        .select(Direction.SOUTH, Variant.variant()
//                                                .with(VariantProperties.MODEL, modelId)
//                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
//                                        .select(Direction.WEST, Variant.variant()
//                                                .with(VariantProperties.MODEL, modelId)
//                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
//                                        .select(Direction.EAST, Variant.variant()
//                                                .with(VariantProperties.MODEL, modelId)
//                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
//                        )
//        );
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
                .map(ItemLike::asItem)
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

    private void registerFumo(BlockModelGenerators blockStateModelGenerator, FumoType fumoType) {
        Block block = fumoType.block();
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
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

    private void registerWoodBundle(BlockModelGenerators generator, WoodBundle bundle) {

        // ===== LOG / WOOD =====
        generator.woodProvider(bundle.log().asBlock())
                .logWithHorizontal(bundle.log().asBlock())
                .wood(bundle.wood().asBlock());

        generator.woodProvider(bundle.strippedLog().asBlock())
                .logWithHorizontal(bundle.strippedLog().asBlock())
                .wood(bundle.strippedWood().asBlock());

        // ===== LEAVES =====
        generator.createTintedLeaves(bundle.leaves().asBlock(), TexturedModel.LEAVES, -12012264);

        // ===== SAPLING =====
        generator.createCrossBlockWithDefaultItem(
                bundle.sapling().asBlock(),
                BlockModelGenerators.PlantType.NOT_TINTED
        );

        // ===== 木板家族（核心）=====
        BlockFamily family = new BlockFamily.Builder(bundle.planks().asBlock())
                .stairs(bundle.stairs().asBlock())
                .slab(bundle.slab().asBlock())
                .fence(bundle.fence().asBlock())
                .fenceGate(bundle.fenceGate().asBlock())
                .button(bundle.button().asBlock())
                .door(bundle.door().asBlock())
                .trapdoor(bundle.trapdoor().asBlock())
                .getFamily();

        generator.family(bundle.planks().asBlock()).generateFor(family);
    }

    private void registerDecorativeBlockCreator(BlockModelGenerators blockStateModelGenerator, DecorativeBlockBundle creator) {
        blockStateModelGenerator.family(creator.block().asBlock()).stairs(creator.stair().asBlock()).slab(creator.slab().asBlock()).wall(creator.wall().asBlock());
    }

    private void registerFamily(BlockModelGenerators generator, BlockFamily family) {
        TexturedModel texturedModel = this.uniqueModels.getOrDefault(family.getBaseBlock(), TexturedModel.CUBE.get(family.getBaseBlock()));
        generator.new BlockFamilyProvider(texturedModel.getMapping()).fullBlock(family.getBaseBlock(), texturedModel.getTemplate()).generateFor(family);
    }

    @Override
    public @NonNull String getName() {
        return "Touhou Model Provider";
    }
}
