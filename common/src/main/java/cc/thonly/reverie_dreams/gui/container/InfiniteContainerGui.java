package cc.thonly.reverie_dreams.gui.container;

import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.inventory.InfiniteInventory;
import cc.thonly.reverie_dreams.util.DistributedTickTask;
import cc.thonly.reverie_dreams.util.InfiniteInventoryBlockEntity;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings({"DuplicatedCode", "ConstantValue"})
public abstract class InfiniteContainerGui extends SimpleGui implements GuiCommon {

    public static final int MAX_SLOT_SIZE = 9 * 4;

    private final InfiniteInventoryBlockEntity blockEntity;
    private final BlockState state;
    private final BlockPos pos;
    private final DistributedTickTask closeCheckTickTask;

    private final GuiElementBuilder[] slots = new GuiElementBuilder[MAX_SLOT_SIZE];

    public InfiniteContainerGui(
            ServerPlayer player,
            BlockState state,
            Level world,
            BlockPos pos
    ) {
        super(MenuType.GENERIC_9x4, player, false);

        this.state = state;
        this.blockEntity = (InfiniteInventoryBlockEntity) world.getBlockEntity(pos);
        this.pos = pos;

        this.closeCheckTickTask = DistributedTickTask.createTickTask(() -> {
            if (this.blockEntity == null) {
                return;
            }

            Level level = this.blockEntity.getLevel();

            if (level == null) {
                this.close();
                return;
            }

            if (level.getBlockState(this.pos).getBlock() != this.getSupportBlock()) {
                this.close();
            }

        }, 3);
        this.init();
    }

    public abstract Block getSupportBlock();

    public abstract boolean canInsertItem(ItemStack itemStack);

    @Override
    public void init() {
        this.setTitle(Component.empty().append(this.getSupportBlock().getName()));

        if (this.blockEntity == null) {
            return;
        }

        for (int i = 0; i < MAX_SLOT_SIZE; i++) {
            GuiElementBuilder builder = new GuiElementBuilder(ItemStack.EMPTY);
            this.slots[i] = builder;
            this.setSlot(i, builder);
        }
    }

    @Override
    public ItemStack quickMove(int index) {
        AbstractContainerMenu menu = this.player.containerMenu;

        if (menu == null || this.blockEntity == null) {
            return ItemStack.EMPTY;
        }

        if (index < 0 || index >= menu.slots.size()) {
            return ItemStack.EMPTY;
        }

        ItemStack source = menu.getSlot(index).getItem();

        if (source.isEmpty() || !this.canInsertItem(source)) {
            return ItemStack.EMPTY;
        }

        ItemStack original = source.copy();

        InfiniteInventory inventory = this.blockEntity.getInventory();

        int inserted =
                inventory.addItemAndGetInserted(source);

        if (inserted <= 0) {
            return ItemStack.EMPTY;
        }

        source.shrink(inserted);
        menu.getSlot(index).set(source);

        this.blockEntity.setChanged();
        menu.broadcastChanges();

        return original.copyWithCount(inserted);
    }

    @Override
    public boolean onClick(int containerIndex, ClickType type, ContainerInput action, GuiElement element) {
        AbstractContainerMenu containerMenu = this.player.containerMenu;
        if (containerMenu == null || this.blockEntity == null) {
            return super.onClick(containerIndex, type, action, element);
        }

        if (containerIndex < 0 || containerIndex >= MAX_SLOT_SIZE) {
            return super.onClick(containerIndex, type, action, element);
        }

        InfiniteInventory inventory = this.blockEntity.getInventory();
        ItemStack carried = containerMenu.getCarried();

        switch (type) {
            case MOUSE_LEFT -> {
                if (!carried.isEmpty()) {
                    this.putItem(inventory, containerMenu, carried.getCount());
                } else {
                    this.takeItem(inventory, containerMenu, containerIndex, 1);
                }
            }
            case MOUSE_RIGHT -> {
                if (!carried.isEmpty()) {
                    this.putItem(inventory, containerMenu, 1);
                } else {
                    this.takeItem(inventory, containerMenu, containerIndex, 1);
                }
            }
            case MOUSE_LEFT_SHIFT -> {
                if (!carried.isEmpty()) {
                    this.putItem(inventory, containerMenu, carried.getCount());
                } else {
                    this.quickMoveToPlayer(inventory, containerMenu, containerIndex, 64);
                }
            }
            case MOUSE_RIGHT_SHIFT -> {
                if (!carried.isEmpty()) {
                    putItem(inventory, containerMenu, (int) (carried.getCount() / 2));
                } else {
                    this.quickMoveToPlayer(inventory, containerMenu, containerIndex, 32);
                }
            }
            case MOUSE_MIDDLE -> {
                if (carried.isEmpty()) {
                    this.takeItem(inventory, containerMenu, containerIndex, 64);
                }
            }
            case MOUSE_DOUBLE_CLICK -> {
                if (carried.isEmpty()) {
                    this.takeAllSameItem(inventory, containerMenu, containerIndex);
                }
            }
            default -> {
                return super.onClick(containerIndex, type, action, element);
            }
        }

        containerMenu.broadcastChanges();
        this.blockEntity.setChanged();

        return super.onClick(containerIndex, type, action, element);
    }

    private void quickMoveToPlayer(
            InfiniteInventory inventory,
            AbstractContainerMenu menu,
            int slot,
            int count
    ) {
        ItemStack source = inventory.getItem(slot);

        if (source.isEmpty()) {
            return;
        }

        int available = inventory.getItemCount(slot);

        if (available <= 0) {
            return;
        }

        int amount = Math.min(count, available);
        amount = Math.min(amount, source.getMaxStackSize());

        ItemStack moving = source.copyWithCount(amount);

        Inventory playerInventory = this.player.getInventory();

        int before = moving.getCount();

        playerInventory.add(moving);

        int inserted = before - moving.getCount();

        if (inserted <= 0) {
            return;
        }

        inventory.removeItem(slot, inserted);

        menu.broadcastChanges();
        this.blockEntity.setChanged();
    }

    private void takeItem(
            InfiniteInventory inventory,
            AbstractContainerMenu containerMenu,
            int slot,
            int count
    ) {
        int available = inventory.getItemCount(slot);

        if (available <= 0) {
            return;
        }

        int amount = Math.min(count, available);

        ItemStack result = inventory.removeItem(slot, amount);

        if (!result.isEmpty()) {
            containerMenu.setCarried(result);
        }
    }

    private void putItem(
            InfiniteInventory inventory,
            AbstractContainerMenu containerMenu,
            int count
    ) {
        ItemStack carried = containerMenu.getCarried();

        if (carried.isEmpty()) {
            return;
        }

        if (!this.canInsertItem(carried)) {
            return;
        }

        int amount = Math.min(
                count,
                carried.getCount()
        );

        ItemStack insert =
                carried.copyWithCount(amount);

        inventory.addItem(insert);

        carried.shrink(amount);

        if (carried.isEmpty()) {
            containerMenu.setCarried(ItemStack.EMPTY);
        } else {
            containerMenu.setCarried(carried);
        }
    }

    private void takeAllSameItem(
            InfiniteInventory inventory,
            AbstractContainerMenu containerMenu,
            int slot
    ) {
        ItemStack target = inventory.getSingleItem(slot);
        if (target.isEmpty()) {
            return;
        }

        int total = inventory.getCount(target);
        if (total <= 0) {
            return;
        }

        int maxStackSize = target.getMaxStackSize();
        int amount = Math.min(total, maxStackSize);

        ItemStack result = target.copyWithCount(amount);

        int remaining = amount;

        for (int i = 0; i < inventory.getMaxSize() && remaining > 0; i++) {
            ItemStack item = inventory.getSingleItem(i);
            if (item.isEmpty()) {
                continue;
            }

            if (!ItemStack.isSameItemSameComponents(item, target)) {
                continue;
            }

            int available = inventory.getItemCount(i);
            int remove = Math.min(remaining, available);

            inventory.removeItem(i, remove);

            remaining -= remove;
        }

        if (remaining < amount) {
            result.setCount(amount - remaining);
            containerMenu.setCarried(result);
        }
    }

    @Override
    public void onTick() {
        super.onTick();

        if (this.blockEntity == null) {
            this.close();
            return;
        }

        InfiniteInventory inventory = this.blockEntity.getInventory();

        for (int i = 0; i < this.slots.length; i++) {
            ItemStack itemStack = inventory.getSingleItem(i);
            if (itemStack.isEmpty()) {
                GuiElementBuilder builder = new GuiElementBuilder(ItemStack.EMPTY);
                this.slots[i] = builder;
                this.setSlot(i, builder);
                continue;
            }

            int count = inventory.getItemCount(i);
            GuiElementBuilder builder = new GuiElementBuilder(itemStack);
            Component component = builder.getComponent(DataComponents.CUSTOM_NAME);
            if (component == null) {
                component = itemStack.getHoverName();
            }
            builder.setName(Component.empty().append(component).append(" §b(×%s)".formatted(count)));

            this.slots[i] = builder;
            this.setSlot(i, builder);
        }

        this.closeCheckTickTask.tick();
    }
}