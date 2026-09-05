package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractRecipeTypeProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class CustomRecipeTypeProvider extends AbstractRecipeTypeProvider {

    public CustomRecipeTypeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    public void configured(HolderLookup.Provider provider) {

    }

    @Override
    public @NonNull String getName() {
        return "Custom Recipe Provider";
    }
}
