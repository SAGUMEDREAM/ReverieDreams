package cc.thonly.reverie_dreams.item.template;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.item.base.RoleCard;
import cc.thonly.reverie_dreams.server.dialog.role.RoleCardDialog;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.RoleCards;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.world.RDBuiltInGameRules;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.*;

@SuppressWarnings({"DuplicatedCode", "unchecked"})
@Getter
public class RoleCardItem extends Item {
    public static final BiMap<String, UsingData> USING_DATA_MAP = HashBiMap.create();
    public static final SoundEvent SOUND = SoundEvents.BUCKET_FILL;

    public static void clearUsingDataFor(Player player) {
        USING_DATA_MAP.entrySet().removeIf(entry -> entry.getValue().player == player);
    }

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
            GameRules gameRules = serverWorld.getGameRules();

            UUID uuid = UUID.randomUUID();
            boolean freeChoice = gameRules.get(RDBuiltInGameRules.FREE_CHOICE_OF_ROLE.value());
            Optional<RoleCard> roleCardWrapper = this.getRoleCardComponent(itemStack);
            if (freeChoice && roleCardWrapper.isEmpty()) {
                UsingData data = new UsingData(player, serverWorld, context.getClickedPos().above(), itemStack, RoleCards.PROTAGONIST_GROUP, true);

                clearUsingDataFor(player);
                USING_DATA_MAP.put(uuid.toString(), data);
                Dialog selectMenu = getAllSelectMenu(uuid.toString());
                player.openDialog(Holder.direct(selectMenu));

                player.swing(hand);
                return InteractionResult.SUCCESS_SERVER;
            }
            if (roleCardWrapper.isPresent()) {
                RoleCard roleCard = roleCardWrapper.get();
                UsingData data = new UsingData(player, serverWorld, context.getClickedPos().above(), itemStack, roleCard);

                clearUsingDataFor(player);
                USING_DATA_MAP.put(uuid.toString(), data);
                Dialog selectMenu = getSelectMenu(uuid.toString(), data);
                player.openDialog(Holder.direct(selectMenu));
                player.swing(hand);
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        return super.useOn(context);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        Component defaultName = super.getName(itemStack);
        MinecraftServer server = ReverieDreams.getServer();
        if (server != null) {
            GameRules gameRules = server.getGameRules();
            if (gameRules.get(RDBuiltInGameRules.FREE_CHOICE_OF_ROLE.value())) {
                return Component.translatable("item.reverie_dreams.role_card.full");
            }
        }
        return defaultName;
    }

    public static Dialog getSelectMenu(String uuid, UsingData data) {
        return RoleCardDialog.selection(uuid, data);
    }

    public static Dialog getAllSelectMenu(String uuid) {
        return RoleCardDialog.groups(uuid);
    }

    @Getter
    public static class UsingData {
        private final ServerPlayer player;
        private final ServerLevel world;
        private final ItemStack itemStack;
        private final BlockPos blockPos;
        private final RoleCard roleCard;
        private final boolean freeChoice;
        private final Map<Identifier, NPCRoleType> id2Role = new LinkedHashMap<>();
        private final List<NPCRoleType> roleList = new ArrayList<>();

        public UsingData(ServerPlayer player, ServerLevel world, BlockPos blockPos, ItemStack itemStack, RoleCard roleCard) {
            this(player, world, blockPos, itemStack, roleCard, false);
        }

        public UsingData(ServerPlayer player, ServerLevel world, BlockPos blockPos, ItemStack itemStack, RoleCard roleCard, boolean freeChoice) {
            this.player = player;
            this.world = world;
            this.itemStack = itemStack;
            this.blockPos = blockPos;
            this.roleCard = roleCard;
            this.freeChoice = freeChoice;
            List<RoleCard> cards = freeChoice ? new ArrayList<>(BuiltInRegistryProviders.ROLE_CARD.values()) : List.of(roleCard);
            for (RoleCard card : cards) {
                for (NPCRoleType role : card.stream().toList()) {
                    if (role.getId() != null && this.id2Role.putIfAbsent(role.getId(), role) == null) {
                        this.roleList.add(role);
                    }
                }
            }
        }
    }
}
