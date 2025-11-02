package cc.thonly.reverie_dreams.item.base;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import eu.pb4.polymer.core.api.item.PolymerSpawnEggItem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

@Setter
@Getter
@ToString
public class SpawnEggItem extends PolymerSpawnEggItem implements IdentifierGetter {
    public static final DyedItemColor DEFAULT_COLOR = new DyedItemColor(16777215);
    private final ResourceLocation identifier;
    private long color = -1;

    public SpawnEggItem(String identifier, EntityType<? extends Mob> type, Properties settings) {
        super(type, settings.setId(ResourceKey.create(Registries.ITEM, ReverieDreams.id(identifier))).component(DataComponents.DYED_COLOR, DEFAULT_COLOR));
        this.identifier = ReverieDreams.id(identifier);
    }

    public SpawnEggItem(ResourceLocation identifier, EntityType<? extends Mob> type, Properties settings) {
        super(type, settings.setId(ResourceKey.create(Registries.ITEM, identifier)).component(DataComponents.DYED_COLOR, DEFAULT_COLOR));
        this.identifier = identifier;

    }

    public void setColor(long color) {
        this.color = color;
        DyedItemColor dyedColorComponent = this.components.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(16777215));
        dyedColorComponent.rgb = (int) this.color;
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
