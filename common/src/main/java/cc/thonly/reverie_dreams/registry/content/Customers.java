package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.CraftingConflict;
import cc.thonly.reverie_dreams.data.Customer;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class Customers {

    public static void bootstrap(RegistryProvider<Customer> customers) {
        BuiltInRegistryProviders.registerForBuiltin(customers, Customer.UNDEFINED_KEY, Customer.UNDEFINED);
        customers.defaultId(Customer.UNDEFINED_KEY);
        customers.addAliaName(ReverieDreams.id("cyan_reimu"), ReverieDreams.id("reimu"));
    }

    public static void reload(ResourceManager manager) {
        BuiltInRegistryProviders.CUSTOMER.clear();
        Map<Identifier, Resource> resources = manager.listResources("customer", id ->
                id.getPath().endsWith(".json")
        );
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Resource resource = entry.getValue();
            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                DataResult<Customer.Data> result = Customer.Data.CODEC.parse(JsonOps.INSTANCE, json);
                Optional<Customer.Data> optional = result.result();
                if (optional.isPresent()) {
                    Customer.Data data = optional.get();
                    Identifier roleType = data.getRoleType();
                    Customer customer = data.buildOrThrow();
                    BuiltInRegistryProviders.register(BuiltInRegistryProviders.CUSTOMER, roleType, customer);
                } else {
                    log.error("Failed to parse crafting_conflict {}: {}", entry.getKey(), result.error().map(Object::toString).orElse("Unknown error"));
                }
            } catch (IOException e) {
                log.error("Failed to load food_property {}: {}", entry.getKey(), e.getMessage(), e);
            }
        }
    }

}
