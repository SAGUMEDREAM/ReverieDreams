package cc.thonly.reverie_dreams.gui;

import cc.thonly.reverie_dreams.data.npc.NPCRole;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleFastEntity;
import cc.thonly.reverie_dreams.item.builder.RoleCard;
import cc.thonly.reverie_dreams.item.template.RoleCardItem;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class RoleSelectGui extends SimpleGui implements GuiCommon {
    private final List<NPCRole> roles;
    private final ItemStack itemStack;
    private final BlockPos clickedPos;

    public RoleSelectGui(ItemStack itemStack, BlockPos clickedPos, ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.roles = new ArrayList<>();
        this.itemStack = itemStack;
        this.clickedPos = clickedPos;
        Optional<RoleCard> componentOptional = RoleCardItem.getRoleCardComponent(itemStack);
        if (componentOptional.isEmpty()) {
            this.close();
            return;
        }
        RoleCard roleCardComponent = componentOptional.get();
        roleCardComponent.stream().forEach(this.roles::add);
        this.setTitle(itemStack.getDisplayName());
        this.init();
    }

    @Override
    public void init() {
        boolean renderedRandom = false;
        Iterator<NPCRole> iterator = this.roles.iterator();
        for (int i = 0; i < this.getVirtualSize(); i++) {
            boolean hasNext = iterator.hasNext();
            if (!hasNext) {
                if (!renderedRandom) {
                    renderedRandom = true;
                    var element = new GuiElementBuilder(Items.PAPER);
                    element.setName(Component.translatable("dialog.text.random"));
                    element.setCallback((index, clickTypeWrapper, clickType) -> {
                        this.player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                        if (!clickTypeWrapper.isLeft) {
                            return;
                        }
                        if (this.roles.isEmpty()) {
                            return;
                        }
                        int randomIdx = ThreadLocalRandom.current().nextInt(this.roles.size());
                        NPCRole npcRole = this.roles.get(randomIdx);
                        ServerLevel serverLevel = this.player.serverLevel();
                        EntityType<NPCRoleFastEntity> entityType = npcRole.get();
                        entityType.spawn(serverLevel, this.clickedPos.relative(Direction.UP), EntitySpawnReason.SPAWN_ITEM_USE);
                        if (!this.isCreativeMode()) {
                            this.itemStack.shrink(1);
                        }
                        this.close();
                    });
                    this.setSlot(i, element);
                    continue;
                }
                var element = new GuiElementBuilder(Items.AIR);
                this.setSlot(i, element);
                continue;
            }
            NPCRole next = iterator.next();
            ItemStack itemStack = next.getSpawnEgg().getDefaultInstance();
            var element = new GuiElementBuilder(itemStack);
            element.setItem(next.getSpawnEgg());
            element.setName(Component.translatable(next.translateKey()));
            element.setCallback((index, clickTypeWrapper, clickType) -> {
                this.player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                if (!clickTypeWrapper.isLeft) {
                    return;
                }
                if (this.itemStack.isEmpty()) {
                    return;
                }
                ServerLevel serverLevel = this.player.serverLevel();
                EntityType<NPCRoleFastEntity> entityType = next.get();
                entityType.spawn(serverLevel, this.clickedPos.relative(Direction.UP), EntitySpawnReason.SPAWN_ITEM_USE);
                if (!this.isCreativeMode()) {
                    this.itemStack.shrink(1);
                }
                this.close();
            });
            this.setSlot(i, element);
        }
    }

    public boolean isCreativeMode() {
        return this.player.isCreative();
    }

}
