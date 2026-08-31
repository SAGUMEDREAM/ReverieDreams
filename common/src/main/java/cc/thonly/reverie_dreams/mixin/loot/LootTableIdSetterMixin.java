package cc.thonly.reverie_dreams.mixin.loot;

import cc.thonly.reverie_dreams.api.loot.LootContextModifier;
import cc.thonly.reverie_dreams.api.loot.LootTableIdSetter;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootTable.class)
public class LootTableIdSetterMixin implements LootTableIdSetter {
    @Unique
    @Nullable
    private ResourceKey<LootTable> reverie_dreams$lootTableId;

    @Inject(
            method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At("HEAD")
    )
    public void lootSourceSetter(LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        LootTable lootTable = (LootTable) (Object) this;
        LootTableIdSetter lootTableIdSetter = (LootTableIdSetter) lootTable;
        LootContextModifier iLootContext = (LootContextModifier) context;
        iLootContext.reverie_dreams$setLootTableSource(lootTable);
        iLootContext.reverie_dreams$setLootTableId(lootTableIdSetter.reverie_dreams$getLootTableId());
    }

    @Override
    public ResourceKey<LootTable> reverie_dreams$getLootTableId() {
        return this.reverie_dreams$lootTableId;
    }

    @Override
    public void reverie_dreams$setLootTableId(ResourceKey<LootTable> key) {
        this.reverie_dreams$lootTableId = key;
    }
}
