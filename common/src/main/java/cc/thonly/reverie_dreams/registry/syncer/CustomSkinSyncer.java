package cc.thonly.reverie_dreams.registry.syncer;

import cc.thonly.reverie_dreams.data.skin.CustomSkinConfig;
import cc.thonly.reverie_dreams.data.skin.CustomType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.registry.impl.RegistrySyncer;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class CustomSkinSyncer implements Supplier<RegistrySyncer<CustomType, CustomSkinConfig>> {
    @Override
    public RegistrySyncer<CustomType, CustomSkinConfig> get() {
        return new Impl(BuiltInRegistryProviders.CUSTOM_SKIN_TYPE,
                CustomSkinConfig.CODEC,
                new RegistrySyncer.ClientReloadListener<>() {
                    @Override
                    public void preProcessing(RegistryProvider<CustomType> registry) {
                        for (CustomType skinType : BuiltInRegistryProviders.CUSTOM_SKIN_TYPE.values()) {
                            Optional<ResourceKey<CustomType>> resourceKey = BuiltInRegistryProviders.CUSTOM_SKIN_TYPE.getResourceKey(skinType);
                            if (resourceKey.isEmpty()) {
                                continue;
                            }
                            BuiltInRegistryProviders.CUSTOM_SKIN_TYPE.unregister(resourceKey.get());
                        }
                    }

                    @Override
                    public void afterProcessing(RegistryProvider<CustomType> registry) {

                    }

                    @Override
                    public CustomType update(Identifier key,
                                           @Nullable CustomType old,
                                           CustomSkinConfig data
                    ) {
                        return data.value();
                    }

                    @Override
                    public RegistrySyncer<CustomType, CustomSkinConfig> getSyncer() {
                        return BuiltInRegistryProviders.CUSTOM_SKIN_TYPE.getSyncer();
                    }

                }
        );
    }

    public static class Impl extends RegistrySyncer<CustomType, CustomSkinConfig> {

        public Impl(RegistryProvider<CustomType> registry, Codec<CustomSkinConfig> codec, ClientReloadListener<CustomType, CustomSkinConfig> clientReloadListener) {
            super(registry, codec, clientReloadListener);
        }

        @Override
        public CustomType toT(CustomSkinConfig config) {
            return config.value();
        }

        @Override
        public CustomSkinConfig toD(CustomType skinType) {
            return new CustomSkinConfig(skinType.getId(), skinType.getConfig().getType(), skinType.getConfig().getCapeTexture(), skinType.getConfig().getElytraTexture());
        }
    }

}
