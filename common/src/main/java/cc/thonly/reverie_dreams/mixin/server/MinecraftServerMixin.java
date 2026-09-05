package cc.thonly.reverie_dreams.mixin.server;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.nota.NotaAPI;
import cc.thonly.reverie_dreams.item.armor.EarphoneItem;
import cc.thonly.reverie_dreams.server.component.ServerPlayerComponentManager;
import com.mojang.datafixers.DataFixer;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.Optional;
import java.util.function.BooleanSupplier;

@Slf4j
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(Thread serverThread, LevelStorageSource.LevelStorageAccess storageSource, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer fixerUpper, Services services, LevelLoadListener levelLoadListener, CallbackInfo ci) {
        MinecraftServer minecraftServer = (MinecraftServer) (Object) this;
        ReverieDreams.setServer(minecraftServer);
        ServerPlayerComponentManager.serverAccess().onLoad(minecraftServer);
        NotaAPI.getAPI().setServer(minecraftServer);
    }

    @Inject(method = "tickServer", at = @At("TAIL"))
    public void onTickEnd(BooleanSupplier haveTime, CallbackInfo ci) {
        EarphoneItem.VEC_3_DS.clear();
    }

}
