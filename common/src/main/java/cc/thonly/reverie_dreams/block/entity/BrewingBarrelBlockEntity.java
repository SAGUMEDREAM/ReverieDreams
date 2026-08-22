package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.keine.tag.ConventionalItemTags;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.BarrelRecipe;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BrewingBarrelBlockEntity extends BlockEntity {
    public static final int SIZE = 27;
    @Getter
    private SimpleContainer inventory = new SimpleContainer(SIZE);
    @Getter
    private Optional<BarrelRecipe.IdEntry> recipeEntry = Optional.empty();
    @Getter
    private int maxBrewingTick = 1;
    @Getter
    private int brewingTick = 0;
    @Getter
    Optional<IngredientStack> output = Optional.empty();
    @Getter
    int count = 0;
    @Getter
    DataComponentPatch patch = DataComponentPatch.EMPTY;

    public BrewingBarrelBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(RDBlockEntityTypes.BREWING_BARREL.get(), worldPosition, blockState);
    }

    public static void onBlockEntityTick(final Level level,
                                         final BlockPos pos,
                                         final BlockState state,
                                         final BrewingBarrelBlockEntity entity) {
        if (entity.output.isPresent()) {
            return;
        }
        boolean brewing = entity.isBrewing();
        if (brewing) {
            entity.brewingTick++;
        } else {
            entity.finishBrewing();
        }
    }

    private void finishBrewing() {
        IngredientStack stack = IngredientStack.empty();
        if (this.recipeEntry.isPresent()) {
            BarrelRecipe.IdEntry idEntry = this.recipeEntry.get();
            if (idEntry.recipe().isPresent()) {
                BarrelRecipe barrelRecipe = idEntry.recipe().get();
                stack = barrelRecipe.getOutput().copy();
            }
        }
        this.output = Optional.ofNullable(stack.apply(this.patch));
        this.count = 9;
        this.brewingTick = 0;
        this.maxBrewingTick = 1;
        this.recipeEntry = Optional.empty();
        this.setChanged();
    }

    public boolean onUseItem(LivingEntity livingEntity, ItemStack itemStack) {
        if (!itemStack.is(Items.GLASS_BOTTLE)) {
            return false;
        }
        if (!this.hasOutput()) {
            return false;
        }
        SoundEventPlayUtils.playSound(livingEntity, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS);
        if (this.output.isEmpty()) {
            return false;
        }
        boolean reduced = false;
        if (livingEntity instanceof Player player) {
            player.addItem(this.output.get().build());
            itemStack.shrink(1);
            reduced = true;
        } else if (livingEntity instanceof NPCSimpleEntity npc) {
            npc.getInventory().addItem(this.output.get().build());
            itemStack.shrink(1);
            reduced = true;
        }
        if (reduced) {
            this.count--;
            if (this.count <= 0) {
                this.clearOutput();
            }
            this.setChanged();
            return true;
        }
        return false;
    }

    public void clearOutput() {
        this.count = 0;
        this.output = Optional.empty();
        this.patch = DataComponentPatch.EMPTY;
        this.setChanged();
    }

    public boolean isBrewing() {
        return (this.brewingTick < this.maxBrewingTick) && this.recipeEntry.isPresent();
    }

    public boolean hasOutput() {
        return this.count > 0 && this.output.isPresent() && !this.output.get().isEmpty();
    }

    public boolean startMatchesBrewing() {
        if (this.isBrewing()) {
            return false;
        }
        if (this.hasOutput()) {
            return false;
        }
        List<BarrelRecipe> matches = RecipeManager.BARREL_RECIPE.getMatches(this.getInputs().stream().map(IngredientStack::new).toList());
        if (matches.isEmpty()) {
            return false;
        }
        BarrelRecipe first = matches.getFirst();
        if (first == null) {
            return false;
        }
        Identifier recipeKey = RecipeManager.BARREL_RECIPE.getRecipeKey(first);
        this.recipeEntry = Optional.of(new BarrelRecipe.IdEntry(Optional.ofNullable(recipeKey), Optional.of(first)));
        int costTime = first.getCostTime();
        this.brewingTick = 0;
        this.maxBrewingTick = costTime;
        this.patch = this.buildComponentPatch();
        for (ItemStack itemStack : this.inventory) {
            itemStack.shrink(1);
        }
        this.setChanged();
        return true;
    }

    private DataComponentPatch buildComponentPatch() {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();

        List<ItemStack> inputs = this.getInputs();

        Map<MobEffect, MobEffectInstance> effects = new HashMap<>();

        for (ItemStack itemStack : inputs) {
            PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);

            if (potionContents == null) {
                continue;
            }

            for (MobEffectInstance effect : potionContents.getAllEffects()) {
                MobEffect effectType = effect.getEffect().value();

                MobEffectInstance existing = effects.get(effectType);

                if (existing == null) {
                    effects.put(effectType, effect);
                    continue;
                }

                int amplifier = Math.max(
                        existing.getAmplifier(),
                        effect.getAmplifier()
                ) + 1;

                int duration = Math.max(
                        existing.getDuration(),
                        effect.getDuration()
                );

                MobEffectInstance merged = new MobEffectInstance(
                        effect.getEffect(),
                        duration,
                        amplifier,
                        effect.isAmbient(),
                        effect.isVisible(),
                        effect.showIcon()
                );

                effects.put(effectType, merged);
            }
        }

        List<MobEffectInstance> buildEffects = new ArrayList<>(effects.values());

        builder.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), buildEffects, Optional.empty()));
        return builder.build();
    }

    public List<ItemStack> getInputs() {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < 18; i++) {
            list.add(this.inventory.getItem(i));
        }
        return list;
    }

    public List<ItemStack> getOutputs() {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 18; i < SIZE; i++) {
            list.add(this.inventory.getItem(i));
        }
        return list;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        SimpleContainer inventory = new SimpleContainer(SIZE);
        ContainerHelper.loadAllItems(view, inventory.getItems());
        this.inventory = inventory;
        this.recipeEntry = view.read("RecipeTarget", BarrelRecipe.IdEntry.CODEC);
        this.brewingTick = view.getIntOr("BrewingTick", 0);
        this.maxBrewingTick = view.getIntOr("MaxBrewingTick", 1);
        this.output = view.read("Output", IngredientStack.CODEC);
        this.patch = view.read("OutputPatch", DataComponentPatch.CODEC).orElse(DataComponentPatch.EMPTY);
    }

    @Override
    public void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ContainerHelper.saveAllItems(view, this.inventory.getItems());
        this.recipeEntry.ifPresent(idEntry -> view.storeNullable("RecipeTarget", BarrelRecipe.IdEntry.CODEC, idEntry));
        view.putInt("BrewingTick", this.brewingTick);
        view.putInt("MaxBrewingTick", this.maxBrewingTick);
        this.output.ifPresent(stack -> view.storeNullable("Output", IngredientStack.CODEC, stack));
        view.storeNullable("OutputPatch", DataComponentPatch.CODEC, this.patch);
    }
}
