package cc.thonly.reverie_dreams.fabric.mixin.client;

import cc.thonly.reverie_dreams.client.pak.PakSkinPacks;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Arrays;

/** Adds one independent source only to the physical client's resource repository. */
@Mixin(Minecraft.class)
public abstract class PakClientPackMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/packs/repository/PackRepository;<init>([Lnet/minecraft/server/packs/repository/RepositorySource;)V"), index = 0)
    private RepositorySource[] reverieDreams$addPakSource(RepositorySource[] original) {
        RepositorySource[] sources = Arrays.copyOf(original, original.length + 1);
        sources[original.length] = PakSkinPacks.SOURCE;
        return sources;
    }
}
