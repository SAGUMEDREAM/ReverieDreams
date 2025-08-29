package cc.thonly.mystias_izakaya.block.entity;

import cc.thonly.mystias_izakaya.block.kitchenware.AbstractKitchenwareBlock;
import cc.thonly.mystias_izakaya.block.KitchenBlockType;
import cc.thonly.mystias_izakaya.block.MIBlockEntities;
import cc.thonly.mystias_izakaya.gui.recipe.block.KitchenBlockGui;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.mystias_izakaya.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.util.PlayerUtils;
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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;

@Setter
@Getter
@ToString
public class KitchenwareBlockEntity extends BlockEntity implements SidedInventory {
    public static final Supplier<ItemStackWrapper> DEFAULT_WRAPPER_FACTORY = ItemStackWrapper::empty;
    public static final Gson GSON = new Gson();
    public static final Map<UUID, Set<KitchenBlockGui<?>>> SESSIONS = new Object2ObjectOpenHashMap<>();
    public static final int OUTPUT_SLOT = 5;
    private SimpleInventory inventory = new SimpleInventory(6);
    private KitchenRecipeType.KitchenType recipeType;
    private Identifier recipeId;
    private ItemStackWrapper preOutput = DEFAULT_WRAPPER_FACTORY.get();
    private Double tickLeft = 0.0;
    private DoubleUnaryOperator bonusOperator;
    private UUID uuid = UUID.randomUUID();
    private final AbstractKitchenwareBlock block;
    private WorkingState workingState = WorkingState.NONE;

    public KitchenwareBlockEntity(BlockPos pos, BlockState state) {
        super(MIBlockEntities.KITCHENWARE_BLOCK_ENTITY, pos, state);
        Block block = state.getBlock();
        this.block = (AbstractKitchenwareBlock) block;
        this.recipeType = KitchenBlockType.BLOCK_2_KITCHEN_TYPE.get(block);
        this.bonusOperator = this.block.getBonusOperator();
    }


    public static void tick(World world, BlockPos blockPos, BlockState state, KitchenwareBlockEntity self) {
        KitchenwareBlockEntity blockEntity = self.get();
        if (blockEntity.recipeType == null) {
            return;
        }
        if (world.isClient() || self.recipeType == null) return;
        ServerWorld serverWorld = (ServerWorld) world;
        BlockPos pos = self.getPos();
        if (self.preOutput != null && !self.preOutput.isEmpty()) {
            self.workingState = WorkingState.WORKING;
            self.tickLeft -= self.bonusOperator.applyAsDouble(1.0);
            serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1, 0, 0.5, 0, 0.1);
            if (self.tickLeft <= 0.0) {
                self.tickLeft = 0.0;
                self.workingState = WorkingState.NONE;
                self.handleOutput();
            }
            self.markDirty();
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
        this.markDirty();
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
        if (this.world == null || this.world.isClient()) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld) this.getWorld();
        BlockPos blockPos = this.getPos();

        if (blockEntity.isWorking()) {
            blockEntity.tickLeft -= blockEntity.bonusOperator.applyAsDouble(1.0);
            if (serverWorld != null) {
                serverWorld.spawnParticles(
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
            blockEntity.markDirty();
        } else if (!blockEntity.isWorking() && !blockEntity.preOutput.getItemStack().isEmpty()) {
            ItemStack prevStack = blockEntity.inventory.getStack(OUTPUT_SLOT);
            if (!prevStack.isEmpty()) {
                Item item = prevStack.getItem();
                if (item != blockEntity.preOutput.getItemStack().getItem()) {
                    blockEntity.throwItem(serverWorld, prevStack);
                }
                if (!ItemStack.areItemsAndComponentsEqual(blockEntity.preOutput.getItemStack(), prevStack)) {
                    blockEntity.throwItem(serverWorld, prevStack);
                }
            }
            if (ItemStack.areItemsAndComponentsEqual(blockEntity.preOutput.getItemStack(), prevStack)) {
                if (prevStack.getCount() < prevStack.getMaxCount()) {
                    prevStack.setCount(prevStack.getCount() + 1);
                } else {
                    blockEntity.throwItem(serverWorld, prevStack);
                    prevStack.setCount(prevStack.getCount() + 1);
                }

            } else {
                blockEntity.inventory.setStack(OUTPUT_SLOT, blockEntity.preOutput.getItemStack().copy());
                if (this.getWorld() != null && blockEntity.block.isWillBeFailure(this.getWorld())) {
                    blockEntity.inventory.setStack(OUTPUT_SLOT, MIItems.DARK_CUISINE.getDefaultStack().copy());
                }
            }
            blockEntity.preOutput = DEFAULT_WRAPPER_FACTORY.get();

            List<ServerPlayerEntity> nearbyPlayers = PlayerUtils.getNearbyPlayers(serverWorld, blockEntity.pos, 16);
            for (ServerPlayerEntity player : nearbyPlayers) {
                player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0f, 1.0f);
            }

            blockEntity.markDirty();
        }

    }

    public void throwItem(ServerWorld world, ItemStack prevItem) {
        ItemEntity itemEntity = new ItemEntity(world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), prevItem.copy());
        world.spawnEntity(itemEntity);
        this.inventory.setStack(OUTPUT_SLOT, ItemStack.EMPTY);
    }

    public boolean isWorking() {
        return this.workingState == WorkingState.WORKING;
    }


    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, this.inventory.heldStacks);
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
    protected void readData(ReadView view) {
        super.readData(view);
        SimpleInventory inventory = new SimpleInventory(6);
        Inventories.readData(view, inventory.heldStacks);
        this.inventory = inventory;
        this.tickLeft = view.getDouble("TickLeft", 0.0);
        this.workingState = WorkingState.getFromInt(view.getInt("WorkingState", 0));
        Optional<String> pOutputOptional = view.getOptionalString("PreOutput");
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
    public int[] getAvailableSlots(Direction side) {
        return new int[] {OUTPUT_SLOT};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return dir == Direction.DOWN;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return this.inventory.removeStack(slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return this.inventory.removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.setStack(slot, stack);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public void clear() {
        this.inventory.clear();
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
