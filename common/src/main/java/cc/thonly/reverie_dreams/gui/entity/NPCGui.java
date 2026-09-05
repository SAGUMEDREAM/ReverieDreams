package cc.thonly.reverie_dreams.gui.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCMenuType;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.gui.PlayerHeadInfo;
import cc.thonly.reverie_dreams.gui.slot.PredicateSlot;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import cc.thonly.reverie_dreams.registry.content.NPCMenuTypes;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.BaseSlotGui;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ArmorSlot;

import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.ArrayList;
import java.util.List;

public class NPCGui extends SimpleGui implements GuiCommon {
    public static final String[][] GRID = {
            {"/", "X", "I", "I", "I", "I", "X", "A", "A"},
            {"*", "X", "I", "I", "I", "I", "X", "A", "A"},
            {"-", "X", "I", "I", "I", "I", "X", "A", "A"},
            {"+", "X", "I", "I", "I", "I", "X", "A", "A"},
            {"X", "X", "X", "X", "I", "I", "X", "A", "A"},
            {"I", "I", "X", "X", "X", "X", "X", "A", "A"}
    };

    private static final int PREVIOUS_PAGE_SLOT = 52;
    private static final int NEXT_PAGE_SLOT = 53;
    private static final int MENU_PAGE_SIZE = 10;

    private static final List<Integer> MENU_SLOTS = createMenuSlots();

    private final ServerPlayer player;
    private final NPCSimpleEntity npc;

    private int menuPage = 0;

    public NPCGui(ServerPlayer player, NPCSimpleEntity npc) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.npc = npc;
        this.init();
    }

    private static List<Integer> createMenuSlots() {
        List<Integer> slots = new ArrayList<>();

        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                if (!GRID[row][col].equals("A")) {
                    continue;
                }

                int slot = row * 9 + col;

                if (slot == PREVIOUS_PAGE_SLOT || slot == NEXT_PAGE_SLOT) {
                    continue;
                }

                slots.add(slot);
            }
        }

        return List.copyOf(slots);
    }

    @Override
    public void init() {
        this.updateName();
        int inventoryIndex = 0;

        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                int slotIndex = row * 9 + col;
                String posChar = GRID[row][col];

                if (posChar.equals("A")) {
                    continue;
                }

                if (posChar.equalsIgnoreCase("I")) {
                    if (row == GRID.length - 1 && col == 0) {
                        this.setSlotRedirect(
                                slotIndex,
                                new PredicateSlot(
                                        this.npc.getInventory(),
                                        NPCInventoryImpl.MAIN_HAND,
                                        0,
                                        0,
                                        stack -> !this.npc.isLockSlot()
                                )
                        );
                    } else if (row == GRID.length - 1 && col == 1) {
                        this.setSlotRedirect(
                                slotIndex,
                                new PredicateSlot(
                                        this.npc.getInventory(),
                                        NPCInventoryImpl.OFF_HAND,
                                        0,
                                        0,
                                        stack -> !this.npc.isLockSlot()
                                )
                        );
                    } else {
                        this.setSlotRedirect(
                                slotIndex,
                                new PredicateSlot(
                                        this.npc.getInventory(),
                                        inventoryIndex,
                                        0,
                                        0,
                                        stack -> !this.npc.isLockSlot()
                                )
                        );
                        inventoryIndex++;
                    }

                    continue;
                }

                if (posChar.equals("/")) {
                    this.setSlotRedirect(
                            slotIndex,
                            new ArmorSlot(
                                    this.npc.getInventory(),
                                    this.npc,
                                    EquipmentSlot.HEAD,
                                    NPCInventoryImpl.HEAD,
                                    0,
                                    0,
                                    InventoryMenu.EMPTY_ARMOR_SLOT_HELMET
                            )
                    );
                    continue;
                }

                if (posChar.equals("*")) {
                    this.setSlotRedirect(
                            slotIndex,
                            new ArmorSlot(
                                    this.npc.getInventory(),
                                    this.npc,
                                    EquipmentSlot.CHEST,
                                    NPCInventoryImpl.CHEST,
                                    0,
                                    0,
                                    InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE
                            )
                    );
                    continue;
                }

                if (posChar.equals("-")) {
                    this.setSlotRedirect(
                            slotIndex,
                            new ArmorSlot(
                                    this.npc.getInventory(),
                                    this.npc,
                                    EquipmentSlot.LEGS,
                                    NPCInventoryImpl.LEGS,
                                    0,
                                    0,
                                    InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS
                            )
                    );
                    continue;
                }

                if (posChar.equals("+")) {
                    this.setSlotRedirect(
                            slotIndex,
                            new ArmorSlot(
                                    this.npc.getInventory(),
                                    this.npc,
                                    EquipmentSlot.FEET,
                                    NPCInventoryImpl.FEET,
                                    0,
                                    0,
                                    InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS
                            )
                    );
                }
            }
        }

        this.updateMenus();
    }

    private void updateName() {
        this.setTitle(Component.empty()
                .append(Component.translatable("space.-8"))
                .append(Component.literal("\ub000")
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.WHITE)
                                .withFont(new FontDescription.Resource(
                                        ReverieDreams.id("reverie_dreams")
                                ))))
                .append(Component.translatable("space.-168"))
                .append(this.getRoleName()));
    }

    private void updateMenus() {
        List<NPCMenuType> menuTypes =
                NPCMenuTypes.get(this.player, this.npc);

        int pageCount = Math.max(
                1,
                (menuTypes.size() + MENU_PAGE_SIZE - 1) / MENU_PAGE_SIZE
        );

        if (this.menuPage >= pageCount) {
            this.menuPage = pageCount - 1;
        }

        if (this.menuPage < 0) {
            this.menuPage = 0;
        }

        int startIndex = this.menuPage * MENU_PAGE_SIZE;

        for (int i = 0; i < MENU_SLOTS.size(); i++) {
            int slotIndex = MENU_SLOTS.get(i);
            int menuIndex = startIndex + i;

            if (menuIndex >= menuTypes.size()) {
                this.setSlot(
                        slotIndex,
                        new GuiElementBuilder(ItemStack.EMPTY)
                );
                continue;
            }

            NPCMenuType menuType = menuTypes.get(menuIndex);

            GuiElementBuilder builder =
                    menuType.create(
                            this.player,
                            this.npc,
                            this
                    );

            this.setSlot(slotIndex, builder);
        }

        this.updatePagination(pageCount);
    }

    private void updatePagination(int pageCount) {
        GuiElementBuilder previous =
                new GuiElementBuilder(Items.PLAYER_HEAD)
                        .setName(
                                Component.translatable(
                                        "gui.npc.page.previous"
                                )
                        )
                        .setProfileSkinTexture(
                                PlayerHeadInfo.GUI_PREVIOUS_PAGE
                        )
                        .setComponent(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(
                                        true,
                                        ReferenceSortedSets.emptySet()
                                )
                        )
                        .setCallback((index, type, action, basedGui) -> {
                            if (this.menuPage > 0) {
                                this.menuPage--;
                                this.updateMenus();
                                this.player.containerMenu.broadcastChanges();
                                SoundEventPlayUtils.playUISound(
                                        player,
                                        SoundEvents.UI_BUTTON_CLICK.value(),
                                        1.0f,
                                        1.0f
                                );
                            }
                        });

        GuiElementBuilder next =
                new GuiElementBuilder(Items.PLAYER_HEAD)
                        .setName(
                                Component.translatable(
                                        "gui.npc.page.next"
                                )
                        )
                        .setProfileSkinTexture(
                                PlayerHeadInfo.GUI_NEXT_PAGE
                        )
                        .setComponent(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(
                                        true,
                                        ReferenceSortedSets.emptySet()
                                )
                        )
                        .setCallback((index, type, action, basedGui) -> {
                            if (this.menuPage < pageCount - 1) {
                                this.menuPage++;
                                this.updateMenus();
                                this.player.containerMenu.broadcastChanges();
                                SoundEventPlayUtils.playUISound(
                                        player,
                                        SoundEvents.UI_BUTTON_CLICK.value(),
                                        1.0f,
                                        1.0f
                                );
                            }
                        });

        if (this.menuPage <= 0) {
            previous.setName(Component.translatable("gui.npc.page.previous.disabled"))
                    .setComponent(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(
                                    true,
                                    ReferenceSortedSets.emptySet()
                            )
                    ).setCallback((i, clickType, containerInput, slotBasedGui) -> {
                        SoundEventPlayUtils.playUISound(
                                player,
                                SoundEvents.UI_BUTTON_CLICK.value(),
                                1.0f,
                                1.0f
                        );
                    });
        }

        if (this.menuPage >= pageCount - 1) {
            next.setName(Component.translatable("gui.npc.page.next.disabled"))
                    .setComponent(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(
                                    true,
                                    ReferenceSortedSets.emptySet()
                            )
                    ).setCallback((i, clickType, containerInput, slotBasedGui) -> {
                        SoundEventPlayUtils.playUISound(
                                player,
                                SoundEvents.UI_BUTTON_CLICK.value(),
                                1.0f,
                                1.0f
                        );
                    });
        }

        this.setSlot(
                PREVIOUS_PAGE_SLOT,
                previous
        );

        this.setSlot(
                NEXT_PAGE_SLOT,
                next
        );
    }

    public Component getRoleName() {
        return this.npc.hasCustomName()
                ? this.npc.getCustomName()
                : this.npc.getName();
    }

    @Override
    public void onTick() {
        super.onTick();
        this.updateMenus();
    }

    @Override
    public void onOpen() {
        super.onOpen();
        this.npc.setPaused(true);
    }

    @Override
    public void onPlayerClose(boolean success) {
        super.onPlayerClose(success);
        this.npc.setPaused(false);
    }

    public static int size() {
        return NPCInventoryImpl.MAX_SIZE;
    }
}