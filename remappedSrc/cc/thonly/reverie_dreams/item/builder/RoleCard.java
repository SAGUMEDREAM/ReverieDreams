package cc.thonly.reverie_dreams.item.builder;

import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.datagen.generator.RecipeTypeProvider;
import cc.thonly.reverie_dreams.entity.npc.NPCRole;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.item.base.SpawnEggItem;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe;
import cc.thonly.reverie_dreams.registry.*;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class RoleCard implements CodecStep<RoleCard>, OwnerBinding<RoleCard>, BuiltinObject {
    public static final Codec<RoleCard> CODEC = Codec.unit(RoleCard::new);
    public static final Long DEFAULT_COLOR = 16777215L;
    @Setter
    @Getter
    private IntrinsicalRegister<RoleCard> owner;
    @Setter
    @Getter
    private ResourceLocation id;
    private ResourceLocation itemId;
    private Long color = DEFAULT_COLOR;
    private final List<NPCRole> entries = new LinkedList<>();

    private RoleCard() {
    }

    public RoleCard(ResourceLocation id, Long color, List<NPCRole> roles) {
        this.id = id;
        this.color = color;
        this.of(roles);
    }

    public RoleCard(ResourceLocation id, Long color, NPCRole... roles) {
        this.id = id;
        this.color = color;
        this.of(roles);
    }

    @Nullable
    public static RoleCard findRoleCard(NPCRole role) {
        Stream<RoleCard> stream = RegistryManager.ROLE_CARD.values().stream().filter(card -> card.entries.contains(role));
        return stream.findFirst()
                .orElse(null);
    }

    public RoleCard of(List<NPCRole> roles) {
        for (NPCRole role : roles) {
            if (!this.entries.contains(role)) {
                this.entries.add(role);
            }
        }
        return this;
    }

    public RoleCard of(NPCRole... roles) {
        return this.of(Arrays.asList(roles));
    }

    public ItemStack itemStack() {
        ItemStack itemStack = new ItemStack(ModItems.ROLE_CARD, 1);
        itemStack.set(DataComponents.ITEM_NAME, Component.translatable(this.translationKey()));
        itemStack.set(DataComponents.DYED_COLOR, new DyedItemColor(this.color.intValue()));
        itemStack.set(ModDataComponentTypes.ROLE_CARD_ID, this.getId());
        return itemStack.copy();
    }

    public Stream<NPCRole> stream() {
        return this.entries.stream();
    }

    public Optional<NPCRole> random() {
        if (this.entries.isEmpty()) return Optional.empty();
        int index = ThreadLocalRandom.current().nextInt(this.entries.size());
        return Optional.ofNullable(this.entries.get(index));
    }

    public RoleCard build() {
        this.itemId = ResourceLocation.fromNamespaceAndPath(this.id.getNamespace(), this.id.getPath() + "_role_card");
        for (NPCRole entry : this.entries) {
            Item egg = entry.getEgg();
            if (egg instanceof SpawnEggItem eggItem) {
                eggItem.setColor(this.color);
            }
        }
        return this;
    }

    public String translationKey() {
        return this.itemId.toLanguageKey();
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public RecipeBuilder createRecipeBuilder() {
        return new RecipeBuilder(this);
    }

    @Override
    public Codec<RoleCard> getCodec() {
        return CODEC;
    }

    @Getter
    public static class RecipeBuilder {
        private static final int[] INDEXES = new int[]{0, 1, 3, 4, 6, 7};
        private static final int[] POINT_INDEXES = new int[]{0, 1, 3};
        private static final int[] POWER_INDEXES = new int[]{4, 6, 7};
        private static final int[] EMPTY_INDEXES = new int[]{2, 5};
        private final RoleCard roleCard;
        private final List<ItemStackWrapper> itemStackWrappers;
        private GensokyoAltarRecipe result;
        private int plus = 0;

        public RecipeBuilder(RoleCard roleCard) {
            this.roleCard = roleCard;
            this.itemStackWrappers = new ArrayList<>();
        }

        public RecipeBuilder plus() {
            return this.plus(1);
        }

        public RecipeBuilder plus(int value) {
            this.plus += value;
            return this;
        }

        public RecipeBuilder itemStack(ItemStackWrapper... recipeWrappers) {
            Collections.addAll(this.itemStackWrappers, recipeWrappers);
            return this;
        }

        public RecipeBuilder itemStack(List<ItemStackWrapper> recipeWrappers) {
            this.itemStackWrappers.addAll(recipeWrappers);
            return this;
        }

        public RecipeBuilder build() {
            List<ItemStackWrapper> wrappers = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                wrappers.add(ItemStackWrapper.empty());
            }
            for (int i = 0; i < POINT_INDEXES.length; i++) {
                int pointIndex = POINT_INDEXES[i];
                wrappers.set(pointIndex, ItemStackWrapper.of(ModItems.POINT, 14 + i + this.plus));
            }
            for (int i = 0; i < POWER_INDEXES.length; i++) {
                int powerIndex = POWER_INDEXES[i];
                wrappers.set(powerIndex, ItemStackWrapper.of(ModItems.POWER, 18 + i + this.plus));
            }
            PrimitiveIterator.OfInt iterator = Arrays.stream(EMPTY_INDEXES).iterator();
            for (ItemStackWrapper itemStackWrapper : this.itemStackWrappers) {
                if (!iterator.hasNext()) continue;
                Integer next = iterator.next();
                wrappers.set(next, itemStackWrapper);
            }

            this.result = new GensokyoAltarRecipe(
                    ItemStackWrapper.of(ModItems.ROLE_CARD.getDefaultInstance()),
                    wrappers,
                    ItemStackWrapper.of(this.roleCard.itemStack())
            );
            return this;
        }

        public void apply(RecipeTypeProvider.Factory<GensokyoAltarRecipe> factory) {
            factory.register(this.roleCard.getId(), this.result);
        }
    }
}
