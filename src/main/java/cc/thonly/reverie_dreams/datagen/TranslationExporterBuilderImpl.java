package cc.thonly.reverie_dreams.datagen;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

public interface TranslationExporterBuilderImpl {
    public static TranslationExporter createBuilder(RegistryWrapper.WrapperLookup wrapperLookup, FabricLanguageProvider.TranslationBuilder translationBuilder) {
        return new TranslationExporter(wrapperLookup, translationBuilder);
    }

}
