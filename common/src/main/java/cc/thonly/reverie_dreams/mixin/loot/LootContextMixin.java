package cc.thonly.reverie_dreams.mixin.loot;

import cc.thonly.reverie_dreams.inf.ILootContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(LootContext.class)
public abstract class LootContextMixin implements ILootContext {
    @Unique
    private ResourceKey<LootTable> reverie_dreams$lootTableId = null;

    @Unique
    @Nullable
    private LootTable reverie_dreams$lootTableSource = null;

    @Shadow
    @Final
    private HolderGetter.Provider lootDataResolver;

    @Shadow
    public abstract ServerLevel getLevel();

    @Unique
    @Nullable
    public LootTable reverie_dreams$getLootTableSource() {
        return this.reverie_dreams$lootTableSource;
    }

    @Override
    public void reverie_dreams$setLootTableSource(LootTable lootTable) {
        this.reverie_dreams$lootTableSource = lootTable;
    }

    @Override
    public void reverie_dreams$setLootTableId(ResourceKey<LootTable> key) {
        this.reverie_dreams$lootTableId = key;
    }

    @Override
    public @Nullable ResourceKey<LootTable> reverie_dreams$getLootTableKey() {
        if (this.reverie_dreams$lootTableId != null) {
            return this.reverie_dreams$lootTableId;
        }
        if (this.reverie_dreams$lootTableSource == null) {
            return null;
        }
        MinecraftServer server = this.getLevel().getServer();
        ReloadableServerRegistries.Holder holder = server.reloadableRegistries();
        HolderLookup.Provider lookup = holder.lookup();
        Optional<? extends HolderLookup.RegistryLookup<LootTable>> optionalRegistryLookup = lookup.lookup(Registries.LOOT_TABLE);
        if (optionalRegistryLookup.isEmpty()) {
            return null;
        }
        HolderLookup.RegistryLookup<LootTable> registryLookup = optionalRegistryLookup.get();
        Optional<Holder.Reference<LootTable>> any = registryLookup
                .listElements()
                .filter(lootTableReference -> Objects.equals(lootTableReference.value, this.reverie_dreams$lootTableSource))
                .findAny();
        if (any.isEmpty()) {
            return null;
        }
        Holder.Reference<LootTable> reference = any.get();
        Optional<ResourceKey<LootTable>> lootTableResourceKeyOptional = reference.unwrapKey();
        if (lootTableResourceKeyOptional.isEmpty()) {
            return null;
        }
        ResourceKey<LootTable> lootTableResourceKey = lootTableResourceKeyOptional.get();
        this.reverie_dreams$lootTableId = lootTableResourceKey;
        return lootTableResourceKey;
    }
}
