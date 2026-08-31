package cc.thonly.reverie_dreams.registry.content.component;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.AliasManager;
import cc.thonly.reverie_dreams.component.BattleStickRecorder;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.component.GapRecorder;
import cc.thonly.reverie_dreams.component.RoleFollowerArchive;
import cc.thonly.reverie_dreams.data.BeverageProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.data.danmaku.SpellcardRenderer;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.SpellCardFrameConfig;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.prop.MusicalInstrumentItem;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.RegistrySupplier;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
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
public class RDDataComponentTypes {
    @SuppressWarnings("rawtypes")
    public static final List<Holder<DataComponentType>> COMPONENTS = new ArrayList<>();
    public static final RegistrySupplier<DataComponentType<DanmakuProperties>> DANMAKU_PROPERTIES =
            registerDataComponentType("danmaku_properties", DanmakuProperties.CODEC);

    public static final RegistrySupplier<DataComponentType<SpellCardFrameConfig>> SPELLCARD_FRAME_CONFIG =
            registerDataComponentType("spellcard_frame", SpellCardFrameConfig.COMPONENT_CODEC);

    public static final RegistrySupplier<DataComponentType<SpellcardRenderer>> SPELL_CARD_COMPONENT =
            registerDataComponentType("spell_card_component", SpellcardRenderer.CODEC);

    public static final RegistrySupplier<DataComponentType<IngredientStack>> DANMAKU_SHAPE =
            registerDataComponentType("shape", IngredientStack.CODEC);

    public static final RegistrySupplier<DataComponentType<Integer>> FOV =
            registerDataComponentType("fov", Codec.INT);

    public static final RegistrySupplier<DataComponentType<Unit>> SILVER_ITEM =
            registerDataComponentType("silver_item", Unit.CODEC);

    public static final RegistrySupplier<DataComponentType<Identifier>> ROLE_CARD_ID =
            registerDataComponentType("role_card_id", Identifier.CODEC);

    public static final RegistrySupplier<DataComponentType<Integer>> MAX_DISTANCE =
            registerDataComponentType("max_distance", Codec.INT);

    public static final RegistrySupplier<DataComponentType<List<GapRecorder>>> GAP_RECORDER =
            registerDataComponentType("gap_recorder", GapRecorder.LIST_CODEC);

    public static final RegistrySupplier<DataComponentType<BattleStickRecorder>> BATTLE_STICK_RECORDER =
            registerDataComponentType("battle_stick_recorder", BattleStickRecorder.CODEC);

    public static final RegistrySupplier<DataComponentType<String>> PLAYING_MUSIC =
            registerDataComponentType("playing_music", Codec.STRING);

    public static final RegistrySupplier<DataComponentType<NoteBlockInstrument>> NOTE_TYPE =
            registerDataComponentType("note_type", MusicalInstrumentItem.NOTE_BLOCK_INSTRUMENT_CODEC);

    public static final RegistrySupplier<DataComponentType<RoleFollowerArchive>> ROLE_FOLLOWER_ARCHIVE =
            registerDataComponentType("role_follower_archive", RoleFollowerArchive.CODEC);

    public static final RegistrySupplier<DataComponentType<Boolean>> ROLE_CAN_RESPAWN =
            registerDataComponentType("role_can_respawn", Codec.BOOL);

    public static final RegistrySupplier<DataComponentType<Unit>> FOOD_ITEM_TYPE =
            registerDataComponentType("cuisine_item_type", Unit.CODEC);

    public static final RegistrySupplier<DataComponentType<Unit>> INGREDIENT_ITEM_TYPE =
            registerDataComponentType("ingredient_item_type", Unit.CODEC);

    public static final RegistrySupplier<DataComponentType<Unit>> DRINK_ITEM_TYPE =
            registerDataComponentType("beverage_item_type", Unit.CODEC);

    public static final RegistrySupplier<DataComponentType<List<FoodProperty>>> FOOD_PROPERTIES =
            registerDataComponentType("food_properties", FoodProperty.BY_REGISTRY_LIST_CODEC);

    public static final RegistrySupplier<DataComponentType<List<BeverageProperty>>> BEVERAGE_PROPERTIES =
            registerDataComponentType("beverage_properties", BeverageProperty.BY_REGISTRY_LIST_CODEC);

    public static final RegistrySupplier<DataComponentType<Integer>> FOOD_BONUS =
            registerDataComponentType("food_bonus", Codec.INT);

    public static final RegistrySupplier<DataComponentType<KitchenRecipe.IdEntry>> RECIPE_MEMORY =
            registerDataComponentType("recipe_memory", KitchenRecipe.IdEntry.CODEC);

    public static final RegistrySupplier<DataComponentType<String>> GUIDE_BOOK_NAMESPACE =
            registerDataComponentType("guidebook_namespace", Codec.STRING);

    public static final RegistrySupplier<DataComponentType<Identifier>> GUIDE_BOOK_PAGE_ID =
            registerDataComponentType("guidebook_page_id", Identifier.CODEC);

    public static final RegistrySupplier<DataComponentType<Unit>> SHOW_ONLY =
            registerDataComponentType("show_only", Unit.CODEC);

    public static final RegistrySupplier<DataComponentType<String>> CHEQUE_PLAYER_ID =
            registerDataComponentType("cheque_player_id", Codec.STRING);

    public static final RegistrySupplier<DataComponentType<Component>> CHEQUE_NAME =
            registerDataComponentType("cheque_name", ComponentSerialization.CODEC);

    public static final RegistrySupplier<DataComponentType<Integer>> CHEQUE_AMOUNT =
            registerDataComponentType("cheque_amount", Codec.INT);

    @Deprecated
    public static final RegistrySupplier<DataComponentType<List<BeverageProperty>>> DRINK_PROPERTIES = BEVERAGE_PROPERTIES;

    static {
        AliasManager.Registrar registrar = AliasManager.get(Registries.DATA_COMPONENT_TYPE);
        registrar.addAlias(ReverieDreams.id("drink_properties"), ReverieDreams.id("beverage_properties"));
        registrar.addAlias(ReverieDreams.id("drink_item_type"), ReverieDreams.id("beverage_item_type"));
        registrar.addAlias(ReverieDreams.id("food_item_type"), ReverieDreams.id("cuisine_item_type"));
    }

    public static void initialize() {

    }

    @SuppressWarnings({"rawtypes", "UnnecessaryLocalVariable"})
    public static <T> RegistrySupplier<DataComponentType<T>> registerDataComponentType(
            String path,
            Codec<T> codec
    ) {
        Codec castCodec = codec;
        StreamCodec streamCodec = ByteBufCodecs.fromCodec(castCodec);
        RegistrySupplier<DataComponentType> componentType = MCBuiltInRegistries.DATA_COMPONENT_TYPE.register(
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
