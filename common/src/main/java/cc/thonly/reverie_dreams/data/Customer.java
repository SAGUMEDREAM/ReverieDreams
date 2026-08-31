package cc.thonly.reverie_dreams.data;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.RoleType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.NPCRoleTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@ToString
public class Customer {
    public static final Identifier UNDEFINED_KEY = ReverieDreams.id("undefined");
    public static final Customer UNDEFINED = new Customer(
            NPCRoleTypes.REIMU,
            false,
            CustomerBudget.MAX,
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(Stream.of("npc.event.send_message.0", "npc.event.send_message.1", "npc.event.send_message.2", "npc.event.send_message.3", "npc.event.send_message.4", "npc.event.send_message.5", "npc.event.send_message.6", "npc.event.send_message.7", "npc.event.send_message.8", "npc.event.send_message.9").map(Component::translatable).toList()),
            new ArrayList<>(),
            new ArrayList<>(),
            new CustomerEvaluation()
    );
    public static final Codec<Customer> CODEC = Codec.lazyInitialized(() -> {
        return Identifier.CODEC.xmap(
                BuiltInRegistryProviders.CUSTOMER::getValue, entry -> {
                    Identifier key = BuiltInRegistryProviders.CUSTOMER.getKey(entry);
                    if (key == null) {
                        return UNDEFINED_KEY;
                    }
                    return key;
                }
        );
    });

    final RoleType roleType;
    final boolean rare;
    final CustomerBudget budget;
    final List<FoodProperty> likes;
    final List<FoodProperty> dislikes;
    final List<BeverageProperty> beverages;
    final List<Component> chats;
    final List<Component> orders;
    final List<Component> descriptions;
    final CustomerEvaluation evaluation;

    public Customer(RoleType roleType,
                    boolean rare,
                    CustomerBudget budget,
                    List<FoodProperty> likes,
                    List<FoodProperty> dislikes,
                    List<BeverageProperty> beverages,
                    List<Component> chats,
                    List<Component> orders,
                    List<Component> descriptions,
                    CustomerEvaluation evaluation
    ) {
        this.roleType = roleType;
        this.rare = rare;
        this.budget = budget;
        this.likes = likes;
        this.dislikes = dislikes;
        this.beverages = beverages;
        this.chats = chats;
        this.orders = orders;
        this.descriptions = descriptions;
        this.evaluation = evaluation;
    }

    public RoleType roleType() {
        return this.roleType;
    }

    public boolean rare() {
        return this.rare;
    }

    public CustomerBudget budget() {
        return this.budget;
    }

    public List<FoodProperty> likes() {
        return List.copyOf(this.likes);
    }

    public List<FoodProperty> dislikes() {
        return List.copyOf(this.dislikes);
    }

    public List<BeverageProperty> beverages() {
        return List.copyOf(this.beverages);
    }

    public List<Component> chats() {
        return List.copyOf(this.chats);
    }

    public List<Component> orders() {
        return List.copyOf(this.orders);
    }

    public List<Component> descriptions() {
        return List.copyOf(this.descriptions);
    }

    public CustomerEvaluation evaluation() {
        return this.evaluation;
    }

    public Data toObject() {
        return new Data(
                this.roleType.identify(),
                this.rare,
                this.budget,
                this.likes.stream().map(FoodProperty::getId).toList(),
                this.dislikes.stream().map(FoodProperty::getId).toList(),
                this.beverages.stream().map(BeverageProperty::getId).toList(),
                this.chats.stream().map(component -> {
                    if (component.getContents() instanceof TranslatableContents translatableContents) {
                        return translatableContents.getKey();
                    }
                    if (component.getContents() instanceof PlainTextContents plainTextContents) {
                        return plainTextContents.text();
                    }
                    return "???";
                }).toList(),
                this.orders.stream().map(component -> {
                    if (component.getContents() instanceof TranslatableContents translatableContents) {
                        return translatableContents.getKey();
                    }
                    if (component.getContents() instanceof PlainTextContents plainTextContents) {
                        return plainTextContents.text();
                    }
                    return "???";
                }).toList(),
                this.descriptions.stream().map(component -> {
                    if (component.getContents() instanceof TranslatableContents translatableContents) {
                        return translatableContents.getKey();
                    }
                    if (component.getContents() instanceof PlainTextContents plainTextContents) {
                        return plainTextContents.text();
                    }
                    return "???";
                }).toList(),
                this.evaluation
        );
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Data {
        public static final Codec<Data> CODEC = Codec.lazyInitialized(() -> {
            return RecordCodecBuilder.create(instance -> instance.group(
                    Identifier.CODEC.optionalFieldOf("role_type", ReverieDreams.id("undefined")).forGetter(Data::getRoleType),
                    Codec.BOOL.optionalFieldOf("rare", false).forGetter(Data::isRare),
                    CustomerBudget.CODEC.optionalFieldOf("budget", CustomerBudget.MAX).forGetter(Data::getBudget),
                    Identifier.CODEC.listOf().optionalFieldOf("likes", new ArrayList<>()).forGetter(Data::getLikes),
                    Identifier.CODEC.listOf().optionalFieldOf("dislikes", new ArrayList<>()).forGetter(Data::getDislikes),
                    Identifier.CODEC.listOf().optionalFieldOf("beverages", new ArrayList<>()).forGetter(Data::getBeverages),
                    Codec.STRING.listOf().optionalFieldOf("chats", new ArrayList<>()).forGetter(Data::getChats),
                    Codec.STRING.listOf().optionalFieldOf("orders", new ArrayList<>()).forGetter(Data::getOrders),
                    Codec.STRING.listOf().optionalFieldOf("descriptions", new ArrayList<>()).forGetter(Data::getDescriptions),
                    CustomerEvaluation.CODEC.optionalFieldOf("evaluation", new CustomerEvaluation()).forGetter(Data::getEvaluation)
            ).apply(instance, Data::new));
        });
        Identifier roleType = ReverieDreams.id("undefined");
        boolean rare = false;
        CustomerBudget budget = CustomerBudget.MAX;
        List<Identifier> likes = new ArrayList<>();
        List<Identifier> dislikes = new ArrayList<>();
        List<Identifier> beverages = new ArrayList<>();
        List<String> chats = new ArrayList<>();
        List<String> orders = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();
        CustomerEvaluation evaluation = new CustomerEvaluation();

        public Optional<Customer> build() {
            RoleType roleType = BuiltInRegistryProviders.ROLE_TYPE_MERGED.getValue(this.roleType);
            if (roleType == null) {
                log.error("Missing role_type {} in registry", this.roleType);
                return Optional.empty();
            }
            List<FoodProperty> likes = new ArrayList<>();
            for (Identifier like : this.likes) {
                BuiltInRegistryProviders.FOOD_PROPERTY.get(like).ifPresent(reference -> likes.add(reference.value()));
            }
            List<FoodProperty> dislikes = new ArrayList<>();
            for (Identifier like : this.dislikes) {
                BuiltInRegistryProviders.FOOD_PROPERTY.get(like).ifPresent(reference -> dislikes.add(reference.value()));
            }
            List<BeverageProperty> beverage = new ArrayList<>();
            for (Identifier like : this.beverages) {
                BuiltInRegistryProviders.BEVERAGE_PROPERTY.get(like).ifPresent(reference -> beverage.add(reference.value()));
            }
            return Optional.of(
                    new Customer(
                            roleType,
                            this.rare,
                            this.budget,
                            likes,
                            dislikes,
                            beverage,
                            new ArrayList<>(this.chats.stream().map(Component::translatable).toList()),
                            new ArrayList<>(this.orders.stream().map(Component::translatable).toList()),
                            new ArrayList<>(this.descriptions.stream().map(Component::translatable).toList()),
                            this.evaluation
                    )
            );
        }

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        public Customer buildOrThrow() {
            return this.build().get();
        }

    }
}
