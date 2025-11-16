package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.block.KitchenBlockType;
import cc.thonly.reverie_dreams.block.kitchen.AbstractKitchenwareBlock;
import cc.thonly.reverie_dreams.gui.recipe.gui.KitchenBlockGui;
import cc.thonly.reverie_dreams.registry.content.item.RDFoodItems;
import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.util.entity.PlayerUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;

@Setter
@Getter
@ToString
public class KitchenwareBlockEntity extends BlockEntity implements WorldlyContainer {
    public static final Supplier<ItemStackWrapper> DEFAULT_WRAPPER_FACTORY = ItemStackWrapper::empty;
    public static final Gson GSON = new Gson();
    public static final Map<UUID, Set<KitchenBlockGui<?>>> SESSIONS = new Object2ObjectOpenHashMap<>();
    public static final int OUTPUT_SLOT = 5;
    private SimpleContainer inventory = new SimpleContainer(6);
    private KitchenRecipeType.KitchenType recipeType;
    private ResourceLocation recipeId;
    private ItemStackWrapper preOutput = DEFAULT_WRAPPER_FACTORY.get();
    private Double tickLeft = 0.0;
    private DoubleUnaryOperator bonusOperator;
    private UUID uuid = UUID.randomUUID();
    private final AbstractKitchenwareBlock block;
    private WorkingState workingState = WorkingState.NONE;

    public KitchenwareBlockEntity(BlockPos pos, BlockState state) {
        super(RDBlockEntityTypes.KITCHENWARE_BLOCK_ENTITY, pos, state);
        Block block = state.getBlock();
        this.block = (AbstractKitchenwareBlock) block;
        this.recipeType = KitchenBlockType.BLOCK_2_KITCHEN_TYPE.get(block);
        this.bonusOperator = this.block.getBonusOperator();
    }


    public static void tick(Level world, BlockPos blockPos, BlockState state, KitchenwareBlockEntity self) {
        KitchenwareBlockEntity blockEntity = self.get();
        if (blockEntity.recipeType == null) {
            return;
        }
        if (world.isClientSide() || self.recipeType == null) return;
        ServerLevel serverWorld = (ServerLevel) world;
        BlockPos pos = self.getBlockPos();
        if (self.preOutput != null && !self.preOutput.isEmpty()) {
            self.workingState = WorkingState.WORKING;
            self.tickLeft -= self.bonusOperator.applyAsDouble(1.0);
            serverWorld.sendParticles(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1, 0, 0.5, 0, 0.1);
            if (self.tickLeft <= 0.0) {
                self.tickLeft = 0.0;
                self.workingState = WorkingState.NONE;
                self.handleOutput();
            }
            self.setChanged();
        } else {
            self.workingState = WorkingState.NONE;
        }
    }
//    public static void tick(World world, BlockPos blockPos, BlockState state, KitchenwareBlockEntity self) {
//        KitchenwareBlockEntity blockEntity = self.get();
//        if (blockEntity.recipeType == null) {
//            return;
//        }
//        if (world.isClient() || self.recipeType == null) return;
//
//        ServerWorld serverWorld = (ServerWorld) world;
//        BlockPos pos = self.getPos();
//
//        switch (self.workingState) {
//            case WorkingState.WORKING -> {
//                self.tickLeft -= self.tickSpeedBonus + 1.0;
//                serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1, 0, 0.5, 0, 0.1);
//                if (self.tickLeft <= 0.0) {
//                    self.tickLeft = 0.0;
//                    self.workingState = WorkingState.NONE;
//                    self.handleOutput();
//                }
//                self.markDirty();
//            }
//            case NONE_FUEL -> {
//                if (self.preOutput.isEmpty()) {
//                    break;
//                }
//
//                Optional<CooktopBlockEntity> cooktopBlockEntityResult = self.getCooktopBlockEntity();
//                if (cooktopBlockEntityResult.isEmpty()) {
//                    break;
//                }
//                CooktopBlockEntity cooktopBlockEntity = cooktopBlockEntityResult.get();
//
//                if (cooktopBlockEntity.ticks > 0 || cooktopBlockEntity.use()) {
//                    self.workingState = WorkingState.WORKING;
//                    self.markDirty();
//                } else {
//                    self.workingState = WorkingState.NONE_FUEL;
//                }
//            }
//            case NONE -> {
//                if (!self.preOutput.isEmpty()) {
//                    self.workingState = WorkingState.NONE_FUEL;
//                }
//            }
//        }
//        if ((!self.block.getRequiredEnergy()) && !self.preOutput.isEmpty()) {
//            self.workingState = WorkingState.WORKING;
//        }
//    }
//
//    public Optional<CooktopBlockEntity> getCooktopBlockEntity() {
//        if (this.world == null) {
//            return Optional.empty();
//        }
//        BlockPos blockPos = this.getPos();
//        BlockPos down = blockPos.down();
//        BlockState blockState = this.world.getBlockState(down);
//        Block block = blockState.getBlock();
//        if (block != MIBlocks.COOKTOP) {
//            return Optional.empty();
//        }
//        if (!(this.world.getBlockEntity(down) instanceof CooktopBlockEntity cooktop)) {
//            return Optional.empty();
//        } else {
//            return Optional.of(cooktop);
//        }
//    }

    public void setOutput(ItemStack itemStack, Double time) {
        this.setOutput(ItemStackWrapper.of(itemStack), time);
    }

    public void setOutput(ItemStackWrapper recipeWrapper, Double time) {
        this.setPreOutput(recipeWrapper);
        this.setTickLeft(time);
//        if (!this.hasFuel()) {
//            this.useFuel();
//        }
        this.setChanged();
    }

//    public void useFuel() {
//        Optional<CooktopBlockEntity> cooktop = this.getCooktopBlockEntity();
//        cooktop.ifPresent((cooktopBlockEntity) -> {
//            boolean use = cooktopBlockEntity.use();
//            if (use) {
//                this.workingState = WorkingState.WORKING;
//            }
//        });
//    }

//    public boolean hasFuel() {
//        Optional<CooktopBlockEntity> cooktop = this.getCooktopBlockEntity();
//        return cooktop.isPresent() && cooktop.get().isWorking();
//    }

    public void handleOutput() {
        KitchenwareBlockEntity blockEntity = this;
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        ServerLevel serverWorld = (ServerLevel) this.getLevel();
        BlockPos blockPos = this.getBlockPos();

        if (blockEntity.isWorking()) {
            blockEntity.tickLeft -= blockEntity.bonusOperator.applyAsDouble(1.0);
            if (serverWorld != null) {
                serverWorld.sendParticles(
                        ParticleTypes.SNOWFLAKE,
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ(),
                        1,
                        0,
                        0.5,
                        0,
                        0.1
                );
            }
            blockEntity.setChanged();
        } else if (!blockEntity.isWorking() && !blockEntity.preOutput.getItemStack().isEmpty()) {
            ItemStack prevStack = blockEntity.inventory.getItem(OUTPUT_SLOT);
            if (!prevStack.isEmpty()) {
                Item item = prevStack.getItem();
                if (item != blockEntity.preOutput.getItemStack().getItem()) {
                    blockEntity.throwItem(serverWorld, prevStack);
                }
                if (!ItemStack.isSameItemSameComponents(blockEntity.preOutput.getItemStack(), prevStack)) {
                    blockEntity.throwItem(serverWorld, prevStack);
                }
            }
            if (ItemStack.isSameItemSameComponents(blockEntity.preOutput.getItemStack(), prevStack)) {
                if (prevStack.getCount() < prevStack.getMaxStackSize()) {
                    prevStack.setCount(prevStack.getCount() + 1);
                } else {
                    blockEntity.throwItem(serverWorld, prevStack);
                    prevStack.setCount(prevStack.getCount() + 1);
                }

            } else {
                blockEntity.inventory.setItem(OUTPUT_SLOT, blockEntity.preOutput.getItemStack().copy());
                if (this.getLevel() != null && blockEntity.block.isWillBeFailure(this.getLevel())) {
                    blockEntity.inventory.setItem(OUTPUT_SLOT, RDFoodItems.DARK_CUISINE.getDefaultInstance().copy());
                }
            }
            blockEntity.preOutput = DEFAULT_WRAPPER_FACTORY.get();

            List<ServerPlayer> nearbyPlayers = PlayerUtil.getNearbyPlayers(serverWorld, blockEntity.worldPosition, 16);
            for (ServerPlayer player : nearbyPlayers) {
                player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 1.0f);
            }

            blockEntity.setChanged();
        }

    }

    public void throwItem(ServerLevel world, ItemStack prevItem) {
        ItemEntity itemEntity = new ItemEntity(world, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), prevItem.copy());
        world.addFreshEntity(itemEntity);
        this.inventory.setItem(OUTPUT_SLOT, ItemStack.EMPTY);
    }

    public boolean isWorking() {
        return this.workingState == WorkingState.WORKING;
    }


    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ContainerHelper.saveAllItems(view, this.inventory.items);
        view.putDouble("TickLeft", this.tickLeft);
        view.putInt("WorkingState", this.workingState.getId());
        DataResult<JsonElement> dataResult = ItemStackWrapper.CODEC.encodeStart(JsonOps.INSTANCE, this.preOutput);
        Optional<JsonElement> result = dataResult.result();
        if (result.isPresent()) {
            JsonElement element = result.get();
            view.putString("PreOutput", GSON.toJson(element));
        }
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        SimpleContainer inventory = new SimpleContainer(6);
        ContainerHelper.loadAllItems(view, inventory.items);
        this.inventory = inventory;
        this.tickLeft = view.getDoubleOr("TickLeft", 0.0);
        this.workingState = WorkingState.getFromInt(view.getIntOr("WorkingState", 0));
        Optional<String> pOutputOptional = view.getString("PreOutput");
        if (pOutputOptional.isPresent()) {
            String preOutputJson = pOutputOptional.get();
            JsonElement json = JsonParser.parseString(preOutputJson);
            Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);
            DataResult<ItemStackWrapper> parse = ItemStackWrapper.CODEC.parse(input);
            Optional<ItemStackWrapper> result = parse.result();
            result.ifPresent(wrapper -> this.preOutput = wrapper);
        }
    }

    public KitchenwareBlockEntity get() {
        return this;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[] {OUTPUT_SLOT};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return dir == Direction.DOWN;
    }

    @Override
    public int getContainerSize() {
        return this.inventory.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.inventory.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return this.inventory.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return this.inventory.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.inventory.setItem(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public void clearContent() {
        this.inventory.clearContent();
    }

    @Getter
    public enum WorkingState {
        NONE(0),
        NONE_FUEL(1),
        WORKING(2);
        private final int id;

        WorkingState(int id) {
            this.id = id;
        }

        public static WorkingState getFromInt(int id) {
            List<WorkingState> list = Arrays.stream(WorkingState.values()).filter(e -> e.id == id).toList();
            return list.isEmpty() ? NONE : list.getFirst();
        }
    }
}
