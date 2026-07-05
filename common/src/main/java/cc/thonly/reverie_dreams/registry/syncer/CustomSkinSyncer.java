package cc.thonly.reverie_dreams.registry.syncer;

import cc.thonly.reverie_dreams.data.skin.CustomSkinConfig;
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

public class CustomSkinSyncer implements Supplier<RegistrySyncer<SkinType, CustomSkinConfig>> {
    @Override
    public RegistrySyncer<SkinType, CustomSkinConfig> get() {
        return new Impl(RegistryImpls.SKIN_TYPE,
                CustomSkinConfig.CODEC,
                new RegistrySyncer.ClientReloadListener<>() {
                    @Override
                    public void preProcessing(RegistryImpl<SkinType> registry) {
                        for (SkinType skinType : RegistryImpls.SKIN_TYPE.values()) {
                            if (skinType instanceof CustomSkinConfig.CustomType) {
                                Optional<ResourceKey<SkinType>> resourceKey = RegistryImpls.SKIN_TYPE.getResourceKey(skinType);
                                if (resourceKey.isEmpty()) {
                                    continue;
                                }
                                RegistryImpls.SKIN_TYPE.unregister(resourceKey.get());
                            }
                        }
                    }

                    @Override
                    public void afterProcessing(RegistryImpl<SkinType> registry) {

                    }

                    @Override
                    public SkinType update(Identifier key,
                                           @Nullable SkinType old,
                                           CustomSkinConfig data
                    ) {
                        return data.value();
                    }

                    @Override
                    public RegistrySyncer<SkinType, CustomSkinConfig> getSyncer() {
                        return RegistryImpls.SKIN_TYPE.getSyncer();
                    }
                }
        );
    }

    public static class Impl extends RegistrySyncer<SkinType, CustomSkinConfig> {

        public Impl(RegistryImpl<SkinType> registry, Codec<CustomSkinConfig> dataCodec, ClientReloadListener<SkinType, CustomSkinConfig> clientReloadListener) {
            super(registry, dataCodec, clientReloadListener);
        }

        @Override
        public SkinType toT(CustomSkinConfig config) {
            return config.value();
        }

        @Override
        public CustomSkinConfig toD(SkinType skinType) {
            return new CustomSkinConfig(skinType.getId(), skinType.getConfig().getType(), skinType.getConfig().getCapeTexture(), skinType.getConfig().getElytraTexture());
        }
    }

}
