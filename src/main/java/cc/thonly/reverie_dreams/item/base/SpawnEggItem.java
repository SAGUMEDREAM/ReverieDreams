package cc.thonly.reverie_dreams.item.base;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import eu.pb4.polymer.core.api.item.PolymerSpawnEggItem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

@Setter
@Getter
@ToString
public class SpawnEggItem extends PolymerSpawnEggItem implements IdentifierGetter {
    public static final DyedColorComponent DEFAULT_COLOR = new DyedColorComponent(16777215);
    private final Identifier identifier;
    private long color = -1;

    public SpawnEggItem(String identifier, EntityType<? extends MobEntity> type, Settings settings) {
        super(type, settings.registryKey(RegistryKey.of(RegistryKeys.ITEM, Touhou.id(identifier))).component(DataComponentTypes.DYED_COLOR, DEFAULT_COLOR));
        this.identifier = Touhou.id(identifier);
    }

    public SpawnEggItem(Identifier identifier, EntityType<? extends MobEntity> type, Settings settings) {
        super(type, settings.registryKey(RegistryKey.of(RegistryKeys.ITEM, identifier)).component(DataComponentTypes.DYED_COLOR, DEFAULT_COLOR));
        this.identifier = identifier;

    }

    public void setColor(long color) {
        this.color = color;
        DyedColorComponent dyedColorComponent = this.components.getOrDefault(DataComponentTypes.DYED_COLOR, new DyedColorComponent(16777215));
        dyedColorComponent.rgb = (int) this.color;
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack itemStack = super.getDefaultStack();
        if (this.color != -1) {
            itemStack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent((int) this.color));
        } else {
            itemStack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(16777215));
        }
        return itemStack;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        super.useOnBlock(context);
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        Hand hand = context.getHand();
        if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.swingHand(hand);
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }
}
