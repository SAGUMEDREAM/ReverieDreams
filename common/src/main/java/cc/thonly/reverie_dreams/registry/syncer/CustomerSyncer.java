package cc.thonly.reverie_dreams.registry.syncer;

import cc.thonly.reverie_dreams.data.Customer;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.registry.impl.RegistrySyncer;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class CustomerSyncer implements Supplier<RegistrySyncer<Customer, Customer.Data>> {
    @Override
    public RegistrySyncer<Customer, Customer.Data> get() {
        return new Impl(
                BuiltInRegistryProviders.CUSTOMER,
                Customer.Data.CODEC,
                new RegistrySyncer.ClientReloadListener<>() {
                    @Override
                    public void preProcessing(RegistryProvider<Customer> registry) {
                        BuiltInRegistryProviders.CUSTOMER.clear();
                    }

                    @Override
                    public void afterProcessing(RegistryProvider<Customer> registry) {

                    }

                    @Override
                    public Customer update(Identifier key, @Nullable Customer old, Customer.Data data) {
                        return data.buildOrThrow();
                    }

                    @Override
                    public RegistrySyncer<Customer, Customer.Data> getSyncer() {
                        return BuiltInRegistryProviders.CUSTOMER.getSyncer();
                    }
                }
        );
    }

    public static class Impl extends RegistrySyncer<Customer, Customer.Data> {

        public Impl(RegistryProvider<Customer> registry, Codec<Customer.Data> dataCodec, ClientReloadListener<Customer, Customer.Data> clientReloadListener) {
            super(registry, dataCodec, clientReloadListener);
        }

        @Override
        public Customer toT(Customer.Data data) {
            return data.buildOrThrow();
        }

        @Override
        public Customer.Data toD(Customer customer) {
            return customer.toObject();
        }
    }
}
