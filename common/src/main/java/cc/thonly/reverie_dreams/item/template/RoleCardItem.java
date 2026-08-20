package cc.thonly.reverie_dreams.item.template;

import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleRedirectEntity;
import cc.thonly.reverie_dreams.item.base.RoleCard;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.server.CustomClickActionRegistry;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.*;

@Getter
public class RoleCardItem extends Item {
    public static final BiMap<String, UsingData> USING_DATA_MAP = HashBiMap.create();
    public static final SoundEvent SOUND = SoundEvents.BUCKET_FILL;

    public RoleCardItem(Properties settings) {
        super(settings);
    }

    public Optional<RoleCard> getRoleCardComponent(ItemStack itemStack) {
        Identifier identifier = itemStack.get(RDDataComponentTypes.ROLE_CARD_ID.value());
        if (identifier == null) {
            return Optional.empty();
        }
        RoleCard roleCard = BuiltInRegistryProviders.ROLE_CARD.getValue(identifier);
        if (roleCard == null) {
            return Optional.empty();
        }
        return Optional.of(roleCard);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (!world.isClientSide() && context.getLevel() instanceof ServerLevel serverWorld && context.getPlayer() instanceof ServerPlayer player) {
            InteractionHand hand = context.getHand();
            ItemStack itemStack = context.getItemInHand();
            Optional<RoleCard> roleCardWrapper = this.getRoleCardComponent(itemStack);
            if (roleCardWrapper.isPresent()) {
                RoleCard roleCard = roleCardWrapper.get();
                UUID uuid = UUID.randomUUID();
                UsingData data = new UsingData(player, serverWorld, context.getClickedPos().above(), itemStack, roleCard);

                USING_DATA_MAP.put(uuid.toString(), data);
                Dialog selectMenu = getSelectMenu(uuid.toString(), data);
                player.openDialog(Holder.direct(selectMenu));
                player.swing(hand);
            }
        }
        return super.useOn(context);
    }

//    @Override
//    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
//        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
//        Optional<RoleCard> roleCardComponent = this.getRoleCardComponent(stack);
//        if (roleCardComponent.isEmpty()) {
//            textConsumer.accept(Text.translatable("item.disabled"));
//            return;
//        }
//        if (roleCardComponent.get().isEmpty()) {
//            textConsumer.accept(Text.translatable("item.disabled"));
//            return;
//        }
//        textConsumer.accept(Text.translatable("item.tooltip.use"));
//    }

    @SuppressWarnings("unchecked")
    public static Dialog getSelectMenu(String uuid, UsingData data) {
        MultiActionDialog dialog = new MultiActionDialog(
                new CommonDialogData(
                        data.getItemStack().getHoverName(),
                        Optional.empty(),
                        true, false,
                        DialogAction.CLOSE,
                        new ArrayList<>(
                                List.of(
                                        new PlainMessage(Component.translatable("dialog.message.select"), 200)
                                )
                        ),
                        new ArrayList<>()
                ),
                new ArrayList<>(List.of()),
                Optional.empty(),
                1
        );
        for (Map.Entry<Identifier, NPCRoleType> entry : data.id2Role.entrySet()) {
            Identifier identifier = entry.getKey();
            NPCRoleType role = entry.getValue();
            EntityType<NPCSimpleRedirectEntity> entityType = (EntityType<NPCSimpleRedirectEntity>) (Object) role.get().value();
            CompoundTag element = new CompoundTag();
            element.putString("session_id", uuid);
            element.putString("entity_id", identifier.toString());
            dialog.actions().add(new ActionButton(
                    new CommonButtonData(Component.empty().append(Component.translatable(entityType.getDescriptionId())), 180),
                    Optional.of(new StaticAction(new ClickEvent.Custom(CustomClickActionRegistry.ROLE_SUMMON_KEY, Optional.of(element))))
            ));
        }
        CompoundTag element = new CompoundTag();
        element.putString("session_id", uuid);
        element.putString("entity_id", "random");
        dialog.actions().add(new ActionButton(
                new CommonButtonData(Component.empty().append(Component.translatable("dialog.text.random")), 180),
                Optional.of(new StaticAction(new ClickEvent.Custom(CustomClickActionRegistry.ROLE_SUMMON_KEY, Optional.of(element))))
        ));
        dialog.actions().add(new ActionButton(
                new CommonButtonData(Component.empty().append(Component.translatable("dialog.text.exit")), 200),
                Optional.empty()
        ));
        return dialog;
    }

    @Getter
    public static class UsingData {
        private final ServerPlayer player;
        private final ServerLevel world;
        private final ItemStack itemStack;
        private final BlockPos blockPos;
        private final RoleCard roleCard;
        private final Map<Identifier, NPCRoleType> id2Role = new Object2ObjectOpenHashMap<>();
        private final List<NPCRoleType> roleList = new ArrayList<>();

        public UsingData(ServerPlayer player, ServerLevel world, BlockPos blockPos, ItemStack itemStack, RoleCard roleCard) {
            this.player = player;
            this.world = world;
            this.itemStack = itemStack;
            this.blockPos = blockPos;
            this.roleCard = roleCard;
            this.roleList.addAll(this.roleCard.stream().toList());
            for (NPCRoleType npcRole : this.roleList) {
                this.id2Role.put(npcRole.getId(), npcRole);
            }
        }
    }
}
