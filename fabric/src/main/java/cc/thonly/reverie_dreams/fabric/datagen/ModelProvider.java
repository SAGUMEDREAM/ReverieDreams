package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.item.AncientIronEquipment;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.board.BoardKind;
import cc.thonly.reverie_dreams.block.decor.RecordPlayerBlock;
import cc.thonly.reverie_dreams.board.BoardPosition;
import cc.thonly.reverie_dreams.entity.aquatic.AquaticSpecies;
import cc.thonly.reverie_dreams.armor.WaterproofArmorMaterial;
import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.block.nature.PuffYoBushBlock;
import cc.thonly.reverie_dreams.block.decor.TatamiBlock;
import cc.thonly.reverie_dreams.block.bundle.DecorativeBlockBundle;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.block.redstone.RemoteClientBlock;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.item.base.RoleCard;
import cc.thonly.reverie_dreams.mixin.accessor.BlockModelGeneratorsAccessor;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseHomeBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDKitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDMagicForestBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoolBlocks;
import cc.thonly.reverie_dreams.registry.content.item.*;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import cc.thonly.reverie_dreams.api.registry.block.CuisineBlockBindings;
import java.util.Locale;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.world.item.ItemDisplayContext;

import static net.minecraft.client.data.models.BlockModelGenerators.*;
import static net.minecraft.client.data.models.BlockModelGenerators.Y_ROT_90;
import static net.minecraft.client.data.models.BlockModelGenerators.plainVariant;

@Slf4j
public class ModelProvider extends FabricModelProvider {
    private final PackOutput.PathProvider displayItemPaths;
    private final FabricPackOutput packOutput;
    private final Map<Block, TexturedModel> uniqueModels = ImmutableMap.<Block, TexturedModel>builder()
            .build();

    public ModelProvider(FabricPackOutput output) {
        super(output);
        packOutput = output;
        displayItemPaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        // ItemModelOutput is keyed by registered Item. Standalone ITEM_MODEL overrides
        // have independent IDs, so save typed ClientItems through this same provider/cache.
        var standaloneModels = boardDisplayModels();
        standaloneModels.putAll(SpellcardItemModels.standaloneModels());
        return CompletableFuture.allOf(super.run(output),
                DataProvider.saveAll(output, ClientItem.CODEC, displayItemPaths, standaloneModels));
    }

    private static Map<Identifier, ClientItem> boardDisplayModels() {
        Map<Identifier, ClientItem> models = new LinkedHashMap<>();
        for (BoardKind kind : BoardKind.values()) {
            models.put(ReverieDreams.id("board/" + kind.id), new ClientItem(
                    ItemModelUtils.plainModel(ReverieDreams.id("block/" + kind.id)), ClientItem.Properties.DEFAULT));
            // Read piece identities from the rules' initial position; Go starts empty
            // and uses the shared PAWN identity for its single kind of stone.
            BoardPosition position = new BoardPosition(kind);
            Set<Integer> types = new TreeSet<>(Set.of(BoardPosition.PAWN));
            for (int square = 0; square < position.size(); square++) {
                if (position.at(square) != 0) types.add(Math.abs(position.at(square)));
            }
            for (String side : List.of("first", "second")) for (int type : types) {
                Identifier id = ReverieDreams.id("board/piece/" + kind.name().toLowerCase(Locale.ROOT)
                        + "_" + side + "_" + type);
                models.put(id, new ClientItem(ItemModelUtils.plainModel(id), ClientItem.Properties.DEFAULT));
            }
        }
        return models;
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators output) {
        BlockModelGenerators blockStateModelGenerator = new BlockModelGenerators(output.blockStateOutput,
                DecorationItemDisplays.wrap(output.itemModelOutput), output.modelOutput);
        registerWoolFamilies(blockStateModelGenerator);
        MagicMushroomModels.generate(blockStateModelGenerator);
        for (var entry : cc.thonly.reverie_dreams.world.sapling.GensokyoSaplings.entries()) {
            var sapling = cc.thonly.reverie_dreams.registry.content.block.RDGensokyoSaplings.SAPLINGS.get(entry.forest()).asBlock();
            // Both stages use the vanilla block model and its resource-pack-aware textures.
            blockStateModelGenerator.blockStateOutput.accept(MultiVariantGenerator.dispatch(sapling,
                    plainVariant(ModelLocationUtils.getModelLocation(entry.appearance()))));
            blockStateModelGenerator.registerSimpleItemModel(sapling,
                    ModelLocationUtils.getModelLocation(entry.appearance().asItem()));
        }
        DeadwoodModels.generate(blockStateModelGenerator);
        LandmarkModels.generate(blockStateModelGenerator);
        LargeBambooData.models(blockStateModelGenerator);
        BambooForestData.models(blockStateModelGenerator);
        for (var binding : CuisineBlockBindings.bindings()) {
            var id = binding.block().unwrapKey().orElseThrow().identifier();
            blockStateModelGenerator.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(binding.block().value(),
                            plainVariant(ReverieDreams.id("block/" + id.getPath()))));
        }
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
        blockStateModelGenerator.family(RDWoodBlocks.PLUM_FRUIT_LEAVES.asBlock());
        blockStateModelGenerator.createCrossBlockWithDefaultItem(RDWoodBlocks.PLUM_SAPLING.asBlock(), BlockModelGenerators.PlantType.NOT_TINTED);
        blockStateModelGenerator.family(RDWoodBlocks.PINE_FRUIT_LEAVES.asBlock());
        blockStateModelGenerator.createCrossBlockWithDefaultItem(RDWoodBlocks.PINE_SAPLING.asBlock(), BlockModelGenerators.PlantType.NOT_TINTED);
        blockStateModelGenerator.family(RDWoodBlocks.CHESTNUT_FRUIT_LEAVES.asBlock());
        blockStateModelGenerator.createCrossBlockWithDefaultItem(RDWoodBlocks.CHESTNUT_SAPLING.asBlock(), BlockModelGenerators.PlantType.NOT_TINTED);
        this.registerWoodBundle(blockStateModelGenerator, RDWoodBlocks.IROHA_MAPLE_BUNDLE);
        this.registerWoodBundle(blockStateModelGenerator, RDWoodBlocks.ITAYA_MAPLE_BUNDLE);
        this.registerWoodBundle(blockStateModelGenerator, RDWoodBlocks.HAUCHIWA_MAPLE_BUNDLE);

        blockStateModelGenerator.family(RDBlocks.MAGIC_ICE_BLOCK.asBlock());
        blockStateModelGenerator.family(RDBlocks.POINT_BLOCK.asBlock());
        blockStateModelGenerator.family(RDBlocks.POWER_BLOCK.asBlock());
        blockStateModelGenerator.family(RDBlocks.SILVER_ORE.asBlock());
        blockStateModelGenerator.family(RDBlocks.MOON_IRON_ORE.asBlock());
        blockStateModelGenerator.family(RDBlocks.MOON_GOLD_ORE.asBlock());
        blockStateModelGenerator.family(RDBlocks.MOON_DIAMOND_ORE.asBlock());
        blockStateModelGenerator.family(RDBlocks.MOON_QUARTZ_ORE.asBlock());
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

        this.createControllerRails(blockStateModelGenerator, RDBlocks.RAIL_CONTROLLER_BLOCK.asBlock());
        this.createSignalRails(blockStateModelGenerator, RDBlocks.SIGNAL_RAIL_BLOCK.asBlock());
        this.createSignalDelayer(blockStateModelGenerator, RDBlocks.SIGNAL_DELAYER_BLOCK.asBlock(), TexturedModel.ORIENTABLE_ONLY_TOP);
        this.createSignalCS(blockStateModelGenerator, RDBlocks.REMOTE_CLIENT.asBlock());
        this.createSignalCS(blockStateModelGenerator, RDBlocks.REMOTE_SERVER.asBlock());
        this.createSignalCS(blockStateModelGenerator, RDBlocks.SPEAKER.asBlock());
        Identifier moistFarmland = Identifier.withDefaultNamespace("block/farmland_moist");
        blockStateModelGenerator.blockStateOutput.accept(MultiVariantGenerator.dispatch(
                RDBlocks.PADDY_FIELD.asBlock(), plainVariant(moistFarmland)));
        blockStateModelGenerator.registerSimpleItemModel(RDBlocks.PADDY_FIELD.asBlock(), moistFarmland);
        for (Block block : List.of(RDBlocks.MYSTIA_BELL.asBlock(), RDBlocks.MENU_BOOK.asBlock())) {
            Identifier base = ModelLocationUtils.getModelLocation(block);
            blockStateModelGenerator.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                    .with(PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.OPEN).generate((facing, open) -> {
                        MultiVariant variant = plainVariant(open ? base.withPath(base.getPath() + "_open") : base);
                        return switch (facing) {
                            case EAST -> variant.with(Y_ROT_90);
                            case SOUTH -> variant.with(Y_ROT_180);
                            case WEST -> variant.with(Y_ROT_270);
                            default -> variant;
                        };
                    })));
            blockStateModelGenerator.registerSimpleItemModel(block, base);
        }

        var recordPlayer = RDBlocks.RECORD_PLAYER.asBlock();
        var recordModel = ReverieDreams.id("block/record_player");
        blockStateModelGenerator.blockStateOutput.accept(MultiVariantGenerator.dispatch(recordPlayer)
                .with(PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, RecordPlayerBlock.PLAYING).generate((facing, playing) -> {
                    MultiVariant variant = plainVariant(playing ? ReverieDreams.id("block/record_player_on") : recordModel);
                    return switch (facing) {
                        case EAST -> variant.with(Y_ROT_90);
                        case SOUTH -> variant.with(Y_ROT_180);
                        case WEST -> variant.with(Y_ROT_270);
                        default -> variant;
                    };
                })));
        blockStateModelGenerator.registerSimpleItemModel(recordPlayer, ReverieDreams.id("item/decoration/record_player"));

        DreamBlockModels.generate(blockStateModelGenerator);
        this.generateCropBlockModel(blockStateModelGenerator);
        this.generateKitchenBlock(blockStateModelGenerator);
        this.generateJapaneseDecorBlockAsset(blockStateModelGenerator);
        this.generateJapaneseExteriorBlockAsset(blockStateModelGenerator);
        JapaneseShrineModels.generate(blockStateModelGenerator);
        JapaneseHomeModels.generate(blockStateModelGenerator);
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
        itemModelGenerator.declareCustomModelItem(RDItems.LUNAR_POWER_CELL.asItem());
        itemModelGenerator.declareCustomModelItem(RDItems.LUNAR_SIGNAL_COIL.asItem());
        itemModelGenerator.declareCustomModelItem(RDItems.LUNAR_SIGNAL_CRYSTAL.asItem());
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
        itemModelGenerator.generateFlatItem(RDItems.FAIRY_DUST.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.GHOST_VEIL.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.ONI_HORN.asItem(), ModelTemplates.FLAT_ITEM);
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
        itemModelGenerator.generateFlatItem(RDItems.GENSOKYO_COMPASS.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.BOUNDARY_OFUDA.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.TIME_STOP_CLOCK.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.EARPHONE.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.KOISHI_HAT.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.ONI_MASK.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.FUMO_LICENSE.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.CURSED_DECOY_DOLL.asItem());
        itemModelGenerator.generateFlatItem(RDItems.VAISRAVANAS_PAGODA.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.DREAM_PILLOW.asItem());
        itemModelGenerator.generateFlatItem(RDItems.DREAM_MIRROR.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_COCOON.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.DREAM_SPOOL.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.TENGU_CAMERA.asItem());
        itemModelGenerator.declareCustomModelItem(RDItems.HIMEKAIDOU_HATATES_PHONE.asItem());
        itemModelGenerator.generateFlatItem(RDItems.BAD_APPLE.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SCARECROW.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.EXORCISM_PAPER.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.COPPER_COIN.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SILVER_COIN.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.GOLD_COIN.asItem(), ModelTemplates.FLAT_ITEM);
        SpellcardItemModels.generate(itemModelGenerator, packOutput.getModContainer());
        var techniqueModel = ModelTemplates.FLAT_ITEM.create(RDItems.COMBAT_TECHNIQUE.asItem(),
                new TextureMapping().put(TextureSlot.LAYER0,
                        new net.minecraft.client.resources.model.sprite.Material(Identifier.withDefaultNamespace("item/book"))),
                itemModelGenerator.modelOutput);
        itemModelGenerator.itemModelOutput.accept(RDItems.COMBAT_TECHNIQUE.asItem(),
                ItemModelUtils.plainModel(techniqueModel));
        itemModelGenerator.generateFlatItem(RDItems.SATORI_EYE.asItem(), ModelTemplates.FLAT_ITEM);
        RDItems.LUNAR_WEAPONS.forEach(item -> itemModelGenerator.declareCustomModelItem(item.asItem()));
        RDItems.LUNAR_MAGAZINES.forEach(item -> itemModelGenerator.declareCustomModelItem(item.asItem()));
        itemModelGenerator.generateFlatItem(RDItems.FAST_RECIPE_BOOK.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.LOW_GRAVITY_BOOT.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.CROWN_OF_THE_UNDERWORLD.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.SUNFLOWER.asItem());
        itemModelGenerator.generateFlatItem(RDItems.CUSTOM_SKIN_SELECTOR.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.CHEQUE.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SOUL_CARD.asItem(), ModelTemplates.FLAT_ITEM);

        // 工坊物品（物品定义由 workshop_interactions 清单提供）
        for (var item : RDWorkshopItems.ALL) {
            itemModelGenerator.createFlatItemModel(item.asItem(), ModelTemplates.FLAT_ITEM);
        }

        // 武器
        itemModelGenerator.generateFlatItem(RDItems.HAKUREI_CANE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.BAGUA_FURNACE.asItem());
        itemModelGenerator.generateFlatItem(RDItems.WIND_BLESSING_CANE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.declareCustomModelItem(RDItems.MAGIC_BROOM.asItem());
        itemModelGenerator.generateFlatItem(RDItems.KNIFE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
//        itemModelGenerator.generateSpear(RDItems.GUNGNIR);
        itemModelGenerator.itemModelOutput.accept(RDItems.LEVATIN.asItem(), ItemModelUtils.select(
                new DisplayContext(),
                ItemModelUtils.plainModel(ReverieDreams.id("item/levatin_staff")),
                ItemModelUtils.when(ItemDisplayContext.GUI,
                        ItemModelUtils.plainModel(ReverieDreams.id("item/levatin_staff_gui")))));
        itemModelGenerator.declareCustomModelItem(RDItems.ROKANKEN.asItem());
        itemModelGenerator.declareCustomModelItem(RDItems.HAKUROKEN.asItem());
//        itemModelGenerator.generateFlatItem(RDItems.ROKANKEN.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
//        itemModelGenerator.generateFlatItem(RDItems.HAKUROKEN.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
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
        itemModelGenerator.declareCustomModelItem(RDItems.IRON_BAR.asItem());
        itemModelGenerator.declareCustomModelItem(RDItems.ONI_IRON_BAR.asItem());
        itemModelGenerator.generateFlatItem(RDItems.DEATH_SCYTHE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAID_HAIRBAND.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAID_UPPER_SKIRT.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAID_LOWER_SKIRT.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.MAID_SHOE.asItem(), ModelTemplates.FLAT_ITEM);

        // 工具矿物类
        itemModelGenerator.generateFlatItem(AncientIronEquipment.HELMET, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(AncientIronEquipment.CHESTPLATE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(AncientIronEquipment.LEGGINGS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(AncientIronEquipment.BOOTS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(AncientIronEquipment.SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(AncientIronEquipment.PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(AncientIronEquipment.AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(AncientIronEquipment.SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(AncientIronEquipment.HOE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateSpear(AncientIronEquipment.SPEAR);
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
        itemModelGenerator.generateFlatItem(RDItems.A_MELODY_BLOOMING_ON_THE_EARTH.asItem(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(RDItems.SPRAWLING_EARTH.asItem(), ModelTemplates.FLAT_ITEM);

        this.generateGuiItemModels(itemModelGenerator);
        this.generateDanmakuItemModels(itemModelGenerator);
        this.generateHolder(itemModelGenerator);
        this.generateMystiaItem(itemModelGenerator);
    }

    public void generateJapaneseDecorBlockAsset(BlockModelGenerators blockStateModelGenerator) {
        // Vanilla model templates: door / bars / lantern. Bars, shoji and
        // lantern are re-implemented so their item can reference the block
        // model directly instead of vanilla's flat item sprite.
        blockStateModelGenerator.createDoor(RDBlocks.FUSUMA_DOOR.asBlock());
        blockStateModelGenerator.createDoor(RDJapaneseHomeBlocks.SHOJI_DOOR.asBlock());
        this.createJapaneseShoji(blockStateModelGenerator, RDBlocks.SHOJI_PANE.asBlock());
        this.createJapaneseBars(blockStateModelGenerator, RDBlocks.RANMA.asBlock());
        this.createJapaneseLantern(blockStateModelGenerator, RDBlocks.CHOCHIN.asBlock());

        // Capped 2x2 connection states; models are authored as normal source resources.
        JapaneseInteriorModels.tatami(blockStateModelGenerator, RDBlocks.TATAMI.asBlock(), "tatami_auto_", 1);
        JapaneseInteriorModels.tatami(blockStateModelGenerator, RDBlocks.TATAMI_BLOCK.asBlock(), "tatami_block_auto_", 16);
        JapaneseInteriorModels.tableModels(blockStateModelGenerator);
        JapaneseInteriorModels.tableState(blockStateModelGenerator, RDBlocks.CHABUDAI.asBlock());
        JapaneseInteriorModels.teaModels(blockStateModelGenerator);
        this.registerRotatable(blockStateModelGenerator, RDBlocks.TEA_SET.asBlock());
        this.registerDirectBlockItem(blockStateModelGenerator, RDBlocks.TEA_SET.asBlock(), "block/tea_set");
        for (var board : List.of(RDBlocks.XIANGQI_BOARD, RDBlocks.CHESS_BOARD, RDBlocks.GO_BOARD)) {
            this.registerRotatable(blockStateModelGenerator, board.asBlock());
            this.registerDirectBlockItem(blockStateModelGenerator, board.asBlock(), "block/" + BuiltInRegistries.BLOCK.getKey(board.asBlock()).getPath());
        }

        // Horizontal-facing thin decorations.
        this.registerRotatable(blockStateModelGenerator, RDBlocks.TATAMI_CORNER.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDBlocks.SUDARE.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDBlocks.NOREN_INDIGO.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDBlocks.NOREN_WHITE.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDBlocks.NOREN_RED.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDBlocks.NOREN_BLACK.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDBlocks.NOREN_BROWN.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDBlocks.NOREN_GREEN.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDBlocks.ANDON.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDBlocks.ZABUTON.asBlock());
        RDBlocks.ZABUTONS.forEach((color, block) -> {
            new ModelTemplate(Optional.of(ReverieDreams.id("block/zabuton_colored")), Optional.empty(),
                    TextureSlot.TOP, TextureSlot.SIDE).create(block.asBlock(), new TextureMapping()
                    .put(TextureSlot.TOP, TextureMapping.getBlockTexture(BuiltInRegistries.BLOCK.getValue(
                            Identifier.withDefaultNamespace(color.getName() + "_wool"))))
                    .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(BuiltInRegistries.BLOCK.getValue(
                            Identifier.withDefaultNamespace(color.getName() + "_concrete")))),
                    blockStateModelGenerator.modelOutput);
            this.registerRotatable(blockStateModelGenerator, block.asBlock());
            this.registerDirectBlockItem(blockStateModelGenerator, block.asBlock(), "block/" + color.getName() + "_zabuton");
        });
        this.registerRotatable(blockStateModelGenerator, RDBlocks.BYOBU.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDBlocks.KAKEJIKU.asBlock());

        // Block items reference their block models directly (like vanilla
        // carpets and fences) instead of flat item sprites. Fusuma stays a flat
        // door icon, matching vanilla doors.
        this.registerDirectBlockItem(blockStateModelGenerator, RDBlocks.TATAMI.asBlock(), "block/tatami_conn_0");
        this.registerDirectBlockItem(blockStateModelGenerator, RDBlocks.TATAMI_BLOCK.asBlock(), "block/tatami_block_conn_0");
        this.registerDirectBlockItem(blockStateModelGenerator, RDBlocks.TATAMI_CORNER.asBlock(), "block/tatami_corner");
        this.registerDirectBlockItem(blockStateModelGenerator, RDBlocks.SUDARE.asBlock(), "block/sudare");
        this.registerDirectBlockItem(blockStateModelGenerator, RDBlocks.NOREN_INDIGO.asBlock(), "block/noren_indigo");
        this.registerDirectBlockItem(blockStateModelGenerator, RDBlocks.NOREN_WHITE.asBlock(), "block/noren_white");
        this.registerDirectBlockItem(blockStateModelGenerator, RDBlocks.NOREN_RED.asBlock(), "block/noren_red");
        this.registerDirectBlockItem(blockStateModelGenerator, RDBlocks.NOREN_BLACK.asBlock(), "block/noren_black");
        this.registerDirectBlockItem(blockStateModelGenerator, RDBlocks.NOREN_BROWN.asBlock(), "block/noren_brown");
        this.registerDirectBlockItem(blockStateModelGenerator, RDBlocks.NOREN_GREEN.asBlock(), "block/noren_green");
        this.registerDirectBlockItem(blockStateModelGenerator, RDBlocks.ANDON.asBlock(), "block/andon");
        this.registerDirectBlockItem(blockStateModelGenerator, RDBlocks.ZABUTON.asBlock(), "block/zabuton");
        this.registerDirectBlockItem(blockStateModelGenerator, RDBlocks.CHABUDAI.asBlock(), "block/chabudai_table_0");
        this.registerDirectBlockItem(blockStateModelGenerator, RDBlocks.BYOBU.asBlock(), "block/byobu");
        this.registerDirectBlockItem(blockStateModelGenerator, RDBlocks.KAKEJIKU.asBlock(), "block/kakejiku");
    }

    /**
     * Shoji screens keep the pane connection properties, but the paper panel
     * always fills the block face instead of shrinking to open bars. Each
     * connected side gets a half sheet running from the block edge to the
     * centre, so straight runs join seamlessly and corners form an L. A screen
     * without connections gets one full sheet so it never shows as an empty
     * frame.
     */
    private void createJapaneseShoji(BlockModelGenerators generator, Block block) {
        MultiVariant half = plainVariant(ReverieDreams.id("block/shoji_pane_half"));
        MultiVariant halfEast = half.with(Y_ROT_90);
        MultiVariant halfSouth = half.with(Y_ROT_180);
        MultiVariant halfWest = half.with(Y_ROT_270);
        MultiVariant panelX = plainVariant(ReverieDreams.id("block/shoji_pane_panel_x"));
        MultiVariant panelZ = plainVariant(ReverieDreams.id("block/shoji_pane_panel_z"));

        MultiPartGenerator multipart = MultiPartGenerator.multiPart(block);
        // North/south axis: a full sheet when both sides connect, otherwise the
        // sheet only spans the connected half.
        multipart.with(shojiCondition(true, null, true, null), panelX);
        multipart.with(shojiCondition(true, null, false, null), half);
        multipart.with(shojiCondition(false, null, true, null), halfSouth);
        // East/west axis: the same halves rotated a quarter turn.
        multipart.with(shojiCondition(null, true, null, true), panelZ);
        multipart.with(shojiCondition(null, true, null, false), halfEast);
        multipart.with(shojiCondition(null, false, null, true), halfWest);
        // Unconnected screen still fills its own face.
        multipart.with(shojiCondition(false, false, false, false), panelZ);
        generator.blockStateOutput.accept(multipart);

        generator.registerSimpleItemModel(block, ReverieDreams.id("block/shoji_pane_inventory"));
    }

    /**
     * Builds a shoji screen condition; a null side is left unconstrained.
     */
    private ConditionBuilder shojiCondition(Boolean north, Boolean east, Boolean south, Boolean west) {
        ConditionBuilder condition = new ConditionBuilder();
        if (north != null) {
            condition.term(BlockStateProperties.NORTH, north);
        }
        if (east != null) {
            condition.term(BlockStateProperties.EAST, east);
        }
        if (south != null) {
            condition.term(BlockStateProperties.SOUTH, south);
        }
        if (west != null) {
            condition.term(BlockStateProperties.WEST, west);
        }
        return condition;
    }

    private void createJapaneseBars(BlockModelGenerators generator, Block block) {
        TextureMapping mapping = TextureMapping.bars(block);
        Identifier postEnds = ModelTemplates.BARS_POST_ENDS.create(block, mapping, generator.modelOutput);
        Identifier post = ModelTemplates.BARS_POST.create(block, mapping, generator.modelOutput);
        Identifier cap = ModelTemplates.BARS_CAP.create(block, mapping, generator.modelOutput);
        Identifier capAlt = ModelTemplates.BARS_CAP_ALT.create(block, mapping, generator.modelOutput);
        Identifier side = ModelTemplates.BARS_POST_SIDE.create(block, mapping, generator.modelOutput);
        Identifier sideAlt = ModelTemplates.BARS_POST_SIDE_ALT.create(block, mapping, generator.modelOutput);

        MultiPartGenerator multipart = MultiPartGenerator.multiPart(block);
        multipart.with(plainVariant(postEnds));
        multipart.with(barsCondition(true, true, true, true), plainVariant(post));
        multipart.with(barsCondition(true, false, false, false), plainVariant(cap));
        multipart.with(barsCondition(false, true, false, false), plainVariant(cap).with(Y_ROT_90));
        multipart.with(barsCondition(false, false, true, false), plainVariant(capAlt));
        multipart.with(barsCondition(false, false, false, true), plainVariant(capAlt).with(Y_ROT_90));
        multipart.with(new ConditionBuilder().term(BlockStateProperties.NORTH, true), plainVariant(side));
        multipart.with(new ConditionBuilder().term(BlockStateProperties.EAST, true), plainVariant(side).with(Y_ROT_90));
        multipart.with(new ConditionBuilder().term(BlockStateProperties.SOUTH, true), plainVariant(sideAlt));
        multipart.with(new ConditionBuilder().term(BlockStateProperties.WEST, true), plainVariant(sideAlt).with(Y_ROT_90));
        generator.blockStateOutput.accept(multipart);

        // Direct 3D block-model item icon (standalone bars post), like vanilla
        // fence/carpet item models.
        generator.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block).withPath(
                p -> p + "_inventory"));
    }

    private ConditionBuilder barsCondition(boolean north, boolean east, boolean south, boolean west) {
        return new ConditionBuilder()
                .term(BlockStateProperties.NORTH, north)
                .term(BlockStateProperties.EAST, east)
                .term(BlockStateProperties.SOUTH, south)
                .term(BlockStateProperties.WEST, west);
    }

    private void createJapaneseLantern(BlockModelGenerators generator, Block block) {
        // The chochin models are authored source resources: vanilla's lantern
        // template only samples a 6x7 corner of the texture, which shreds a
        // full face sprite like ours.
        MultiVariant standing = plainVariant(ReverieDreams.id("block/chochin"));
        MultiVariant hanging = plainVariant(ReverieDreams.id("block/chochin_hanging"));
        generator.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block)
                        .with(PropertyDispatch.initial(BlockStateProperties.HANGING)
                                .select(false, standing)
                                .select(true, hanging)));
        // Direct block-model item icon (standing lantern), matching how the
        // block renders, instead of vanilla's flat item sprite.
        generator.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block));
    }

    private void registerDirectBlockItem(BlockModelGenerators generator, Block block, String modelPath) {
        generator.registerSimpleItemModel(block, ReverieDreams.id(modelPath));
    }

    public void generateJapaneseExteriorBlockAsset(BlockModelGenerators blockStateModelGenerator) {
        // Engawa reuses tatami's north/east/south/west connection properties;
        // the 16 connection models are authored as normal source resources.
        this.createJapaneseConnectionBlockState(blockStateModelGenerator, RDBlocks.ENGAWA.asBlock(), "engawa_conn_");

        // Fences use vanilla fence post/side/inventory templates with a custom
        // texture mapping, so no planks-style base block is required.
        this.createJapaneseFence(blockStateModelGenerator, RDBlocks.YOTSUME_GAKI.asBlock());
        this.createJapaneseFence(blockStateModelGenerator, RDBlocks.BAMBOO_FENCE.asBlock());

        // 木格小窗: vanilla iron-bars style pane with lattice textures.
        blockStateModelGenerator.createBarsAndItem(RDBlocks.MUSHIKO_MADO.asBlock());

        // Vanilla slab/stairs/wall families for the plaster and roof tile sets.
        blockStateModelGenerator.family(RDBlocks.SHIKKUI_BLOCK.asBlock())
                .stairs(RDBlocks.SHIKKUI_STAIRS.asBlock())
                .slab(RDBlocks.SHIKKUI_SLAB.asBlock())
                .wall(RDBlocks.SHIKKUI_WALL.asBlock());
        blockStateModelGenerator.family(RDBlocks.KAWARA_TILES.asBlock())
                .stairs(RDBlocks.KAWARA_TILES_STAIRS.asBlock())
                .slab(RDBlocks.KAWARA_TILES_SLAB.asBlock());

        RakedGravelModels.generate(blockStateModelGenerator, RDBlocks.RAKED_GRAVEL.asBlock());
        // The basin model carries its own water plane; one variant covers the
        // waterlogged flag as well.
        blockStateModelGenerator.createNonTemplateModelBlock(RDBlocks.TSUKUBAI.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDBlocks.STONE_LANTERN.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDBlocks.STEPPING_STONE.asBlock());

        // Block items reference their block models directly, like vanilla
        // carpets and fences, instead of flat item sprites.
        blockStateModelGenerator.registerSimpleItemModel(
                RDBlocks.ENGAWA.asBlock(), ReverieDreams.id("block/engawa_conn_0"));
        blockStateModelGenerator.registerSimpleItemModel(
                RDBlocks.STONE_LANTERN.asBlock(), ModelLocationUtils.getModelLocation(RDBlocks.STONE_LANTERN.asBlock()));
        blockStateModelGenerator.registerSimpleItemModel(
                RDBlocks.STEPPING_STONE.asBlock(), ModelLocationUtils.getModelLocation(RDBlocks.STEPPING_STONE.asBlock()));
    }

    private void createJapaneseFence(BlockModelGenerators generator, Block block) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.TEXTURE, TextureMapping.getBlockTexture(block));
        generator.new BlockFamilyProvider(mapping).fence(block);
    }

    private void createJapaneseConnectionBlockState(BlockModelGenerators generator, Block block, String modelPrefix) {
        generator.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block)
                        .with(PropertyDispatch.initial(
                                        TatamiBlock.NORTH,
                                        TatamiBlock.EAST,
                                        TatamiBlock.SOUTH,
                                        TatamiBlock.WEST
                                )
                                .generate((north, east, south, west) -> {
                                    int mask = (north ? 1 : 0)
                                            | (east ? 2 : 0)
                                            | (south ? 4 : 0)
                                            | (west ? 8 : 0);
                                    return plainVariant(ReverieDreams.id("block/" + modelPrefix + mask));
                                }))
        );
    }

    public void generateKitchenBlock(BlockModelGenerators blockStateModelGenerator) {
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.COOKING_POT.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.CUTTING_BOARD.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.FRYING_PAN.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.GRILL.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.STEAMER.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.MYSTIA_COOKING_POT.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.MYSTIA_CUTTING_BOARD.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.MYSTIA_FRYING_PAN.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.MYSTIA_GRILL.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.MYSTIA_STEAMER.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.SUPER_COOKING_POT.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.SUPER_CUTTING_BOARD.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.SUPER_FRYING_PAN.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.SUPER_GRILL.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.SUPER_STEAMER.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.EXTREME_COOKING_POT.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.EXTREME_CUTTING_BOARD.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.EXTREME_FRYING_PAN.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.EXTREME_GRILL.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.EXTREME_STEAMER.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.NUKE_COOKING_POT.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.NUKE_CUTTING_BOARD.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.NUKE_FRYING_PAN.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.NUKE_GRILL.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDKitchenBlocks.NUKE_STEAMER.asBlock());

        blockStateModelGenerator.createNonTemplateModelBlock(RDBlocks.PLATE.asBlock());
        blockStateModelGenerator.createNonTemplateModelBlock(RDBlocks.CHAIR.asBlock());
        blockStateModelGenerator.createNonTemplateModelBlock(RDBlocks.TABLE.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDBlocks.CUPBOARD.asBlock());
        this.registerRotatable(blockStateModelGenerator, RDBlocks.ICE_MAKING_MACHINE.asBlock());

        blockStateModelGenerator.family(RDBlocks.BLACK_SALT_BLOCK.asBlock());

        blockStateModelGenerator.createCrossBlockWithDefaultItem(RDWoodBlocks.UDUMBARA_FLOWER.asBlock(), BlockModelGenerators.PlantType.NOT_TINTED);
        blockStateModelGenerator.createCrossBlockWithDefaultItem(RDWoodBlocks.TREMELLA.asBlock(), BlockModelGenerators.PlantType.NOT_TINTED);
        blockStateModelGenerator.createMultiface(RDMagicForestBlocks.FICUS_MICROCARPA.asBlock());
        blockStateModelGenerator.createCrossBlock(RDMagicForestBlocks.PUFF_YO_BUSH.asBlock(),
                BlockModelGenerators.PlantType.NOT_TINTED, PuffYoBushBlock.AGE, 0, 1, 2, 3);
    }

    public void generateMystiaItem(ItemModelGenerators itemModelGenerator) {
        itemModelGenerator.generateFlatItem(RDItems.MYSTIA_ICON.asItem(), ModelTemplates.FLAT_ITEM);
        for (var item : RDIngredientItems.INGREDIENTS) {
            if (RDIngredientItems.EXISTS.contains(item)) {
                continue;
            }
            itemModelGenerator.generateFlatItem(item.asItem(), ModelTemplates.FLAT_ITEM);
        }
        for (var item : RDCuisineItems.CUISINE_ITEMS) {
            itemModelGenerator.generateFlatItem(item.asItem(), ModelTemplates.FLAT_ITEM);
        }
        for (var item : RDBeverageItems.BEVERAGE_ITEMS) {
            itemModelGenerator.generateFlatItem(item.asItem(), ModelTemplates.FLAT_ITEM);
        }
    }

    public void generateHolder(ItemModelGenerators itemModelGenerator) {
        for (var species : AquaticSpecies.values()) {
            itemModelGenerator.itemModelOutput.accept(RDAquaticItems.bucket(species),
                    ItemModelUtils.plainModel(ReverieDreams.id("item/aquatic/" + species.key() + "_bucket")));
        }
        RDEntityHolderItems.HOLDERS.stream().map(ItemLike::asItem).forEach(itemModelGenerator::declareCustomModelItem);
    }

    public void generateGuiItemModels(ItemModelGenerators itemModelGenerator) {
        for (Holder<Item> item : RDGuiPlaceholderItems.getGuiPlaceholderItemList()) {
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
    }

    private void registerGuiItem(ItemModelGenerators itemModelGenerator, Item item) {
//        Model guiSlotModel = item("custom_slot", TextureKey.LAYER0);
//        itemModelGenerator.register(item, guiSlotModel);
    }

    public void generateDanmakuItemModels(ItemModelGenerators itemModelGenerator) {
        for (Item item : BuiltInRegistryProviders.DANMAKU_TYPE.values()
                                                              .stream()
                                                              .filter(type -> !type.isDeleteFromList())
                                                              .map(DanmakuType::getItemHolder)
                                                              .map(ItemLike::asItem)
                                                              .toList()) {
            itemModelGenerator.generateTwoLayerDyedItem(item);
        }
        itemModelGenerator.generateTwoLayerDyedItem(DanmakuTypes.SHORT_LASER.getItemHolder().asItem());
        itemModelGenerator.generateFlatItem(DanmakuTypes.LUNATIC_MOON.getItemHolder().asItem(), ModelTemplates.FLAT_ITEM);
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

    private static void registerWoolFamilies(BlockModelGenerators generator) {
        for (RDWoolBlocks.Family wool : RDWoolBlocks.FAMILIES) {
            TextureMapping texture = TextureMapping.cube(wool.wool());
            generator.new BlockFamilyProvider(texture).stairs(wool.stairs().asBlock());

            Block slab = wool.slab().asBlock();
            Identifier bottom = ModelTemplates.SLAB_BOTTOM.create(slab, texture, generator.modelOutput);
            Identifier top = ModelTemplates.SLAB_TOP.create(slab, texture, generator.modelOutput);
            generator.blockStateOutput.accept(MultiVariantGenerator.dispatch(slab)
                    .with(PropertyDispatch.initial(SlabBlock.TYPE).generate(type -> switch (type) {
                        case BOTTOM -> plainVariant(bottom);
                        case TOP -> plainVariant(top);
                        case DOUBLE -> plainVariant(ModelLocationUtils.getModelLocation(wool.wool()));
                    })));
            generator.registerSimpleItemModel(slab, bottom);
        }
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
        generator.createTintedLeaves(bundle.leaves().asBlock(), TexturedModel.LEAVES, bundle.leafTintColor());

        // ===== 落叶层 =====
        if (bundle.leafLitter() != null) {
            ((BlockModelGeneratorsAccessor) (Object) generator).callCreateLeafLitter(bundle.leafLitter().asBlock());
        }

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
                .pressurePlate(bundle.pressurePlate().asBlock())
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

    private MultiVariant railVariant(BlockModelGenerators generators, Block block, boolean powered, int slope) {
        Identifier base = ModelLocationUtils.getModelLocation(block);
        String name = base.getPath().substring("block/".length());
        String suffix = (powered ? "_on" : "") + (slope == 0 ? "" : slope > 0 ? "_raised_ne" : "_raised_sw");
        Identifier id = base.withPath(base.getPath() + suffix);
        generators.modelOutput.accept(id, () -> RedstoneDeviceModels.rail(name, powered, slope));
        return plainVariant(id);
    }

    private void createSignalCS(BlockModelGenerators generators, Block block) {
        Identifier base = ModelLocationUtils.getModelLocation(block);
        String name = base.getPath().substring("block/".length());
        generators.modelOutput.accept(base, () -> RedstoneDeviceModels.model(name, false));
        Identifier lit = base.withPath(base.getPath() + "_on");
        generators.modelOutput.accept(lit, () -> RedstoneDeviceModels.model(name, true));
        MultiVariant off = plainVariant(base), on = plainVariant(lit);
        var power = block instanceof RemoteClientBlock ? BlockStateProperties.POWERED : BlockStateProperties.OCCUPIED;
        generators.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(
                PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, power).generate((facing, powered) -> {
                    MultiVariant variant = powered ? on : off;
                    return switch (facing) {
                        case EAST -> variant.with(Y_ROT_90);
                        case SOUTH -> variant.with(Y_ROT_180);
                        case WEST -> variant.with(Y_ROT_270);
                        default -> variant;
                    };
                })));
    }

    private void createSignalDelayer(BlockModelGenerators generators, Block block, TexturedModel.Provider provider) {
        Identifier base = ModelLocationUtils.getModelLocation(block);
        generators.modelOutput.accept(base, () -> RedstoneDeviceModels.model("signal_delayer", false));
        Identifier lit = base.withPath(base.getPath() + "_on");
        generators.modelOutput.accept(lit, () -> RedstoneDeviceModels.model("signal_delayer", true));
        MultiVariant normal = plainVariant(base), powered = plainVariant(lit);

        generators.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block)
                        .with(PropertyDispatch.initial(BlockStateProperties.POWERED, BlockStateProperties.OCCUPIED).generate((hasPowered, hasOccupied) -> {
                            if (hasPowered) {
                                return powered;
                            } else {
                                return normal;
                            }
                        }))
                        .with(BlockModelGeneratorsAccessor.getRotationFacing())
        );
    }


    private void createSignalRails(BlockModelGenerators generators, Block block) {
        MultiVariant multiVariant = railVariant(generators, block, false, 0);
        MultiVariant multiVariant2 = railVariant(generators, block, false, 1);
        MultiVariant multiVariant3 = railVariant(generators, block, false, -1);
        MultiVariant multiVariant4 = railVariant(generators, block, true, 0);
        MultiVariant multiVariant5 = railVariant(generators, block, true, 1);
        MultiVariant multiVariant6 = railVariant(generators, block, true, -1);
        generators.registerSimpleFlatItemModel(block);
        generators.blockStateOutput
                .accept(
                        MultiVariantGenerator.dispatch(block)
                                .with(PropertyDispatch.initial(BlockStateProperties.POWERED, BlockStateProperties.RAIL_SHAPE_STRAIGHT)
                                        .generate((powered, railShape) -> {
                                            return switch (railShape) {
                                                case NORTH_SOUTH -> powered ? multiVariant4 : multiVariant;
                                                case EAST_WEST ->
                                                        (powered ? multiVariant4 : multiVariant).with(Y_ROT_90);
                                                case ASCENDING_EAST ->
                                                        (powered ? multiVariant5 : multiVariant2).with(Y_ROT_90);
                                                case ASCENDING_WEST ->
                                                        (powered ? multiVariant6 : multiVariant3).with(Y_ROT_90);
                                                case ASCENDING_NORTH -> powered ? multiVariant5 : multiVariant2;
                                                case ASCENDING_SOUTH -> powered ? multiVariant6 : multiVariant3;
                                                default ->
                                                        throw new UnsupportedOperationException("Fix you generator!");
                                            };
                                        }))
                );
    }

    private void createControllerRails(BlockModelGenerators generators, Block block) {
        MultiVariant multiVariant = railVariant(generators, block, false, 0);
        MultiVariant multiVariant2 = railVariant(generators, block, false, 1);
        MultiVariant multiVariant3 = railVariant(generators, block, false, -1);
        MultiVariant multiVariant4 = railVariant(generators, block, true, 0);
        MultiVariant multiVariant5 = railVariant(generators, block, true, 1);
        MultiVariant multiVariant6 = railVariant(generators, block, true, -1);
        generators.registerSimpleFlatItemModel(block);
        generators.blockStateOutput
                .accept(
                        MultiVariantGenerator.dispatch(block)
                                .with(PropertyDispatch.initial(BlockStateProperties.POWERED, BlockStateProperties.OCCUPIED, BlockStateProperties.RAIL_SHAPE_STRAIGHT)
                                        .generate((powered, occupied, railShape) -> {
                                            return switch (railShape) {
                                                case NORTH_SOUTH -> occupied ? multiVariant4 : multiVariant;
                                                case EAST_WEST ->
                                                        (occupied ? multiVariant4 : multiVariant).with(Y_ROT_90);
                                                case ASCENDING_EAST ->
                                                        (occupied ? multiVariant5 : multiVariant2).with(Y_ROT_90);
                                                case ASCENDING_WEST ->
                                                        (occupied ? multiVariant6 : multiVariant3).with(Y_ROT_90);
                                                case ASCENDING_NORTH -> occupied ? multiVariant5 : multiVariant2;
                                                case ASCENDING_SOUTH -> occupied ? multiVariant6 : multiVariant3;
                                                default ->
                                                        throw new UnsupportedOperationException("Fix you generator!");
                                            };
                                        }))
                );
    }

    @Override
    public @NonNull String getName() {
        return "Touhou Model Provider";
    }
}
