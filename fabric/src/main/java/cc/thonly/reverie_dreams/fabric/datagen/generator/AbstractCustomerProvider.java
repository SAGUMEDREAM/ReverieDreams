package cc.thonly.reverie_dreams.fabric.datagen.generator;

import cc.thonly.reverie_dreams.data.*;
import cc.thonly.reverie_dreams.data.npc.RoleType;
import cc.thonly.reverie_dreams.fabric.util.DataProviderHelper;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractCustomerProvider implements DataProvider {
    public final FabricPackOutput output;
    public final CompletableFuture<HolderLookup.Provider> future;
    private final Map<Identifier, Customer> registries = new Object2ObjectOpenHashMap<>();

    public AbstractCustomerProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        this.output = output;
        this.future = future;
    }

    public abstract void configured(HolderLookup.Provider provider);

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return this.future.thenAcceptAsync(provider -> {
            this.configured(provider);
            DataProviderHelper.outputFile(cache, this.registries, Customer.Data.CODEC, (id, builder) -> builder.toObject(), "customer");
        });
    }

    public void add(RoleType roleType,
                    boolean rare,
                    CustomerBudget budget,
                    List<FoodProperty> likes,
                    List<FoodProperty> dislikes,
                    List<BeverageProperty> beverages,
                    List<String> chats,
                    List<String> orders,
                    List<String> descriptions,
                    CustomerEvaluation evaluation) {
        Customer customer = new Customer(roleType,
                rare,
                budget,
                likes,
                dislikes,
                beverages,
                new ArrayList<>(chats.stream().map(Component::translatable).toList()),
                new ArrayList<>(orders.stream().map(Component::translatable).toList()),
                new ArrayList<>(descriptions.stream().map(Component::translatable).toList()),
                evaluation
        );
        this.registries.put(roleType.identify(), customer);
        BuiltInRegistryProviders.CUSTOMER.register(BuiltInRegistryProviders.CUSTOMER.createKey(roleType.identify()), customer, RegistrationInfo.BUILT_IN);
    }

    public void addRare(RoleType roleType,
                        CustomerBudget budget,
                        List<FoodProperty> likes,
                        List<FoodProperty> dislikes,
                        List<BeverageProperty> beverages,
                        List<String> chats,
                        List<String> orders,
                        List<String> descriptions,
                        CustomerEvaluation evaluation) {
        Customer customer = new Customer(roleType,
                true,
                budget,
                likes,
                dislikes,
                beverages,
                new ArrayList<>(chats.stream().map(Component::translatable).toList()),
                new ArrayList<>(orders.stream().map(Component::translatable).toList()),
                new ArrayList<>(descriptions.stream().map(Component::translatable).toList()),
                evaluation
        );
        this.registries.put(roleType.identify(), customer);
        BuiltInRegistryProviders.CUSTOMER.register(BuiltInRegistryProviders.CUSTOMER.createKey(roleType.identify()), customer, RegistrationInfo.BUILT_IN);
    }

    public void addSimple(RoleType roleType,
                          CustomerBudget budget,
                          List<FoodProperty> likes,
                          List<FoodProperty> dislikes,
                          List<BeverageProperty> beverages,
                          List<String> chats,
                          List<String> orders,
                          List<String> descriptions,
                          CustomerEvaluation evaluation) {
        Customer customer = new Customer(roleType,
                false,
                budget,
                likes,
                dislikes,
                beverages,
                new ArrayList<>(chats.stream().map(Component::translatable).toList()),
                new ArrayList<>(orders.stream().map(Component::translatable).toList()),
                new ArrayList<>(descriptions.stream().map(Component::translatable).toList()),
                evaluation
        );
        this.registries.put(roleType.identify(), customer);
        BuiltInRegistryProviders.CUSTOMER.register(BuiltInRegistryProviders.CUSTOMER.createKey(roleType.identify()), customer, RegistrationInfo.BUILT_IN);
    }

    @Override
    public String getName() {
        return "Customer Provider";
    }
}
