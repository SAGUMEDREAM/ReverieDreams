package cc.thonly.reverie_dreams.fabric;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.polymer.PolymerEntityGetter;
import cc.thonly.reverie_dreams.block.FoodDisplayBlock;
import cc.thonly.reverie_dreams.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.RDMPHooks;
import cc.thonly.reverie_dreams.creative_tab.content.ItemGroupContentHelper;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.registry.content.villager.RDPointOfInterestTypes;
import cc.thonly.reverie_dreams.fabric.polymer.PolymerTHGuideBookItem;
import cc.thonly.reverie_dreams.fabric.polymer.ResourcePackGenerator;
import cc.thonly.reverie_dreams.fabric.polymer.block.GensokyoAltarImpl;
import cc.thonly.reverie_dreams.fabric.polymer.block.ItemStackDisplayImpl;
import cc.thonly.reverie_dreams.fabric.polymer.helper.*;
import cc.thonly.reverie_dreams.item.base.ColoredSpawnEggItem;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.advancements.RDCriteriaTriggers;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.effect.RDPotions;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.sound.RDSoundEvents;
import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.command.CameraCommand;
import de.tomalbrc.cameraobscura.render.renderer.CanvasImageRenderer;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import eu.pb4.polymer.core.api.other.PolymerPotion;
import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.world.entity.BalmEntityTypeRegistration;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.NonNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@SuppressWarnings("unused")
@Slf4j
public class PolymerInitializer {
    public static final String POLYMER_MOD_ID = "reverie_dreams_polymerify";

    public static void bootstrap() {
        Balm.networking().allowServerOnly(ReverieDreams.MOD_ID);
        PolymerInitializer.polymerify();
        registerPlatformEventImpl();
    }

    public static void replaceGuidebook() {
        RDMPHooks.GuidebookFactory.EVENT.register(PolymerTHGuideBookItem::new);
    }

    private static void registerPlatformEventImpl() {
        RDMPHooks.TenguCameraItemUseCallback.EVENT.register((level, player, hand) -> {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            ItemStack stack = player.getItemInHand(hand);
            if (player.isShiftKeyDown()) {

                int fov = stack.getOrDefault(RDDataComponents.FOV.value(), 75);

                float pitch = player.getXRot();

                int delta = pitch < 0 ? +1 : -1;

                int newFov = fov + delta;

                if (newFov < 30) newFov = 30;
                if (newFov > 110) newFov = 110;

                stack.set(RDDataComponents.FOV.value(), newFov);

                ((ServerPlayer) player).sendSystemMessage(
                        Component.literal("§a" + newFov),
                        true
                );

                return InteractionResult.SUCCESS_SERVER;
            }
            Inventory inventory = player.getInventory();
            ItemStack cunsumeStack = ItemStack.EMPTY;
            for (ItemStack itemStack : inventory) {
                if (itemStack.isEmpty()) {
                    continue;
                }
                if (itemStack.is(RDItemTags.REPLACEABLE_BLANK_PHOTOS)) {
                    cunsumeStack = itemStack;
                    break;
                }
            }
            if (cunsumeStack.isEmpty() && !player.isCreative()) {
                return InteractionResult.FAIL;
            }
            ModConfig instance = ModConfig.getInstance();
            instance.renderEntities = true;
            try {
                CanvasImageRenderer renderer = new CanvasImageRenderer(player, 128, 128, instance.renderDistance);
                int fov = stack.getOrDefault(RDDataComponents.FOV.value(), 75);
                int oldFov = instance.fov;
                instance.fov = fov;
                ItemStack finalCunsumeStack = cunsumeStack;
                CompletableFuture.supplyAsync(renderer::render).thenAcceptAsync((mapImage) -> {
                    instance.fov = oldFov;
                    player.getCooldowns().addCooldown(player.getItemInHand(hand), 20 * 4);
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                    level.playSound(null, player.blockPosition(), RDSoundEvents.PHOTO.value(), SoundSource.PLAYERS);
                    if (!finalCunsumeStack.isEmpty()) {
                        finalCunsumeStack.consume(1, player);
                    }
                    RDCriteriaTriggers.USE_ITEM.value().trigger((ServerPlayer) player, finalCunsumeStack);
                    finalizeTenguCamera(mapImage, (ServerPlayer) player);
                }, level.getServer());
            } catch (Exception err) {
                log.error("Can't render canvas", err);
            }
            return InteractionResult.SUCCESS_SERVER;
        });
        RDMPHooks.FoodDisplayBlockEntityTicker.EVENT.register((world, pos, state, blockEntity) -> {
            if (!(world instanceof ServerLevel serverWorld)) {
                return;
            }

            Map<Long, ItemStackDisplayImpl.Model> longModelMap = ItemStackDisplayImpl.MAPPING.computeIfAbsent(serverWorld, w -> new Object2ObjectOpenHashMap<>());
            var model = longModelMap.get(pos.asLong());
            if (model != null) {
                model.updateItem(state);
            }
        });
        RDMPHooks.FoodDisplayBlockEntityUpdater.EVENT.register(blockEntity -> {
            if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
                return;
            }
            Map<Long, ItemStackDisplayImpl.Model> longModelMap = ItemStackDisplayImpl.MAPPING.computeIfAbsent(serverLevel, w -> new Object2ObjectOpenHashMap<>());
            var model = longModelMap.get(blockEntity.getBlockPos().asLong());
            if (!(blockEntity.getBlockState().getBlock() instanceof FoodDisplayBlock)) {
                return;
            }
            if (model != null) {
                model.updateItem(blockEntity.getBlockState());
            }
        });
        RDMPHooks.GensokyoAltarBlockEntityTicker.EVENT.register((world, pos, state, blockEntity) -> {
            if (blockEntity.tick > 5) {
                GensokyoAltarImpl.Model altarModel = GensokyoAltarImpl.POS_TO_MODEL.get(pos.asLong());
                if (altarModel != null) {
                    altarModel.update();
                }
                blockEntity.tick = 0;
            }
            GensokyoAltarImpl.Model altarModel = GensokyoAltarImpl.POS_TO_MODEL.get(pos.asLong());
            if (altarModel != null) {
                altarModel.angle += 2f;
                if (altarModel.angle >= 360) {
                    altarModel.angle = 0;
                }
            }
            blockEntity.tick++;
        });
    }

    private static void finalizeTenguCamera(CanvasImage canvasImage, ServerPlayer player) {
        if (player != null && !player.isRemoved()) {
            player.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
            List<ItemStack> items = CameraCommand.mapItems(canvasImage, player.level());
            items.forEach((x) -> {
                if (!player.addItem(x)) {
                    player.spawnAtLocation(player.level(), x);
                }

            });
        }
    }

    @SuppressWarnings("rawtypes")
    public static void polymerify() {
        PolymerEntityGetter.EVENT.register(PolymerEntity::get);
        for (Holder<Block> holder : RDBlocks.HOLDERS) {
            PolymerBlockHelper.registerOverlay(holder.value());
        }
        for (Holder<DataComponentType> component : RDDataComponents.COMPONENTS) {
            PolymerComponent.registerDataComponent(component.value());
        }
        for (Holder<Item> item : RDItems.LATE_POLYMERIFY_ITEM_LIST) {
            PolymerItemHelper.registerOverlay(item.value());
        }
        for (Item spawnEgg : ColoredSpawnEggItem.SPAWN_EGGS) {
            PolymerItemHelper.registerOverlay(spawnEgg);
        }
        for (Holder<Item> item : RDGuiItems.GUI_ITEM_LIST) {
            PolymerItemHelper.registerOverlay(item.value());
        }
        for (DanmakuType danmakuType : RegistryImpls.DANMAKU_TYPE) {
            PolymerItemHelper.registerOverlay(danmakuType.getItemHolder().asItem());
        }
        for (Holder<SoundEvent> soundEvent : RDSoundEvents.SOUND_EVENTS) {
            PolymerSoundEvent.registerOverlay(soundEvent.value());
        }
        for (Holder<BlockEntityType> entity : RDBlockEntityTypes.ENTITIES) {
            PolymerBlockUtils.registerBlockEntity(entity.value());
        }
        for (Holder<MobEffect> registryEntry : RDStatusEffects.EFFECTS) {
            PolymerStatusEffectHelper.registerOverlay(registryEntry);
        }
        for (Holder<Potion> potion : RDPotions.POTIONS) {
            PolymerPotion.registerOverlay(potion, new PolymerPotion() {
                @Override
                public @NonNull Potion getPolymerReplacement(Potion potion, PacketContext context) {
                    return Potions.HEALING.value();
                }
            });
        }
        for (BalmEntityTypeRegistration<?> entityType : RDEntityTypes.ENTITY_TYPES) {
            PolymerEntityUtils.registerType(entityType.asHolder().value());
        }
        for (Holder<CriterionTrigger<?>> holder : RDCriteriaTriggers.LIST) {
            RegistrySyncUtils.setServerEntry(BuiltInRegistries.TRIGGER_TYPES, holder.value());
        }
        for (Tuple<ResourceKey<CreativeModeTab>, Function<CreativeModeTab.Builder, CreativeModeTab.Builder>> tuple : ItemGroupContentHelper.FABRIC_LATE_INIT) {
            ResourceKey<CreativeModeTab> key = tuple.getA();
            Function<CreativeModeTab.Builder, CreativeModeTab.Builder> builderFunction = tuple.getB();
            PolymerItemGroupUtils.registerPolymerItemGroup(key, builderFunction.apply(ItemGroupContentHelper.builder()).build());
        }
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.RECIPE_SERIALIZER, RecipeManager.DANMAKU_DYE_RECIPE.value());
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.POINT_OF_INTEREST_TYPE, BuiltInRegistries.POINT_OF_INTEREST_TYPE.getValue(RDPointOfInterestTypes.HAWKERS_KEY));
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.POINT_OF_INTEREST_TYPE, BuiltInRegistries.POINT_OF_INTEREST_TYPE.getValue(RDPointOfInterestTypes.PRIEST_KEY));
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.POINT_OF_INTEREST_TYPE, BuiltInRegistries.POINT_OF_INTEREST_TYPE.getValue(RDPointOfInterestTypes.MONEY_SHOP_CLERK_KEY));

        PolymerEntityHelper.bootstrap();
        PolymerVillagerProfessionHelper.bootstrap();

        Iterator<Runnable> rIterator = ReverieDreamsFabric.FABRIC_LATE_INIT.iterator();
        while (rIterator.hasNext()) {
            Runnable next = rIterator.next();
            next.run();
            rIterator.remove();
        }

        PolymerResourcePackUtils.addModAssets(ReverieDreams.MOD_ID);
        PolymerResourcePackUtils.addModAssets(POLYMER_MOD_ID);
        PolymerResourcePackUtils.markAsRequired();
        ResourcePackExtras.forDefault().addBridgedModelsFolder(
                ReverieDreams.id("block"),
                ReverieDreams.id("item"),
                ReverieDreams.id("entity"),
                ReverieDreams.id("font")
        );
        ResourcePackExtras.forDefault().addBridgedModelsFolder(
                polymerifyId("block"),
                polymerifyId("item"),
                polymerifyId("entity"),
                polymerifyId("font")
        );
        ResourcePackGenerator.registerEvent();
    }

    public static Identifier polymerifyId(String name) {
        return Identifier.fromNamespaceAndPath(POLYMER_MOD_ID, name);
    }
}
