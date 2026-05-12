package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractRecipeTypeProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class CustomRecipeTypeProvider extends AbstractRecipeTypeProvider {

    public CustomRecipeTypeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    public void configured() {

    }

    @Override
    public @NonNull String getName() {
        return "Custom Recipe Provider";
    }
}
