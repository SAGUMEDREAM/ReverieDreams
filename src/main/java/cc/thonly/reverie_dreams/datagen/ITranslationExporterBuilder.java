package cc.thonly.reverie_dreams.datagen;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

public interface ITranslationExporterBuilder {
    public static TranslationExporter createBuilder(HolderLookup.Provider wrapperLookup, FabricLanguageProvider.TranslationBuilder translationBuilder) {
        return new TranslationExporter(wrapperLookup, translationBuilder);
    }

}
