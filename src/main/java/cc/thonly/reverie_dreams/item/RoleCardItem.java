package cc.thonly.reverie_dreams.item;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.entity.base.NPCEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCRole;
import cc.thonly.reverie_dreams.item.base.BasicPolymerItem;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.dialog.AfterAction;
import net.minecraft.dialog.DialogActionButtonData;
import net.minecraft.dialog.DialogButtonData;
import net.minecraft.dialog.DialogCommonData;
import net.minecraft.dialog.action.SimpleDialogAction;
import net.minecraft.dialog.body.PlainMessageDialogBody;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.dialog.type.MultiActionDialog;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Consumer;

@Getter
public class RoleCardItem extends BasicPolymerItem {
    public static final BiMap<String, UsingData> USING_DATA_MAP = HashBiMap.create();
    ;
    public static final SoundEvent SOUND = SoundEvents.ITEM_BUCKET_FILL;

    public RoleCardItem(Identifier id, Settings settings) {
        super(id, settings, Items.TRIAL_KEY);
    }

    public RoleCardItem(String path, Settings settings) {
        super(path, settings, Items.TRIAL_KEY);
    }

    public Optional<RoleCard> getRoleCardComponent(ItemStack itemStack) {
        Identifier identifier = itemStack.get(ModDataComponentTypes.ROLE_CARD_ID);
        if (identifier == null) {
            return Optional.empty();
        }
        RoleCard roleCard = RegistryManager.ROLE_CARD.get(identifier);
        if (roleCard == null) {
            return Optional.empty();
        }
        return Optional.of(roleCard);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!world.isClient() && context.getWorld() instanceof ServerWorld serverWorld && context.getPlayer() instanceof ServerPlayerEntity player) {
            Hand hand = context.getHand();
            ItemStack itemStack = context.getStack();
            Optional<RoleCard> roleCardWrapper = this.getRoleCardComponent(itemStack);
            if (roleCardWrapper.isPresent()) {
                RoleCard roleCard = roleCardWrapper.get();
                UUID uuid = UUID.randomUUID();
                UsingData data = new UsingData(player, serverWorld, context.getBlockPos().up(), itemStack, roleCard);

                USING_DATA_MAP.put(uuid.toString(), data);
                Dialog selectMenu = getSelectMenu(uuid.toString(), data);
                player.openDialog(RegistryEntry.of(selectMenu));
                player.swingHand(hand);
//
//                Optional<NPCRole> roleWrapper = roleCard.random();
//                if (roleWrapper.isPresent()) {
//                    NPCRole role = roleWrapper.get();
//                    itemStack.decrementUnlessCreative(1, entity);
//                    EntityType<NPCEntity> entityType = role.get();
//                    entityType.spawn(serverWorld, context.getBlockPos().up(), SpawnReason.SPAWN_ITEM_USE);
//
//                    world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SOUND, entity.getSoundCategory(), 2.0f, 1.0f);
//                    entity.swingHand(hand);
//                    return ActionResult.SUCCESS_SERVER;
//                }
            }
        }
        return super.useOnBlock(context);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        Optional<RoleCard> roleCardComponent = this.getRoleCardComponent(stack);
        if (roleCardComponent.isEmpty()) {
            textConsumer.accept(Text.translatable("item.disabled"));
            return;
        }
        if (roleCardComponent.get().isEmpty()) {
            textConsumer.accept(Text.translatable("item.disabled"));
            return;
        }
        textConsumer.accept(Text.translatable("item.tooltip.use"));
    }

    public static Dialog getSelectMenu(String uuid, UsingData data) {
        MultiActionDialog dialog = new MultiActionDialog(
                new DialogCommonData(
                        data.getItemStack().getName(),
                        Optional.empty(),
                        true, false,
                        AfterAction.CLOSE,
                        new ArrayList<>(
                                List.of(
                                        new PlainMessageDialogBody(Text.translatable("dialog.message.select"), 200)
                                )
                        ),
                        new ArrayList<>()
                ),
                new ArrayList<>(List.of()),
                Optional.empty(),
                1
        );
        for (Map.Entry<Identifier, NPCRole> entry : data.id2Role.entrySet()) {
            Identifier identifier = entry.getKey();
            NPCRole role = entry.getValue();
            EntityType<NPCEntity> entityType = role.get();
            NbtCompound element = new NbtCompound();
            element.putString("session_id", uuid);
            element.putString("entity_id", identifier.toString());
            dialog.actions().add(new DialogActionButtonData(
                    new DialogButtonData(Text.empty().append(Text.translatable(entityType.getTranslationKey())), 180),
                    Optional.of(new SimpleDialogAction(new ClickEvent.Custom(Touhou.id("role/summon"), Optional.of(element))))
            ));
        }
        NbtCompound element = new NbtCompound();
        element.putString("session_id", uuid);
        element.putString("entity_id", "random");
        dialog.actions().add(new DialogActionButtonData(
                new DialogButtonData(Text.empty().append(Text.translatable("dialog.text.random")), 180),
                Optional.of(new SimpleDialogAction(new ClickEvent.Custom(Touhou.id("role/summon"), Optional.of(element))))
        ));
        dialog.actions().add(new DialogActionButtonData(
                new DialogButtonData(Text.empty().append(Text.translatable("dialog.text.exit")), 200),
                Optional.empty()
        ));
        return dialog;
    }

    @Getter
    public static class UsingData {
        private final ServerPlayerEntity player;
        private final ServerWorld world;
        private final ItemStack itemStack;
        private final BlockPos blockPos;
        private final RoleCard roleCard;
        private final Map<Identifier, NPCRole> id2Role = new Object2ObjectOpenHashMap<>();
        private final List<NPCRole> roleList = new ArrayList<>();

        public UsingData(ServerPlayerEntity player, ServerWorld world, BlockPos blockPos, ItemStack itemStack, RoleCard roleCard) {
            this.player = player;
            this.world = world;
            this.itemStack = itemStack;
            this.blockPos = blockPos;
            this.roleCard = roleCard;
            this.roleList.addAll(this.roleCard.stream().toList());
            for (NPCRole npcRole : this.roleList) {
                this.id2Role.put(npcRole.getId(), npcRole);
            }
        }
    }
}
