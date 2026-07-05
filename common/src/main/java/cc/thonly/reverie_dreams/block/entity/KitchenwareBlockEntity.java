package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.block.KitchenBlockType;
import cc.thonly.reverie_dreams.block.kitchen.AbstractKitchenwareBlock;
import cc.thonly.reverie_dreams.gui.block.KitchenBlockGui;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDFoodItems;
import cc.thonly.reverie_dreams.util.entity.PlayerHelper;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
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
    public static final Supplier<IngredientStack> DEFAULT_WRAPPER_FACTORY = IngredientStack::empty;
    public static final Map<UUID, Set<KitchenBlockGui<?>>> SESSIONS = new Object2ObjectOpenHashMap<>();
    public static final int OUTPUT_SLOT = 5;
    private SimpleContainer inventory = new SimpleContainer(6);
    @Nullable
    private KitchenRecipeType.TypeInstance recipeType;
    private IngredientStack preOutput = DEFAULT_WRAPPER_FACTORY.get();
    private Double tickLeft = 0.0;
    private Double tickMax = 0.0;
    private DoubleUnaryOperator bonusOperator;
    private UUID uuid = UUID.randomUUID();
    private final AbstractKitchenwareBlock block;
    private WorkingState workingState = WorkingState.NONE;

    public KitchenwareBlockEntity(BlockPos pos, BlockState state) {
        super(RDBlockEntityTypes.KITCHENWARE_BLOCK.value(), pos, state);
        Block block = state.getBlock();
        this.block = (AbstractKitchenwareBlock) block;
        this.bonusOperator = this.block.getBonusOperator();
        this.recipeType = this.getTypeInstance();
    }

    public static void tick(Level world, BlockPos blockPos, BlockState state, KitchenwareBlockEntity self) {
        KitchenwareBlockEntity blockEntity = self.get();
        KitchenRecipeType.TypeInstance typeInstance = blockEntity.getTypeInstance();
        if (typeInstance == null) {
            return;
        }
        if (world.isClientSide()) {
            return;
        }
        ServerLevel serverWorld = (ServerLevel) world;
        BlockPos pos = self.getBlockPos();
        if (self.preOutput != null && !self.preOutput.isEmpty()) {
            self.workingState = WorkingState.WORKING;
            self.tickLeft -= self.bonusOperator.applyAsDouble(1.0);
            serverWorld.sendParticles(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1, 0, 0.5, 0, 0.1);
            if (self.tickLeft <= 0.0) {
                self.tickLeft = 0.0;
                self.tickMax = 1.0;
                self.workingState = WorkingState.NONE;
                self.handleOutput();
            }
            self.setChanged();
        } else {
            self.workingState = WorkingState.NONE;
        }
        if (self.tickLeft > 0) {
            if (self.level != null && !self.level.isClientSide()) {
                self.level.sendBlockUpdated(self.worldPosition, self.getBlockState(), self.getBlockState(), 3);
            }
        }
    }

    public boolean testIngredients(KitchenRecipe recipe, List<IngredientStack> ingredients) {
        KitchenRecipeType recipeType = KitchenRecipeType.getInstance();
        return recipeType.isMatches(recipe, ingredients);
    }

    public void consume(List<IngredientStack> ingredients) {
        SimpleContainer inventory = this.getInventory();
        for (int i = 0; i < 5; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                UseRemainder useRemainderComponent = stack.get(DataComponents.USE_REMAINDER);
                if (useRemainderComponent != null) {
                    ItemStack itemStack = useRemainderComponent.convertInto().create();
                    this.throwItem((ServerLevel) this.getLevel(), itemStack);
                }
                stack.shrink(1);
            }
        }
    }

    public void setOutput(ItemStack itemStack, Double time) {
        this.setOutput(IngredientStack.of(itemStack), time);
    }

    public void setOutput(IngredientStack ingredientStack, Double time) {
        this.setPreOutput(ingredientStack);
        this.setTickLeft(time);
        this.setTickMax(time);
        this.setChanged();
    }

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
        } else if (!blockEntity.isWorking() && !blockEntity.preOutput.getLazyStack().isEmpty()) {
            ItemStack prevStack = blockEntity.inventory.getItem(OUTPUT_SLOT);
            if (!prevStack.isEmpty()) {
                Item item = prevStack.getItem();
                if (item != blockEntity.preOutput.getLazyStack().getItem()) {
                    blockEntity.throwItem(serverWorld, prevStack);
                }
                if (!ItemStack.isSameItemSameComponents(blockEntity.preOutput.getLazyStack(), prevStack)) {
                    blockEntity.throwItem(serverWorld, prevStack);
                }
            }
            if (ItemStack.isSameItemSameComponents(blockEntity.preOutput.getLazyStack(), prevStack)) {
                if (prevStack.getCount() < prevStack.getMaxStackSize()) {
                    prevStack.setCount(prevStack.getCount() + 1);
                } else {
                    blockEntity.throwItem(serverWorld, prevStack);
                    prevStack.setCount(prevStack.getCount() + 1);
                }

            } else {
                blockEntity.inventory.setItem(OUTPUT_SLOT, blockEntity.preOutput.getLazyStack().copy());
                if (this.getLevel() != null && blockEntity.block.isWillBeFailure(this.getLevel())) {
                    blockEntity.inventory.setItem(OUTPUT_SLOT, RDFoodItems.DARK_CUISINE.createStack().copy());
                }
            }
            blockEntity.preOutput = DEFAULT_WRAPPER_FACTORY.get();

            List<ServerPlayer> nearbyPlayers = PlayerHelper.getNearbyPlayers(serverWorld, blockEntity.worldPosition, 16);
            for (ServerPlayer player : nearbyPlayers) {
                SoundEventPlayUtils.playUISound(player, SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 1.0f);
            }

            blockEntity.setChanged();
        }

    }

    public KitchenRecipeType.@Nullable TypeInstance getTypeInstance() {
        if (this.recipeType != null) {
            return this.recipeType;
        }
        Level level = this.level;
        if (level != null) {
            RegistryAccess registryAccess = level.registryAccess();
            this.recipeType = KitchenBlockType.getMatchType(registryAccess, this.block);
            return this.recipeType;
        }
        return null;
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
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ContainerHelper.saveAllItems(view, this.inventory.getItems());
        view.putDouble("TickMax", this.tickMax);
        view.putDouble("TickLeft", this.tickLeft);
        view.putInt("WorkingState", this.workingState.getId());
        view.storeNullable("PreOutput", IngredientStack.CODEC, this.preOutput);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        SimpleContainer inventory = new SimpleContainer(6);
        ContainerHelper.loadAllItems(view, inventory.getItems());
        this.inventory = inventory;
        this.tickMax = view.getDoubleOr("TickMax", view.getIntOr("TotalCookingTime", 0));
        this.tickLeft = view.getDoubleOr("TickLeft", view.getIntOr("CookingTime", 0));
        this.workingState = WorkingState.getFromInt(view.getIntOr("WorkingState", 0));
        Optional<IngredientStack> preOutputOptional = view.read("PreOutput", IngredientStack.CODEC);
        if (preOutputOptional.isPresent()) {
            preOutputOptional.ifPresent(preOutput -> this.preOutput = preOutput);
        } else {
            ItemStack target = view.read("cooking", ItemStack.CODEC).orElse(ItemStack.EMPTY);
            this.preOutput = IngredientStack.of(target);
        }
    }

    public KitchenwareBlockEntity get() {
        return this;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{OUTPUT_SLOT};
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

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(this::toString, LogUtils.getLogger())) {
            TagValueOutput tagValue = TagValueOutput.createWithContext(logging, registries);
            this.saveAdditional(tagValue);
            return tagValue.buildResult();
        } catch (Exception any) {
            return super.getUpdateTag(registries);
        }
    }

    @Override
    public @org.jspecify.annotations.Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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
