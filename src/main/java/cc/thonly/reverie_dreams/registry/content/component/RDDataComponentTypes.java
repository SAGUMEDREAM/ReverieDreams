package cc.thonly.reverie_dreams.registry.content.component;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.component.BattleStickRecorder;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.component.GapRecorder;
import cc.thonly.reverie_dreams.component.RoleFollowerArchive;
import cc.thonly.reverie_dreams.component.tooltip.TooltipManager;
import cc.thonly.reverie_dreams.data.danmaku.SpellcardRenderer;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.SpellCardFrameConfig;
import cc.thonly.reverie_dreams.item.prop.MusicalInstrumentItem;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import java.util.List;

@Slf4j
public class RDDataComponentTypes {
    public static final DataComponentType<DanmakuProperties> DANMAKU_PROPERTIES = registerComponent("danmaku_properties",
            DataComponentType.<DanmakuProperties>builder()
                    .persistent(DanmakuProperties.CODEC)
                    .build()
    );
    public static final DataComponentType<SpellCardFrameConfig> SPELLCARD_FRAME_CONFIG = registerComponent("spellcard_frame",
            DataComponentType.<SpellCardFrameConfig>builder()
                    .persistent(SpellCardFrameConfig.CODEC)
                    .build());
    public static final DataComponentType<SpellcardRenderer> SPELL_CARD_COMPONENT = registerComponent("spell_card_component",
            DataComponentType.<SpellcardRenderer>builder()
                    .persistent(SpellcardRenderer.CODEC)
                    .build());
    public static final DataComponentType<ItemStackWrapper> DANMAKU_SHAPE = registerComponent("shape",
            DataComponentType.<ItemStackWrapper>builder()
                    .persistent(ItemStackWrapper.CODEC)
                    .build());

    public static final DataComponentType<Unit> SILVER_ITEM = registerComponent("silver_item",
            DataComponentType.<Unit>builder()
                    .persistent(Unit.CODEC)
                    .build()
    );
    public static final DataComponentType<ResourceLocation> ROLE_CARD_ID = registerComponent("role_card_id",
            DataComponentType.<ResourceLocation>builder()
                    .persistent(ResourceLocation.CODEC)
                    .build());
    public static final DataComponentType<Integer> MAX_DISTANCE = registerComponent("max_distance",
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build());
    public static final DataComponentType<List<GapRecorder>> GAP_RECORDER = registerComponent("gap_recorder",
            DataComponentType.<List<GapRecorder>>builder()
                    .persistent(GapRecorder.LIST_CODEC)
                    .build());
    public static final DataComponentType<BattleStickRecorder> BATTLE_STICK_RECORDER = registerComponent("battle_stick_recorder",
            DataComponentType.<BattleStickRecorder>builder()
                    .persistent(BattleStickRecorder.CODEC)
                    .build());
    public static final DataComponentType<String> PLAYING_MUSIC = registerComponent("playing_music",
            DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .build());
    public static final DataComponentType<NoteBlockInstrument> NOTE_TYPE = registerComponent("note_type",
            DataComponentType.<NoteBlockInstrument>builder()
                    .persistent(MusicalInstrumentItem.NOTE_BLOCK_INSTRUMENT_CODEC)
                    .build());
    public static final DataComponentType<RoleFollowerArchive> ROLE_FOLLOWER_ARCHIVE = registerComponent("role_follower_archive",
            DataComponentType.<RoleFollowerArchive>builder()
                    .persistent(RoleFollowerArchive.CODEC)
                    .build()
    );
    public static final DataComponentType<Boolean> ROLE_CAN_RESPAWN = registerComponent("role_can_respawn",
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .build()
    );
    public static final DataComponentType<List<String>> FOOD_PROPERTIES = registerComponent("food_properties",
            DataComponentType.<List<String>>builder()
                    .persistent(Codec.list(Codec.STRING))
                    .build()
    );
    public static final DataComponentType<List<String>> DRINK_PROPERTIES = registerComponent("drink_properties",
            DataComponentType.<List<String>>builder()
                    .persistent(Codec.list(Codec.STRING))
                    .build()
    );
    public static final DataComponentType<Integer> FOOD_BONUS = registerComponent("food_bonus",
            DataComponentType.<Integer>builder().persistent(Codec.INT).build()
    );

    public static void init() {
        TooltipManager.bootstrap();
    }

    public static <T> DataComponentType<T> registerComponent(String path, DataComponentType<T> componentType) {
        DataComponentType<T> value = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ReverieDreams.id(path), componentType);
        PolymerComponent.registerDataComponent(value);
        return value;
    }
}
