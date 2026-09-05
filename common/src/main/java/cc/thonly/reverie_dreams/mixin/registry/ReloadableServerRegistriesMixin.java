package cc.thonly.reverie_dreams.mixin.registry;

import cc.thonly.reverie_dreams.api.loot.LootTableIdSetter;
import cc.thonly.reverie_dreams.mixin.accessor.HolderReferenceAccessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadableServerRegistries.class)
public class ReloadableServerRegistriesMixin {
    @SuppressWarnings("unchecked")
    @Inject(method = "validateRegistry", at = @At("TAIL"))
    private static <T> void makeIdSet(ValidationContext context, LootDataType<T> type, HolderLookup.Provider registries, CallbackInfo ci) {
        if (type != LootDataType.TABLE) {
            return;
        }
        HolderLookup.RegistryLookup<LootTable> registryLookup = registries.lookupOrThrow(LootDataType.TABLE.registryKey());
        registryLookup.listElements().forEach(reference -> {
            HolderReferenceAccessor<LootTable> accessor = (HolderReferenceAccessor<LootTable>) reference;
            ResourceKey<LootTable> lootTableResourceKey = accessor.reverie_dreams$getKeyOrEmpty();
            LootTable lootTable = accessor.reverie_dreams$getValueOrEmpty();
            if (lootTableResourceKey == null || lootTable == null) {
                return;
            }
            LootTableIdSetter lootTableIdSetter = (LootTableIdSetter) lootTable;
            lootTableIdSetter.reverie_dreams$setLootTableId(lootTableResourceKey);
        });
    }
}
