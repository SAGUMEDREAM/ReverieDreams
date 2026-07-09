package cc.thonly.reverie_dreams.item.base;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntity;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.skin.SkinTypes;
import cc.thonly.reverie_dreams.server.CustomClickActionRegistry;
import cc.thonly.reverie_dreams.server.dialog.ActionButtonBuilder;
import cc.thonly.reverie_dreams.server.dialog.DialogBuilder;
import cc.thonly.reverie_dreams.server.dialog.DialogEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("resource")
public class CustomSkinSelectorItem extends Item {

    public CustomSkinSelectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand type) {
        Level level = player.level();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(target instanceof NPCRoleEntity npcRoleEntity)) {
            return InteractionResult.FAIL;
        }
        if ((!npcRoleEntity.isOwner(player)) && (!player.isCreative())) {
            return InteractionResult.FAIL;
        }
        DialogEntry dialogEntry = DialogBuilder.builder(builder -> {
            builder.key(ReverieDreams.id("custom_skin_selector"));
            builder.common(commons -> {
                commons.title(Component.translatable("gui.reverie_dreams.custom_skin_selector.title"));
                {
                    Identifier id = SkinType.RECOVERY;
                    Item icon = Items.STICK;
                    MutableComponent component = Component.empty();
                    CompoundTag tag = new CompoundTag();
                    tag.putString("skin_id", id.toString());
                    tag.putString("dim_key", npcRoleEntity.level().dimension().identifier().toString());
                    tag.putInt("target_id", npcRoleEntity.getId());
                    component.append(
                            Component.translatable(npcRoleEntity.getRoleType().translateKey())
                                     .withStyle(
                                             Style.EMPTY.withClickEvent(new ClickEvent.Custom(CustomClickActionRegistry.MODIFY_CUSTOM_SKIN_KEY, Optional.of(tag)))
                                     )
                    );
                    commons.addItemBody(new ItemStackTemplate(icon), Optional.of(
                            new PlainMessage(component, 200))
                    );
                }
                for (var customType : SkinTypes.getCustomTypes()) {
                    Identifier id = customType.getId();
                    Item icon = customType.getIcon();
                    MutableComponent component = Component.empty();
                    CompoundTag tag = new CompoundTag();
                    tag.putString("skin_id", id.toString());
                    tag.putString("dim_key", npcRoleEntity.level().dimension().identifier().toString());
                    tag.putInt("target_id", npcRoleEntity.getId());
                    component.append(
                            Component.translatable(customType.getDescriptionId())
                                     .withStyle(
                                             Style.EMPTY.withClickEvent(new ClickEvent.Custom(CustomClickActionRegistry.MODIFY_CUSTOM_SKIN_KEY, Optional.of(tag)))
                                     )
                    );
                    commons.addItemBody(new ItemStackTemplate(icon), Optional.of(
                            new PlainMessage(component, 200))
                    );
                }
            });
            builder.actions(actions -> {
                actions.addButton(Component.translatable("gui.reverie_dreams.close"), 180, Optional.empty());
            });
        }).get().buildOrThrow();
        dialogEntry.open(player);
        return InteractionResult.SUCCESS_SERVER;
    }
}
