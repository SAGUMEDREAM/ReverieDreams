package cc.thonly.mystias_izakaya.block;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.block.entity.ItemStackDisplayBlockEntity;
import cc.thonly.reverie_dreams.block.crop.TransparentFlatTripWire;
import cc.thonly.reverie_dreams.interfaces.IItemStack;
import cc.thonly.reverie_dreams.recipe.ItemStackRecipeWrapper;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.UseRemainderComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.item.consume.ConsumeEffect;
import net.minecraft.potion.Potion;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Setter
@Getter
public class ItemStackDisplay extends BlockWithEntity implements FactoryBlock, IdentifierGetter, TransparentFlatTripWire {
    public static final Map<Long, Model> POS_TO_MODEL = new HashMap<>();
    public static final MapCodec<ItemStackDisplay> CODEC = ItemStackDisplay.createCodec(ItemStackDisplay::new);
    private Identifier identifier;

    private ItemStackDisplay(Settings settings) {
        super(settings);
    }

    public ItemStackDisplay(String name, Settings settings) {
        this(MystiasIzakaya.id(name), settings);
    }

    public ItemStackDisplay(Identifier identifier, Settings settings) {
        super(settings.registryKey(RegistryKey.of(RegistryKeys.BLOCK, identifier)));
        this.identifier = identifier;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        ItemStack stack = player.getMainHandStack();
        if (stack != null && !world.isClient && world instanceof ServerWorld serverWorld) {
            if (player.isSneaking()) {
                if (!(serverWorld.getBlockEntity(pos) instanceof ItemStackDisplayBlockEntity isdBlockEntity)) {
                    return ActionResult.PASS;
                }
                boolean isFood = ((IItemStack) (Object) isdBlockEntity.getItem().getItemStack()).isFood();
                if (isFood) {
                    ItemStack contentStack = isdBlockEntity.getItem().getItemStack();
                    ConsumableComponent consumableComponent = contentStack.get(DataComponentTypes.CONSUMABLE);
                    UseRemainderComponent useRemainderComponent = contentStack.get(DataComponentTypes.USE_REMAINDER);
                    contentStack.finishUsing(serverWorld, player);
                    if (consumableComponent != null) {
                        RegistryEntry<SoundEvent> sound = consumableComponent.sound();
                        world.playSound(null, player.getBlockPos(), sound.value(), SoundCategory.BLOCKS, 1.0f, 1.0f);
                    }
                    contentStack.decrementUnlessCreative(1, player);
                    if (useRemainderComponent != null && !player.isInCreativeMode()) {
                        ItemStack itemStack = useRemainderComponent.convert(contentStack, contentStack.getCount(), player.isInCreativeMode(), player::giveOrDropStack);
                        isdBlockEntity.setItem(ItemStackRecipeWrapper.of(itemStack));
                    }
                    isdBlockEntity.update();
                    serverWorld.updateListeners(pos, state, state, Block.NOTIFY_ALL);
                    isdBlockEntity.markDirty();
                    return ActionResult.SUCCESS_SERVER;
                }
            } else {
                if (serverWorld.getBlockEntity(pos) instanceof ItemStackDisplayBlockEntity isdBlockEntity) {
                    ItemStackRecipeWrapper item = isdBlockEntity.getItem();
                    if (!stack.isEmpty() && item.isEmpty()) {
                        ItemStackRecipeWrapper itemStackRecipeWrapper = ItemStackRecipeWrapper.of(stack.copy());
                        itemStackRecipeWrapper.getItemStack().setCount(1);
                        stack.decrementUnlessCreative(1, player);
                        isdBlockEntity.setItem(itemStackRecipeWrapper);
                        isdBlockEntity.setYaw(player.getYaw());
                        isdBlockEntity.update();
                        isdBlockEntity.markDirty();
                    } else {
                        ItemEntity itemEntity = new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), item.getItemStack(), 0, 0.2, 0);
                        isdBlockEntity.setItem(ItemStackRecipeWrapper.empty());
                        serverWorld.spawnEntity(itemEntity);
                    }
                    serverWorld.updateListeners(pos, state, state, Block.NOTIFY_ALL);
                    isdBlockEntity.markDirty();
                }
            }
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld && world.getBlockEntity(pos) instanceof ItemStackDisplayBlockEntity isdBlockEntity) {
            ItemStackRecipeWrapper item = isdBlockEntity.getItem();
            if (!item.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), item.getItemStack(), 0, 0.2, 0);
                isdBlockEntity.setItem(ItemStackRecipeWrapper.empty());
                serverWorld.spawnEntity(itemEntity);
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ItemStackDisplayBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, MIBlockEntities.ITEM_DISPLAY_BLOCK_ENTITY, ItemStackDisplayBlockEntity::tick);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        var model = new Model(world, this, initialBlockState, pos);
        POS_TO_MODEL.put(pos.asLong(), model);
        return model;
    }

    @Getter
    public static class Model extends BlockModel {
        private final ServerWorld serverWorld;
        private final Block block;
        private final BlockPos blockPos;
        private final ItemDisplayElement main;
        private ItemStackDisplayBlockEntity blockEntity;
        private final ItemDisplayElement item;

        public Model(ServerWorld serverWorld, Block block, BlockState initialBlockState, BlockPos blockPos) {
            this.serverWorld = serverWorld;
            this.block = block;
            this.blockPos = blockPos;

            this.main = ItemDisplayElementUtil.createSimple(initialBlockState.getBlock().asItem());
            this.main.setScale(new Vector3f(1.8f));
            this.main.setOffset(new Vec3d(0, -0.05, 0));
            this.item = ItemDisplayElementUtil.createSimple(Items.AIR);
            this.item.setScale(new Vector3f(0.5f));
            this.item.setOffset(new Vec3d(0, -0.22, 0));

            addElement(this.main);
            addElement(this.item);
        }

        public void updateItem(BlockState blockState) {
            boolean chunkLoaded = this.serverWorld.isPosLoaded(this.blockPos.getX(), this.blockPos.getY());
            if (chunkLoaded) {
                BlockEntity blockEntity = this.serverWorld.getBlockEntity(this.blockPos);
                if (this.blockEntity == null && blockEntity instanceof ItemStackDisplayBlockEntity itemStackDisplayBlockEntity) {
                    this.blockEntity = itemStackDisplayBlockEntity;
                }

                ItemStackRecipeWrapper item;
                if (this.blockEntity != null && !ItemStack.areEqual(this.blockEntity.getItem().getItemStack(), this.item.getItem())) {
                    removeElement(this.item);
                    item = this.blockEntity.getItem();
                    this.item.setItem(item.getItemStack().copy());
                    this.item.setOffset(new Vec3d(0, -0.22, 0));
                    this.item.setRotation((float) 0, (float) this.blockEntity.getYaw() + 180);
                    addElement(this.item);
                }
            }
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                updateItem(this.blockState());
            }
            this.tick();
            super.notifyUpdate(updateType);
        }
    }
}
