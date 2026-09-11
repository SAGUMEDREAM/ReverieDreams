package cc.thonly.reverie_dreams.neoforge.mixin;

import dev.architectury.platform.Platform;
import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class RDFabricMixinPlugin implements IMixinConfigPlugin {
    private static final String MORPHER = "sparkle_morpher";
    public static final List<String> YSM_TARGET = List.of(
            "cc.thonly.reverie_dreams.neoforge.mixin.client.BaseNPCLikeEntityRendererMixin",
            "cc.thonly.reverie_dreams.neoforge.mixin.client.BaseNPCLikeEntityMixin");

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    private static boolean isModLoadedEarly(String modId) {
        return FMLLoader.getCurrent()
                .getLoadingModList()
                .getMods()
                .stream()
                .anyMatch(mod -> mod.getModId().equals(modId));
    }

    @Override
    public boolean shouldApplyMixin(
            String targetClassName,
            String mixinClassName
    ) {
        if (YSM_TARGET.stream().anyMatch(mixinClassName::equals)) {
            return isModLoadedEarly(MORPHER);
        }

        return true;
    }

    @Override
    public void acceptTargets(
            Set<String> myTargets,
            Set<String> otherTargets
    ) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo
    ) {
    }

    @Override
    public void postApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo
    ) {
    }

}
