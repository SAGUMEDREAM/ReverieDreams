package cc.thonly.reverie_dreams.registry.content.component;

import cc.thonly.reverie_dreams.component.BattleStickRecorder;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.component.GapRecorder;
import cc.thonly.reverie_dreams.component.RoleFollowerArchive;
import cc.thonly.reverie_dreams.component.tooltip.InitTooltips;
import cc.thonly.reverie_dreams.data.DrinkProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.data.danmaku.SpellcardRenderer;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.SpellCardFrameConfig;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.prop.MusicalInstrumentItem;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import com.mojang.serialization.Codec;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.core.component.BalmDataComponentTypeRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
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
    public static Holder<DataComponentType<DanmakuProperties>> DANMAKU_PROPERTIES;
    public static Holder<DataComponentType<SpellCardFrameConfig>> SPELLCARD_FRAME_CONFIG;
    public static Holder<DataComponentType<SpellcardRenderer>> SPELL_CARD_COMPONENT;
    public static Holder<DataComponentType<IngredientStack>> DANMAKU_SHAPE;
    public static Holder<DataComponentType<Integer>> FOV;
    public static Holder<DataComponentType<Unit>> SILVER_ITEM;
    public static Holder<DataComponentType<Identifier>> ROLE_CARD_ID;
    public static Holder<DataComponentType<Integer>> MAX_DISTANCE;
    public static Holder<DataComponentType<List<GapRecorder>>> GAP_RECORDER;
    public static Holder<DataComponentType<BattleStickRecorder>> BATTLE_STICK_RECORDER;
    public static Holder<DataComponentType<String>> PLAYING_MUSIC;
    public static Holder<DataComponentType<NoteBlockInstrument>> NOTE_TYPE;
    public static Holder<DataComponentType<RoleFollowerArchive>> ROLE_FOLLOWER_ARCHIVE;
    public static Holder<DataComponentType<Boolean>> ROLE_CAN_RESPAWN;
    public static Holder<DataComponentType<Unit>> FOOD_ITEM_TYPE;
    public static Holder<DataComponentType<Unit>> INGREDIENT_ITEM_TYPE;
    public static Holder<DataComponentType<Unit>> DRINK_ITEM_TYPE;
    public static Holder<DataComponentType<List<FoodProperty>>> FOOD_PROPERTIES;
    public static Holder<DataComponentType<List<DrinkProperty>>> DRINK_PROPERTIES;
    public static Holder<DataComponentType<Integer>> FOOD_BONUS;
    public static Holder<DataComponentType<KitchenRecipe.IdEntry>> RECIPE_MEMORY;

    public static void initialize(BalmDataComponentTypeRegistrar registrar) {
        DANMAKU_PROPERTIES = registerComponent(registrar, "danmaku_properties", DanmakuProperties.CODEC);
        SPELLCARD_FRAME_CONFIG = registerComponent(registrar, "spellcard_frame", SpellCardFrameConfig.COMPONENT_CODEC);
        SPELL_CARD_COMPONENT = registerComponent(registrar, "spell_card_component", SpellcardRenderer.CODEC);
        DANMAKU_SHAPE = registerComponent(registrar, "shape", IngredientStack.CODEC);
        FOV = registerComponent(registrar, "fov", Codec.INT);
        SILVER_ITEM = registerComponent(registrar, "silver_item", Unit.CODEC);
        ROLE_CARD_ID = registerComponent(registrar, "role_card_id", Identifier.CODEC);
        MAX_DISTANCE = registerComponent(registrar, "max_distance", Codec.INT);
        GAP_RECORDER = registerComponent(registrar, "gap_recorder", GapRecorder.LIST_CODEC);
        BATTLE_STICK_RECORDER = registerComponent(registrar, "battle_stick_recorder", BattleStickRecorder.CODEC);
        PLAYING_MUSIC = registerComponent(registrar, "playing_music", Codec.STRING);
        NOTE_TYPE = registerComponent(registrar, "note_type", MusicalInstrumentItem.NOTE_BLOCK_INSTRUMENT_CODEC);
        ROLE_FOLLOWER_ARCHIVE = registerComponent(registrar, "role_follower_archive", RoleFollowerArchive.CODEC);
        ROLE_CAN_RESPAWN = registerComponent(registrar, "role_can_respawn", Codec.BOOL);
        FOOD_ITEM_TYPE = registerComponent(registrar, "food_item_type", Unit.CODEC);
        INGREDIENT_ITEM_TYPE = registerComponent(registrar, "ingredient_item_type", Unit.CODEC);
        DRINK_ITEM_TYPE = registerComponent(registrar, "drink_item_type", Unit.CODEC);
        FOOD_PROPERTIES = registerComponent(registrar, "food_properties", FoodProperty.LIST_COMPONENT_CODEC);
        DRINK_PROPERTIES = registerComponent(registrar, "drink_properties", DrinkProperty.LIST_COMPONENT_CODEC);
        FOOD_BONUS = registerComponent(registrar, "food_bonus", Codec.INT);
        RECIPE_MEMORY = registerComponent(registrar, "recipe_memory", KitchenRecipe.IdEntry.CODEC);

        InitTooltips.bootstrap();
    }

    @SuppressWarnings("rawtypes")
    public static <T> Holder<DataComponentType<T>> registerComponent(
            BalmDataComponentTypeRegistrar registrar,
            String path,
            Codec<T> codec
    ) {
        Holder<DataComponentType<T>> holder = registrar.register(path, codec).asHolder();
        COMPONENTS.add((Holder<DataComponentType>) (Object) holder);
        return holder;
    }

}
