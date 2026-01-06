package cc.thonly.reverie_dreams.item.template;

import cc.thonly.reverie_dreams.data.npc.NPCRole;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleFastEntity;
import cc.thonly.reverie_dreams.item.builder.RoleCard;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
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
        ResourceLocation identifier = itemStack.get(RDDataComponents.ROLE_CARD_ID);
        if (identifier == null) {
            return Optional.empty();
        }
        RoleCard roleCard = RegistryHandlers.ROLE_CARD.getValue(identifier);
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
                Optional<NPCRole> random = roleCard.random();
                if (random.isPresent()) {
                    NPCRole npcRole = random.get();
                    EntityType<NPCRoleFastEntity> entityType = npcRole.get();
                    BlockPos offset = context.getClickedPos().offset(0, 1, 0);
                    entityType.spawn(serverWorld, offset, EntitySpawnReason.SPAWN_ITEM_USE);
                }
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

    @Getter
    public static class UsingData {
        private final ServerPlayer player;
        private final ServerLevel world;
        private final ItemStack itemStack;
        private final BlockPos blockPos;
        private final RoleCard roleCard;
        private final Map<ResourceLocation, NPCRole> id2Role = new Object2ObjectOpenHashMap<>();
        private final List<NPCRole> roleList = new ArrayList<>();

        public UsingData(ServerPlayer player, ServerLevel world, BlockPos blockPos, ItemStack itemStack, RoleCard roleCard) {
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
