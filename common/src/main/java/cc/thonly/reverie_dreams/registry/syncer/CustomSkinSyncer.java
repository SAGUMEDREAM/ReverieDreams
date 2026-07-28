package cc.thonly.reverie_dreams.registry.syncer;

import cc.thonly.reverie_dreams.data.skin.CustomSkinConfig;
import cc.thonly.reverie_dreams.data.skin.CustomType;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
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
        return new Impl(RegistryImpls.CUSTOM_SKIN_TYPE,
                CustomSkinConfig.CODEC,
                new RegistrySyncer.ClientReloadListener<>() {
                    @Override
                    public void preProcessing(RegistryImpl<CustomType> registry) {
                        for (CustomType skinType : RegistryImpls.CUSTOM_SKIN_TYPE.values()) {
                            Optional<ResourceKey<CustomType>> resourceKey = RegistryImpls.CUSTOM_SKIN_TYPE.getResourceKey(skinType);
                            if (resourceKey.isEmpty()) {
                                continue;
                            }
                            RegistryImpls.CUSTOM_SKIN_TYPE.unregister(resourceKey.get());
                        }
                    }

                    @Override
                    public void afterProcessing(RegistryImpl<CustomType> registry) {

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
                        return RegistryImpls.CUSTOM_SKIN_TYPE.getSyncer();
                    }

                }
        );
    }

    public static class Impl extends RegistrySyncer<CustomType, CustomSkinConfig> {

        public Impl(RegistryImpl<CustomType> registry, Codec<CustomSkinConfig> codec, ClientReloadListener<CustomType, CustomSkinConfig> clientReloadListener) {
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
