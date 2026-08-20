package cc.thonly.reverie_dreams.data.craftengine;

import cc.thonly.reverie_dreams.mixin.accessor.BlockAccessor;
import cc.thonly.reverie_dreams.mixin.accessor.FireBlockAccessor;
import cc.thonly.reverie_dreams.mixin.accessor.ItemAccessor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"deprecation", "LombokGetterMayBeUsed"})
@Slf4j
public class BlockDefinitionList {

    @JsonProperty("blocks")
    public final Map<String, Definition> blocks = new Object2ObjectLinkedOpenHashMap<>();

    public BlockDefinitionList(List<Block> blockList) {
        for (Block block : blockList) {
            ResourceKey<Block> resourceKey = ((BlockAccessor) block).reverie_dreams$builtInRegistryHolder()
                                                                    .key();
            Identifier id = resourceKey.identifier();
            String key = id.toString();
            Definition definition = new Definition(block);
            this.blocks.put(key, definition);
        }
    }

    public Map<String, Definition> getBlocks() {
        return this.blocks;
    }

    @SuppressWarnings("ALL")
    public static class Definition {
        transient final Block block;
        @JsonProperty("settings")
        public final Settings settings;

        public Definition(Block block) {
            this.block = block;
            this.settings = new Settings(block);
        }
    }

    public static class State {
        transient final Block block;

        public State(Block block) {
            this.block = block;
            this.readState();
        }

        void readState() {

        }
    }

    @SuppressWarnings({"DataFlowIssue", "ConstantValue"})
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class Settings {
        transient final Block block;
        @JsonProperty("hardness")
        Float hardness;

        @JsonProperty("resistance")
        Float resistance;

        @JsonProperty("push_reaction")
        String pushReaction;

        @JsonProperty("map_color")
        Integer mapColor;

        @JsonProperty("replaceable")
        Boolean replaceable;

        @JsonProperty("sounds")
        SoundSettings sounds;

        @JsonProperty("require_correct_tools")
        Boolean requireCorrectTools;

        @JsonProperty("tags")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<String> tags = new ArrayList<>();

        @JsonProperty("luminance")
        Integer luminance;

        @JsonProperty("can_occlude")
        Boolean canOcclude;

        @JsonProperty("jump_factor")
        Float jumpFactor;

        @JsonProperty("speed_factor")
        Float speedFactor;

        @JsonProperty("friction")
        Float friction;
        //
        @JsonProperty("burnable")
        Boolean burnable;

        @JsonProperty("fire_spread_chance")
        Integer fireSpreadChance;

        @JsonProperty("burn_chance")
        Integer burnChance;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @JsonProperty("item")
        String item;

        @JsonProperty("is_redstone_conductor")
        Boolean redstoneConductor;

        @JsonProperty("is_suffocating")
        Boolean suffocating;

        @JsonProperty("is_view_blocking")
        Boolean viewBlocking;

        @JsonProperty("respect_tool_component")
        Boolean respectToolComponent;

        @JsonProperty("correct_tools")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<String> correctTools = new ArrayList<>();

        @JsonProperty("incorrect_tool_dig_speed")
        Float incorrectToolDigSpeed;

        @JsonProperty("instrument")
        String instrument;

        @JsonProperty("fluid_state")
        String fluidState;

        @JsonProperty("support_shape")
        String supportShape;

        @JsonProperty("block_light")
        Integer blockLight;

        @JsonProperty("propagate_skylight")
        Boolean propagateSkylight;

        @JsonProperty("bounce_restitution")
        Float bounceRestitution;

        public Settings(Block block) {
            this.block = block;
            this.readSettings();
        }

        void readSettings() {
            BlockState state = this.block.defaultBlockState();
            BlockBehaviour.Properties properties = this.block.properties();
            this.hardness = this.block.defaultDestroyTime();
            this.resistance = this.block.getExplosionResistance();
            this.pushReaction = state.getPistonPushReaction().name();
            this.sounds = new SoundSettings(state.getSoundType());
            this.luminance = state.getLightEmission();
            this.canOcclude = state.canOcclude();
            this.jumpFactor = this.block.getJumpFactor();
            this.speedFactor = this.block.getSpeedFactor();
            this.friction = this.block.getFriction();
            this.requireCorrectTools = state.requiresCorrectToolForDrops();
            this.replaceable = state.canBeReplaced();
            this.mapColor = state.getMapColor(null, null).col;
            FireBlockAccessor fire = (FireBlockAccessor) (FireBlock) Blocks.FIRE;
            Object2IntMap<Block> igniteOdds = fire.reverie_dreams$getIgniteOdds();
            Object2IntMap<Block> burnOdds = fire.reverie_dreams$getBurnOdds();
            int ignite = igniteOdds.getInt(this.block);
            int burn = burnOdds.getInt(this.block);
            this.burnable = ignite > 0;
            this.fireSpreadChance = ignite;
            this.burnChance = burn;
            this.redstoneConductor = state.isSolid();
            this.suffocating = state.isSolid();
            this.viewBlocking = state.isSolid();
            this.requireCorrectTools = state.requiresCorrectToolForDrops();
            this.instrument = state.instrument().name();

            Holder.Reference<Block> reference = ((BlockAccessor) this.block).reverie_dreams$builtInRegistryHolder();
            if (reference.tags != null) {
                for (TagKey<Block> tag : reference.tags().toList()) {
                    this.tags.add(tag.location().toString());
                }
            }

            Item blockItem = this.block.asItem();
            if (blockItem != null) {
                this.item = ((ItemAccessor) blockItem).reverie_dreams$builtInRegistryHolder().key().identifier().toString();
            }
        }
    }

    public static class SoundSettings {
        @JsonProperty("break")
        String breakSound;
        @JsonProperty("step")
        String stepSound;
        @JsonProperty("place")
        String placeSound;
        @JsonProperty("hit")
        String hitSound;
        @JsonProperty("fall")
        String fallSound;

        public SoundSettings(SoundType sound) {
            this.breakSound = id(sound.getBreakSound());
            this.stepSound = id(sound.getStepSound());
            this.placeSound = id(sound.getPlaceSound());
            this.hitSound = id(sound.getHitSound());
            this.fallSound = id(sound.getFallSound());
        }

        private String id(SoundEvent event) {
            Identifier id = BuiltInRegistries.SOUND_EVENT.getKey(event);
            return id == null ? null : id.toString();
        }

    }
}
