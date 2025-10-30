package cc.thonly.reverie_dreams.mixin.patches;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.function.Consumer;

@Mixin(PackRepository.class)
@Pseudo
@Slf4j
public class ResourcePackManagerMixin {
    @Shadow @Final private Set<RepositorySource> providers;

    @Redirect(
            method = "providePackProfiles",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resource/ResourcePackProvider;register(Ljava/util/function/Consumer;)V"
            )
    )
    private void redirectRegister(RepositorySource instance, Consumer<Pack> consumer) {
        try {
            instance.loadPacks(consumer);
        } catch (Exception e) {
            log.error("ResourcePackProvider register failed: ", e);
        }
    }
}
