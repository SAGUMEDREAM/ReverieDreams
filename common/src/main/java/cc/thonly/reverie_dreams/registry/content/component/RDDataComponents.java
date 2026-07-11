package cc.thonly.reverie_dreams.registry.content.component;

import cc.thonly.reverie_dreams.component.BattleStickRecorder;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.component.GapRecorder;
import cc.thonly.reverie_dreams.component.RoleFollowerArchive;
import cc.thonly.reverie_dreams.data.DrinkProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.data.danmaku.SpellcardRenderer;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.SpellCardFrameConfig;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.prop.MusicalInstrumentItem;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.ReverieDreamsRegistries;
import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.RegistrySupplier;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
@Slf4j
@EqualsAndHashCode
public class RDDataComponents {
    @SuppressWarnings("rawtypes")
    public static final List<Holder<DataComponentType>> COMPONENTS = new ArrayList<>();
    public static final RegistrySupplier<DataComponentType<DanmakuProperties>> DANMAKU_PROPERTIES =
            registerComponent("danmaku_properties", DanmakuProperties.CODEC);

    public static final RegistrySupplier<DataComponentType<SpellCardFrameConfig>> SPELLCARD_FRAME_CONFIG =
            registerComponent("spellcard_frame", SpellCardFrameConfig.COMPONENT_CODEC);

    public static final RegistrySupplier<DataComponentType<SpellcardRenderer>> SPELL_CARD_COMPONENT =
            registerComponent("spell_card_component", SpellcardRenderer.CODEC);

    public static final RegistrySupplier<DataComponentType<IngredientStack>> DANMAKU_SHAPE =
            registerComponent("shape", IngredientStack.CODEC);

    public static final RegistrySupplier<DataComponentType<Integer>> FOV =
            registerComponent("fov", Codec.INT);

    public static final RegistrySupplier<DataComponentType<Unit>> SILVER_ITEM =
            registerComponent("silver_item", Unit.CODEC);

    public static final RegistrySupplier<DataComponentType<Identifier>> ROLE_CARD_ID =
            registerComponent("role_card_id", Identifier.CODEC);

    public static final RegistrySupplier<DataComponentType<Integer>> MAX_DISTANCE =
            registerComponent("max_distance", Codec.INT);

    public static final RegistrySupplier<DataComponentType<List<GapRecorder>>> GAP_RECORDER =
            registerComponent("gap_recorder", GapRecorder.LIST_CODEC);

    public static final RegistrySupplier<DataComponentType<BattleStickRecorder>> BATTLE_STICK_RECORDER =
            registerComponent("battle_stick_recorder", BattleStickRecorder.CODEC);

    public static final RegistrySupplier<DataComponentType<String>> PLAYING_MUSIC =
            registerComponent("playing_music", Codec.STRING);

    public static final RegistrySupplier<DataComponentType<NoteBlockInstrument>> NOTE_TYPE =
            registerComponent("note_type", MusicalInstrumentItem.NOTE_BLOCK_INSTRUMENT_CODEC);

    public static final RegistrySupplier<DataComponentType<RoleFollowerArchive>> ROLE_FOLLOWER_ARCHIVE =
            registerComponent("role_follower_archive", RoleFollowerArchive.CODEC);

    public static final RegistrySupplier<DataComponentType<Boolean>> ROLE_CAN_RESPAWN =
            registerComponent("role_can_respawn", Codec.BOOL);

    public static final RegistrySupplier<DataComponentType<Unit>> FOOD_ITEM_TYPE =
            registerComponent("food_item_type", Unit.CODEC);

    public static final RegistrySupplier<DataComponentType<Unit>> INGREDIENT_ITEM_TYPE =
            registerComponent("ingredient_item_type", Unit.CODEC);

    public static final RegistrySupplier<DataComponentType<Unit>> DRINK_ITEM_TYPE =
            registerComponent("drink_item_type", Unit.CODEC);

    public static final RegistrySupplier<DataComponentType<List<FoodProperty>>> FOOD_PROPERTIES =
            registerComponent("food_properties", FoodProperty.LIST_COMPONENT_CODEC);

    public static final RegistrySupplier<DataComponentType<List<DrinkProperty>>> DRINK_PROPERTIES =
            registerComponent("drink_properties", DrinkProperty.LIST_COMPONENT_CODEC);

    public static final RegistrySupplier<DataComponentType<Integer>> FOOD_BONUS =
            registerComponent("food_bonus", Codec.INT);

    public static final RegistrySupplier<DataComponentType<KitchenRecipe.IdEntry>> RECIPE_MEMORY =
            registerComponent("recipe_memory", KitchenRecipe.IdEntry.CODEC);

    public static final RegistrySupplier<DataComponentType<String>> GUIDE_BOOK_NAMESPACE =
            registerComponent("guidebook_namespace", Codec.STRING);

    public static final RegistrySupplier<DataComponentType<Identifier>> GUIDE_BOOK_PAGE_ID =
            registerComponent("guidebook_page_id", Identifier.CODEC);

    public static final RegistrySupplier<DataComponentType<Unit>> SHOW_ONLY =
            registerComponent("show_only", Unit.CODEC);

    public static void initialize() {

    }

    @SuppressWarnings({"rawtypes", "UnnecessaryLocalVariable"})
    public static <T> RegistrySupplier<DataComponentType<T>> registerComponent(
            String path,
            Codec<T> codec
    ) {
        Codec castCodec = codec;
        StreamCodec streamCodec = ByteBufCodecs.fromCodec(castCodec);
        RegistrySupplier<DataComponentType> componentType = ReverieDreamsRegistries.DATA_COMPONENT_TYPE.register(
                path,
                () -> DataComponentType.builder()
                                       .persistent(castCodec)
                                       .networkSynchronized(streamCodec)
                                       .cacheEncoding()
                                       .build()
        );
        COMPONENTS.add(componentType);
        return (RegistrySupplier) componentType;
    }

}
