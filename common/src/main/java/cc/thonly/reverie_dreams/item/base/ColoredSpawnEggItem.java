package cc.thonly.reverie_dreams.item.base;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.util.item.DataComponentInitializersAccess;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class ColoredSpawnEggItem extends SpawnEggItem {
    public static final List<Item> SPAWN_EGGS = new ArrayList<>(128);
    public static final DyedItemColor DEFAULT_COLOR = new DyedItemColor(16777215);
    private final Identifier itemId;
    private final ResourceKey<Item> itemKey;
    private long color = -1;

    public ColoredSpawnEggItem(String name, EntityType<? extends Mob> type, Properties settings) {
        super(settings.component(DataComponents.ENTITY_DATA, TypedEntityData.of(type, new CompoundTag()))
                .setId(ResourceKey.create(Registries.ITEM, ReverieDreams.id(name)))
                .component(DataComponents.DYED_COLOR, DEFAULT_COLOR)
        );
        this.itemId = ReverieDreams.id(name);
        this.itemKey = ResourceKey.create(Registries.ITEM, this.itemId);
        SPAWN_EGGS.add(this);
    }

    public ColoredSpawnEggItem(Identifier identifier, EntityType<? extends Mob> type, Properties settings) {
        super(settings.component(DataComponents.ENTITY_DATA, TypedEntityData.of(type, new CompoundTag()))
                .setId(ResourceKey.create(Registries.ITEM, identifier))
                .component(DataComponents.DYED_COLOR, DEFAULT_COLOR)
        );
        this.itemId = identifier;
        this.itemKey = ResourceKey.create(Registries.ITEM, this.itemId);
        SPAWN_EGGS.add(this);
    }

    public void setColor(long color) {
        this.color = color;
        DataComponentInitializersAccess.modifyEntry(this.itemKey, modifier -> {
            modifier.set(DataComponents.DYED_COLOR, new DyedItemColor(16777215));
        });
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack itemStack = super.getDefaultInstance();
        if (this.color != -1) {
            itemStack.set(DataComponents.DYED_COLOR, new DyedItemColor((int) this.color));
        } else {
            itemStack.set(DataComponents.DYED_COLOR, new DyedItemColor(16777215));
        }
        return itemStack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        super.useOn(context);
        Player player = context.getPlayer();
        Level world = context.getLevel();
        InteractionHand hand = context.getHand();
        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.swing(hand);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}
