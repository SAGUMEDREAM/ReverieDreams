package cc.thonly.reverie_dreams.mixin.loot;

import cc.thonly.reverie_dreams.api.loot.ILootTableModificationContextExtension;
import dev.architectury.event.events.common.LootEvent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LootEvent.LootTableModificationContext.class)
public interface LootTableModificationContextMixin {

}
