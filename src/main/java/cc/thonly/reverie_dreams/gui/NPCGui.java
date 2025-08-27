package cc.thonly.reverie_dreams.gui;

import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.entity.npc.NPCStates;
import cc.thonly.reverie_dreams.entity.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import cc.thonly.reverie_dreams.item.ModGuiItems;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.ArmorSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCGui extends SimpleGui implements GuiCommon {
    public static final String[][] GRID = {
            {"I", "I", "I", "I", "I", "I", "X", "Q", "W"},
            {"I", "I", "I", "I", "I", "I", "X", "E", "R"},
            {"I", "I", "I", "I", "I", "I", "X", "T", "Y"},
            {"X", "X", "X", "X", "X", "X", "X", "U", "O"},
            {"/", "*", "-", "+", "I", "I", "X", "P", "M"},
            {"X", "X", "X", "X", "X", "X", "X", "N", "B"},
    };
    private final Map<GuiElementBuilder, Integer> builder2index = new HashMap<>();
    private final ServerPlayerEntity player;
    private final NPCEntityImpl npcEntity;

    private GuiElementBuilder npcName;
    private GuiElementBuilder npcMode;
    private GuiElementBuilder npcWorkMode;
    private GuiElementBuilder npcFood;
    private GuiElementBuilder npcHealth;
    private GuiElementBuilder npcArmor;

    public NPCGui(ServerPlayerEntity player, NPCEntityImpl npcEntity) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.player = player;
        this.npcEntity = npcEntity;
        init();
    }

    public void init() {
        int inventory_index = 0;

        this.setTitle(getRoleName());
        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                int slotIndex = row * 9 + col;
                String posChar = GRID[row][col];
                if (posChar.equalsIgnoreCase("X")) {
                    this.setSlot(slotIndex, new GuiElementBuilder()
                            .setItem(ModGuiItems.EMPTY_SLOT));
                }
                if (posChar.equalsIgnoreCase("Q")) {
                    this.npcName = new GuiElementBuilder()
                            .setItem(Items.NAME_TAG)
                            .setItemName(Text.translatable("gui.npc.info"))
                            .setCallback((index, type, action) -> {
                                this.player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                            });
                    this.builder2index.put(this.npcName, slotIndex);
                    this.setSlot(slotIndex, this.npcName);
                }
                if (posChar.equalsIgnoreCase("W")) {
                    this.npcFood = new GuiElementBuilder()
                            .setItem(Items.COOKED_CHICKEN)
                            .setItemName(Text.translatable("gui.npc.info"))
                            .setCallback((index, type, action) -> {
                                this.player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                            });
                    this.builder2index.put(this.npcFood, slotIndex);
                    this.setSlot(slotIndex, this.npcFood);
                }
                if (posChar.equalsIgnoreCase("E")) {
                    this.npcHealth = new GuiElementBuilder()
                            .setItem(Items.GOLDEN_APPLE)
                            .setItemName(Text.translatable("gui.npc.info"))
                            .setCallback((index, type, action) -> {
                                this.player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                            });
                    this.builder2index.put(this.npcHealth, slotIndex);
                    this.setSlot(slotIndex, this.npcHealth);
                }
                if (posChar.equalsIgnoreCase("R")) {
                    this.npcArmor = new GuiElementBuilder()
                            .setItem(Items.IRON_HELMET)
                            .setItemName(Text.translatable("gui.npc.info", getRoleName().getString()))
                            .setCallback((index, type, action) -> {
                                this.player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                            });
                    this.builder2index.put(this.npcArmor, slotIndex);
                    this.setSlot(slotIndex, this.npcArmor);
                }

                if (posChar.equalsIgnoreCase("T")) {
                    BlockPos workingPos = this.npcEntity.getWorkingPos();
                    this.npcMode = new GuiElementBuilder()
                            .setItem(Items.DIAMOND)
                            .setItemName(Text.of("模式开关"))
                            .setLore(List.of
                                    (
                                            this.npcEntity.getNpcState().getTranslateText(),
                                            this.npcEntity.getNpcState() == NPCStates.WORKING ? Text.translatable("gui.npc.mode.work.originpos")
                                                    .append(" : (" + workingPos.getX() + " " + workingPos.getY() + " " + workingPos.getZ() + ")") : Text.of("")

                                    )
                            )
                            .setCallback((index, type, action) -> {
                                this.npcEntity.setNpcState(type.isRight?this.npcEntity.getPreviousState():this.npcEntity.getNextState());
                                this.player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                            })
                    ;
                    this.builder2index.put(this.npcMode, slotIndex);
                    this.setSlot(slotIndex, this.npcMode);
                }
                //工作模式
                if (posChar.equalsIgnoreCase("Y")) {
                    NPCWorkMode currentWorkMode = this.npcEntity.getWorkMode();
                    this.npcWorkMode = new GuiElementBuilder()
                            .setItem(currentWorkMode.getItemDisplay())
                            .setItemName(Text.translatable("gui.npc.work.mode"))
                            .setLore(List.of
                                    (
                                            this.npcEntity.getNpcState().getTranslateText()
                                    )
                            ).setCallback((index, type, action) -> {
                                this.npcEntity.setWorkMode(type.isRight ? this.npcEntity.getWorkMode().getPrevious() : this.npcEntity.getWorkMode().getNext());
                                this.player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                            });
                    this.builder2index.put(this.npcWorkMode, slotIndex);
                    this.setSlot(slotIndex, this.npcWorkMode);
                }


                if (posChar.equalsIgnoreCase("I")) {
                    this.setSlotRedirect(
                            slotIndex,
                            new Slot(this.npcEntity.getInventory(), inventory_index, 0, 0)
                    );
                    inventory_index++;
//                    System.out.println(inventory_index);
//                    System.out.println(this.npcEntity.getInventory().size());
//                    System.out.println(NPCInventoryImpl.FEET);
                }
                if (posChar.equalsIgnoreCase("/")) {
                    this.setSlotRedirect(
                            slotIndex,
                            new ArmorSlot(this.npcEntity.getInventory(), this.npcEntity, EquipmentSlot.HEAD, NPCInventoryImpl.HEAD, 0, 0, PlayerScreenHandler.EMPTY_HELMET_SLOT_TEXTURE)
                    );
                    inventory_index++;
                }
                if (posChar.equalsIgnoreCase("*")) {
                    this.setSlotRedirect(
                            slotIndex,
                            new ArmorSlot(this.npcEntity.getInventory(), this.npcEntity, EquipmentSlot.CHEST, NPCInventoryImpl.CHEST, 0, 0, PlayerScreenHandler.EMPTY_CHESTPLATE_SLOT_TEXTURE)
                    );
                    inventory_index++;
                }

                if (posChar.equalsIgnoreCase("-")) {
                    this.setSlotRedirect(
                            slotIndex,
                            new ArmorSlot(this.npcEntity.getInventory(), this.npcEntity, EquipmentSlot.LEGS, NPCInventoryImpl.LEGS, 0, 0, PlayerScreenHandler.EMPTY_LEGGINGS_SLOT_TEXTURE)
                    );
                    inventory_index++;
                }
                if (posChar.equalsIgnoreCase("+")) {
                    this.setSlotRedirect(
                            slotIndex,
                            new ArmorSlot(this.npcEntity.getInventory(), this.npcEntity, EquipmentSlot.FEET, NPCInventoryImpl.FEET, 0, 0, PlayerScreenHandler.EMPTY_BOOTS_SLOT_TEXTURE)
                    );
                    inventory_index++;
                }
            }
        }
    }

    public Text getRoleName() {
        return this.npcEntity.hasCustomName() ? this.npcEntity.getCustomName() : this.npcEntity.getName();
    }

    @Override
    public void onTick() {
        super.onTick();
        this.npcName.setItemName(Text.translatable("gui.npc.info.name", getRoleName().getString()));
        this.npcFood.setItemName(Text.translatable("gui.npc.info.food"));
        this.npcFood.setLore(
                List.of(
                        Text.translatable("gui.npc.info.food.nutrition", this.npcEntity.getNutrition() + " / 20.0"),
                        Text.translatable("gui.npc.info.food.saturation", this.npcEntity.getSaturation() + " / 20.0")
                )
        );
        this.npcHealth.setItemName(Text.translatable("gui.npc.info.health", this.npcEntity.getHealth(), this.npcEntity.getMaxHealth()));
        this.npcArmor.setItemName(Text.translatable("gui.npc.info.armor", this.npcEntity.getArmor()));
        BlockPos workingPos = this.npcEntity.getWorkingPos();
        this.npcMode.setLore(List.of
                (
                        this.npcEntity.getNpcState().getTranslateText(),
                        this.npcEntity.getNpcState() == NPCStates.WORKING ? Text.translatable("gui.npc.mode.work.originpos").append(" : (" + workingPos.getX() + " " + workingPos.getY() + " " + workingPos.getZ() + ")") : Text.of("")
                )
        );
        NPCWorkMode currentWorkMode = this.npcEntity.getWorkMode();
        this.npcWorkMode.setItem(currentWorkMode.getItemDisplay());
        this.npcWorkMode.setLore(List.of
                (
                        this.npcEntity.getWorkMode().translationKey()
                )
        );

//        System.out.println( this.npcEntity.getNpcState().getId());
        this.builder2index.forEach((builder, index) -> {
            this.setSlot(index, builder);
        });

    }

    @Override
    public void onOpen() {
        super.onOpen();
        this.npcEntity.setPaused(true);
    }

    @Override
    public void onClose() {
        super.onClose();
        this.npcEntity.setPaused(false);
    }

    public static int size() {
        return NPCInventoryImpl.MAX_SIZE;
    }
}
