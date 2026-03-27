package cc.thonly.reverie_dreams.item.builder;

import cc.thonly.reverie_dreams.data.npc.NPCRole;
import cc.thonly.reverie_dreams.datagen.generator.AbstractRecipeTypeProvider;
import cc.thonly.reverie_dreams.item.base.SpawnEggItem;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import cc.thonly.reverie_dreams.util.UnitCodec;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@ToString
public class RoleCard implements CodecStep<RoleCard>, OwnerBinding<RoleCard>, BuiltinObject {
    public static final Codec<RoleCard> CODEC = UnitCodec.unit(RoleCard::new);
    public static final Long DEFAULT_COLOR = 16777215L;
    @Setter
    @Getter
    private RegistryHandler<RoleCard> owner;
    @Setter
    @Getter
    private Identifier id;
    private Identifier itemId;
    private Long color = DEFAULT_COLOR;
    private final List<NPCRole> entries = new LinkedList<>();

    private RoleCard() {
    }

    public RoleCard(Identifier id, Long color, List<NPCRole> roles) {
        this.id = id;
        this.color = color;
        this.of(roles);
    }

    public RoleCard(Identifier id, Long color, NPCRole... roles) {
        this.id = id;
        this.color = color;
        this.of(roles);
    }

    @Nullable
    public static RoleCard findRoleCard(NPCRole role) {
        Stream<RoleCard> stream = RegistryHandlers.ROLE_CARD.values().stream().filter(card -> card.entries.contains(role));
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
        ItemStack itemStack = new ItemStack(RDItems.ROLE_CARD, 1);
        itemStack.set(DataComponents.ITEM_NAME, Component.translatable(this.translationKey()));
        itemStack.set(DataComponents.DYED_COLOR, new DyedItemColor(this.color.intValue()));
        itemStack.set(RDDataComponents.ROLE_CARD_ID, this.getId());
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
        this.itemId = Identifier.fromNamespaceAndPath(this.id.getNamespace(), this.id.getPath() + "_role_card");
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
                wrappers.set(pointIndex, ItemStackWrapper.of(RDItems.POINT, 14 + i + this.plus));
            }
            for (int i = 0; i < POWER_INDEXES.length; i++) {
                int powerIndex = POWER_INDEXES[i];
                wrappers.set(powerIndex, ItemStackWrapper.of(RDItems.POWER, 18 + i + this.plus));
            }
            PrimitiveIterator.OfInt iterator = Arrays.stream(EMPTY_INDEXES).iterator();
            for (ItemStackWrapper itemStackWrapper : this.itemStackWrappers) {
                if (!iterator.hasNext()) continue;
                Integer next = iterator.next();
                wrappers.set(next, itemStackWrapper);
            }

            this.result = new GensokyoAltarRecipe(
                    ItemStackWrapper.of(RDItems.ROLE_CARD.getDefaultInstance()),
                    wrappers,
                    ItemStackWrapper.of(this.roleCard.itemStack())
            );
            return this;
        }

        public void apply(AbstractRecipeTypeProvider.Factory<GensokyoAltarRecipe> factory) {
            factory.register(this.roleCard.getId(), this.result);
        }
    }
}
