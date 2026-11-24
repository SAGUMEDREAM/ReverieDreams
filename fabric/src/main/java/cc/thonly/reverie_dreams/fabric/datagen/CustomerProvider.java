package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractCustomerProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class CustomerProvider extends AbstractCustomerProvider {
    public CustomerProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    public void configured(HolderLookup.Provider provider) {
        RareCustomerDefinitions.register(this);
    }
}
