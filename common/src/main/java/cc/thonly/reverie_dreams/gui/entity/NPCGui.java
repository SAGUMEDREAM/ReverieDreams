package cc.thonly.reverie_dreams.gui.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.util.PredicateSlot;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCGui extends SimpleGui implements GuiCommon {
    public static final String[][] GRID = {
            {"/", "X", "I", "I", "I", "I", "X", "Q", "W"},
            {"*", "X", "I", "I", "I", "I", "X", "E", "R"},
            {"-", "X", "I", "I", "I", "I", "X", "T", "Y"},
            {"+", "X", "I", "I", "I", "I", "X", "U", "O"},
            {"X", "X", "X", "X", "I", "I", "X", "P", "M"},
            {"I", "I", "X", "X", "X", "X", "X", "N", "B"},
    };
    private final Map<GuiElementBuilder, Integer> builder2index = new HashMap<>();
    private final ServerPlayer player;
    private final NPCSimpleEntity npc;

    private GuiElementBuilder npcName;
    private GuiElementBuilder npcMode;
    private GuiElementBuilder npcWorkMode;
    private GuiElementBuilder npcFood;
    private GuiElementBuilder npcHealth;
    private GuiElementBuilder npcArmor;
    private GuiElementBuilder npcXp;
    private GuiElementBuilder npcAutoPick;
    private GuiElementBuilder npcFavorability;

    public NPCGui(ServerPlayer player, NPCSimpleEntity npc) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.npc = npc;
        init();
    }

    public void init() {
        int inventory_index = 0;

        this.setTitle(Component.empty()
                               .append(Component.translatable("space.-8"))
                               .append(Component.literal("\ub000")
                                                .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)
                                                                      .withFont(new FontDescription.Resource(ReverieDreams.id("reverie_dreams")))))
                               .append(Component.translatable("space.-168"))
                               .append(getRoleName()));

        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                int slotIndex = row * 9 + col;
                String posChar = GRID[row][col];
//                if (posChar.equalsIgnoreCase("X")) {
//                    this.setSlot(slotIndex, new GuiElementBuilder()
//                            .setItem(RDGuiItems.EMPTY_SLOT.asItem()));
//                }
//                if (posChar.equalsIgnoreCase("A1")) {
//                    this.setSlot(slotIndex, new GuiElementBuilder()
//                            .setItem(RDGuiItems.HEAD_SLOT.asItem()));
//                }
//                if (posChar.equalsIgnoreCase("A2")) {
//                    this.setSlot(slotIndex, new GuiElementBuilder()
//                            .setItem(RDGuiItems.CHEST_SLOT.asItem()));
//                }
//                if (posChar.equalsIgnoreCase("A3")) {
//                    this.setSlot(slotIndex, new GuiElementBuilder()
//                            .setItem(RDGuiItems.LEG_SLOT.asItem()));
//                }
//                if (posChar.equalsIgnoreCase("A4")) {
//                    this.setSlot(slotIndex, new GuiElementBuilder()
//                            .setItem(RDGuiItems.FEET_SLOT.asItem()));
//                }
                if (posChar.equalsIgnoreCase("Q")) {
                    this.npcName = new GuiElementBuilder()
                            .setItem(Items.NAME_TAG)
                            .setItemName(Component.translatable("gui.npc.info"))
                            .setComponent(RDDataComponentTypes.SHOW_ONLY.value(), Unit.INSTANCE)
                            .setCallback((index, type, action, basedGui) -> {
                                SoundEventPlayUtils.playUISound(this.player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                            });
                    this.builder2index.put(this.npcName, slotIndex);
                    this.setSlot(slotIndex, this.npcName);
                }
                if (posChar.equalsIgnoreCase("W")) {
                    this.npcFood = new GuiElementBuilder()
                            .setItem(Items.COOKED_CHICKEN)
                            .setItemName(Component.translatable("gui.npc.info"))
                            .setComponent(RDDataComponentTypes.SHOW_ONLY.value(), Unit.INSTANCE)
                            .setCallback((index, type, action, basedGui) -> {
                                SoundEventPlayUtils.playUISound(this.player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                            });
                    this.builder2index.put(this.npcFood, slotIndex);
                    this.setSlot(slotIndex, this.npcFood);
                }
                if (posChar.equalsIgnoreCase("E")) {
                    this.npcHealth = new GuiElementBuilder()
                            .setItem(Items.GOLDEN_APPLE)
                            .setItemName(Component.translatable("gui.npc.info"))
                            .setComponent(RDDataComponentTypes.SHOW_ONLY.value(), Unit.INSTANCE)
                            .setCallback((index, type, action, basedGui) -> {
                                SoundEventPlayUtils.playUISound(this.player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                            });
                    this.builder2index.put(this.npcHealth, slotIndex);
                    this.setSlot(slotIndex, this.npcHealth);
                }
                if (posChar.equalsIgnoreCase("R")) {
                    this.npcArmor = new GuiElementBuilder()
                            .setItem(Items.IRON_HELMET)
                            .setItemName(Component.translatable("gui.npc.info", getRoleName().getString()))
                            .setComponent(RDDataComponentTypes.SHOW_ONLY.value(), Unit.INSTANCE)
                            .setCallback((index, type, action, basedGui) -> {
                                SoundEventPlayUtils.playUISound(this.player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                            });
                    this.builder2index.put(this.npcArmor, slotIndex);
                    this.setSlot(slotIndex, this.npcArmor);
                }

                if (posChar.equalsIgnoreCase("T")) {
                    BlockPos workingPos = this.npc.getWorkingPos();
                    this.npcMode = new GuiElementBuilder()
                            .setItem(Items.DIAMOND)
                            .setItemName(Component.translatable("gui.npc.work.button"))
                            .setComponent(RDDataComponentTypes.SHOW_ONLY.value(), Unit.INSTANCE)
                            .setLore(List.of
                                                 (
                                                         this.npc.getNpcState().getTranslateText(),
                                                         this.npc.getNpcState() == NPCStates.WORKING ? Component.translatable("gui.npc.mode.work.originpos")
                                                                                                                .append(" : (" + workingPos.getX() + " " + workingPos.getY() + " " + workingPos.getZ() + ")") : Component.nullToEmpty("")

                                                 )
                            )
                            .setCallback((index, type, action, basedGui) -> {
                                this.npc.setNpcState(type.isRight ? this.npc.getPreviousState() : this.npc.getNextState());
                                SoundEventPlayUtils.playUISound(this.player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                            })
                    ;
                    this.builder2index.put(this.npcMode, slotIndex);
                    this.setSlot(slotIndex, this.npcMode);
                }
                // 工作模式
                if (posChar.equalsIgnoreCase("Y")) {
                    NPCWorkMode currentWorkMode = this.npc.getWorkMode();
                    this.npcWorkMode = new GuiElementBuilder()
                            .setItem(currentWorkMode.getItemDisplay().value())
                            .setItemName(Component.translatable("gui.npc.work.mode"))
                            .setComponent(RDDataComponentTypes.SHOW_ONLY.value(), Unit.INSTANCE)
                            .setCallback((index, type, action, basedGui) -> {
                                SoundEventPlayUtils.playUISound(this.player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                                NPCWorkGui npcWorkGui = new NPCWorkGui(this.player, this.npc);
                                npcWorkGui.open();
                            });
                    this.builder2index.put(this.npcWorkMode, slotIndex);
                    this.setSlot(slotIndex, this.npcWorkMode);
                }
                if (posChar.equalsIgnoreCase("U")) {
                    this.npcXp = new GuiElementBuilder()
                            .setItem(Items.EXPERIENCE_BOTTLE)
                            .setItemName(Component.translatable("gui.npc.info.xp", this.npc.getStoredExperience()))
                            .setComponent(RDDataComponentTypes.SHOW_ONLY.value(), Unit.INSTANCE)
                            .setLore(List.of(
                                    Component.translatable("gui.npc.info.xp.button")
                            ))
                            .setCallback((index, type, action, basedGui) -> {
                                int experienceAmount = this.npc.getStoredExperience();
                                if (experienceAmount > 0) {
                                    SoundEventPlayUtils.playUISound(this.player, SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                                }
                                this.player.giveExperiencePoints(experienceAmount);
                                this.npc.setStoredExperience(0);
                                SoundEventPlayUtils.playUISound(this.player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                            });
                    this.builder2index.put(this.npcXp, slotIndex);
                    this.setSlot(slotIndex, this.npcXp);
                }
                if (posChar.equalsIgnoreCase("O")) {
                    this.npcAutoPick = new GuiElementBuilder()
                            .setItem(Items.NETHERITE_SCRAP)
                            .setName(Component.translatable("gui.npc.info.auto-pick"))
                            .setComponent(RDDataComponentTypes.SHOW_ONLY.value(), Unit.INSTANCE)
                            .setComponent(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, this.npc.isAutoPick())
                            .setCallback((index, type, action, basedGui) -> {
                                this.npc.setAutoPick(!this.npc.isAutoPick());
                                SoundEventPlayUtils.playUISound(this.player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                            });
                    this.builder2index.put(this.npcAutoPick, slotIndex);
                    this.setSlot(slotIndex, this.npcAutoPick);
                }
                if (posChar.equalsIgnoreCase("P")) {
                    this.npcFavorability = new GuiElementBuilder()
                            .setItem(Items.APPLE)
                            .setName(Component.translatable("gui.npc.info.favorability", this.npc.getFavorabilityContainer().get(this.player.getUUID())))
                            .setComponent(RDDataComponentTypes.SHOW_ONLY.value(), Unit.INSTANCE)
                            .setComponent(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, this.npc.isAutoPick())
                            .setCallback((index, type, action, basedGui) -> {
                                SoundEventPlayUtils.playUISound(this.player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                            });
                    this.builder2index.put(this.npcFavorability, slotIndex);
                    this.setSlot(slotIndex, this.npcFavorability);
                }

                if (posChar.equalsIgnoreCase("I")) {
                    // 如果是最后一行的第 0 列 / 第 1 列，分别绑定主手与副手
                    if (row == GRID.length - 1 && col == 0) {
                        this.setSlot(
                                slotIndex,
                                new PredicateSlot(this.npc.getInventory(), NPCInventoryImpl.MAIN_HAND, 0, 0, (stack) -> !this.npc.isLockSlot())
                        );
                    } else if (row == GRID.length - 1 && col == 1) {
                        this.setSlot(
                                slotIndex,
                                new PredicateSlot(this.npc.getInventory(), NPCInventoryImpl.OFF_HAND, 0, 0, (stack) -> !this.npc.isLockSlot())
                        );
                    } else {
                        // 普通背包格，从 inventory_index 顺序分配
                        this.setSlot(
                                slotIndex,
                                new PredicateSlot(this.npc.getInventory(), inventory_index, 0, 0, (stack) -> !this.npc.isLockSlot())
                        );
                        inventory_index++;
                    }
                }
                if (posChar.equalsIgnoreCase("/")) {
                    this.setSlot(
                            slotIndex,
                            new ArmorSlot(this.npc.getInventory(), this.npc, EquipmentSlot.HEAD, NPCInventoryImpl.HEAD, 0, 0, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET)
                    );
                }
                if (posChar.equalsIgnoreCase("*")) {
                    this.setSlot(
                            slotIndex,
                            new ArmorSlot(this.npc.getInventory(), this.npc, EquipmentSlot.CHEST, NPCInventoryImpl.CHEST, 0, 0, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE)
                    );
                }

                if (posChar.equalsIgnoreCase("-")) {
                    this.setSlot(
                            slotIndex,
                            new ArmorSlot(this.npc.getInventory(), this.npc, EquipmentSlot.LEGS, NPCInventoryImpl.LEGS, 0, 0, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS)
                    );
                }
                if (posChar.equalsIgnoreCase("+")) {
                    this.setSlot(
                            slotIndex,
                            new ArmorSlot(this.npc.getInventory(), this.npc, EquipmentSlot.FEET, NPCInventoryImpl.FEET, 0, 0, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS)
                    );
                }
            }
        }
    }

    public Component getRoleName() {
        return this.npc.hasCustomName() ? this.npc.getCustomName() : this.npc.getName();
    }

    @Override
    public void onTick() {
        super.onTick();
        this.npcName.setItemName(Component.translatable("gui.npc.info.name", getRoleName().getString()));
        this.npcFood.setItemName(Component.translatable("gui.npc.info.food"));
        this.npcFood.setLore(
                List.of(
                        Component.translatable("gui.npc.info.food.nutrition", this.npc.getFoodData().getNutrition() + " / 20.0"),
                        Component.translatable("gui.npc.info.food.saturation", this.npc.getFoodData().getSaturation() + " / 20.0")
                )
        );
        this.npcHealth.setItemName(Component.translatable("gui.npc.info.health", this.npc.getHealth(), this.npc.getMaxHealth()));
        this.npcArmor.setItemName(Component.translatable("gui.npc.info.armor", this.npc.getArmorValue()));
        BlockPos workingPos = this.npc.getWorkingPos();
        this.npcMode.setLore(List.of
                                         (
                                                 this.npc.getNpcState().getTranslateText(),
                                                 this.npc.getNpcState() == NPCStates.WORKING ? Component.translatable("gui.npc.mode.work.originpos").append(" : (" + workingPos.getX() + " " + workingPos.getY() + " " + workingPos.getZ() + ")") : Component.nullToEmpty("")
                                         )
        );
        NPCWorkMode currentWorkMode = this.npc.getWorkMode();
        this.npcWorkMode.setItem(currentWorkMode.getItemDisplay().value());
        this.npcWorkMode.setLore(List.of
                                             (
                                                     this.npc.getWorkMode().translationKey()
                                             )
        );

        this.npcXp.setItemName(Component.translatable("gui.npc.info.xp", this.npc.getStoredExperience()));
        this.npcAutoPick.setItem(Items.NETHERITE_SCRAP);
        this.npcAutoPick.setItemName(Component.translatable("gui.npc.info.auto-pick"));
        this.npcAutoPick.setComponent(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, this.npc.isAutoPick());
        this.npcFavorability.setName(Component.translatable("gui.npc.info.favorability", this.npc.getFavorabilityContainer().get(this.player.getUUID())));
        //        System.out.println( this.source.getNpcState().getId());
        this.builder2index.forEach((builder, index) -> {
            this.setSlot(index, builder);
        });

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