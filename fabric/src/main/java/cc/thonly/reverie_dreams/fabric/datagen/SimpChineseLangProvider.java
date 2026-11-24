package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.fabric.datagen.lang.*;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public final class SimpChineseLangProvider extends FabricLanguageProvider {
    public SimpChineseLangProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, "zh_cn", lookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder builder) {
        CustomerEvaluationLang.add(builder);
        CustomerOrderLang.add(builder);
        RoleLang.add(builder);
        ConfigLang.add(builder);
        AdvancementLang.add(builder);
        CommandLang.add(builder);
        EntityLang.add(registries, builder);
        ItemLang.add(builder);
        EffectLang.add(builder);
        DialogLang.add(builder);
        ItemTooltipLang.add(builder);
        SoundLang.add(builder);
        DanmakuLang.add(builder);
        WorldLang.add(builder);
        LandmarkLang.add(builder);
        NPCLang.add(builder);
        BlockLang.add(builder);
        CuisineLang.add(builder);
        FoodDescriptionLang.add(builder);
        BeverageDescriptionLang.add(builder);
        GuidebookLang.add(builder);
    }
}
