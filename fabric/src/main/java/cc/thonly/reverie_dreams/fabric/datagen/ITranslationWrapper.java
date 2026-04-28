package cc.thonly.reverie_dreams.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

public interface ITranslationWrapper {
    public static TranslationWrapper ofWrapper(HolderLookup.Provider wrapperLookup, FabricLanguageProvider.TranslationBuilder translationBuilder) {
        return new TranslationWrapper(wrapperLookup, translationBuilder);
    }

}
