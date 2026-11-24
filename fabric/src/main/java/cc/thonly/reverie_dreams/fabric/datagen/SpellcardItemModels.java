package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.item.danmaku.SpellcardArt;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpells;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class SpellcardItemModels {
    private static final Identifier CORNER = ReverieDreams.id("item/spellcard/difficulty_corner");

    private SpellcardItemModels() {
    }

    static void generate(ItemModelGenerators generator, ModContainer mod) {
        var item = RDItems.SPELLCARD.asItem();
        requireTexture(mod, CORNER);
        List<SelectItemModel.SwitchCase<Identifier>> cases = new ArrayList<>();
        for (var spell : TouhouSpells.catalog()) {
            var art = SpellcardArt.itemModel(spell.id()).withPrefix("item/");
            requireTexture(mod, art);
            ModelTemplates.TWO_LAYERED_ITEM.create(art,
                    TextureMapping.layered(new Material(art), new Material(CORNER)), generator.modelOutput);
            cases.add(ItemModelUtils.when(spell.id(), cardModel(art)));
        }
        if (cases.isEmpty()) {
            generator.generateTwoLayerDyedItem(item);
            return;
        }

        var standard = ModelTemplates.FLAT_ITEM.create(item, TextureMapping.layer0(item), generator.modelOutput);
        var dyed = ModelTemplates.TWO_LAYERED_ITEM.create(standard.withSuffix("_dyed"),
                TextureMapping.layered(TextureMapping.getItemTexture(item), TextureMapping.getItemTexture(item, "_overlay")),
                generator.modelOutput);
        var fallback = ItemModelUtils.conditional(ItemModelUtils.hasComponent(DataComponents.DYED_COLOR),
                ItemModelUtils.tintedModel(dyed, new Constant(-1), new Dye(0)),
                ItemModelUtils.plainModel(standard));
        // Select by the existing pattern component so saved stacks and the normal card creation path
        // use the same art. The fallback also covers blank cards and custom datapack patterns.
        generator.itemModelOutput.accept(item, ItemModelUtils.select(
                new ComponentContents<>(RDDataComponentTypes.DANMAKU_PATTERN.value()), fallback, cases));
    }

    static Map<Identifier, ClientItem> standaloneModels() {
        Map<Identifier, ClientItem> models = new LinkedHashMap<>();
        for (var spell : TouhouSpells.catalog()) {
            var modelId = SpellcardArt.itemModel(spell.id());
            models.put(modelId, new ClientItem(cardModel(modelId.withPrefix("item/")), ClientItem.Properties.DEFAULT));
        }
        return models;
    }

    private static ItemModel.Unbaked cardModel(Identifier art) {
        return ItemModelUtils.tintedModel(art, new Constant(-1), new Dye(0xFFFFFF));
    }

    private static void requireTexture(ModContainer mod, Identifier art) {
        String path = "assets/" + art.getNamespace() + "/textures/" + art.getPath() + ".png";
        mod.findPath(path).orElseThrow(() -> new IllegalStateException("Missing spellcard art: " + path));
    }
}
