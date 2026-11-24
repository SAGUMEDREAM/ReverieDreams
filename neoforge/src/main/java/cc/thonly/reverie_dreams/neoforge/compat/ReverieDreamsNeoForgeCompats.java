package cc.thonly.reverie_dreams.neoforge.compat;

import cc.thonly.reverie_dreams.compat.ReverieDreamsCompats;
import cc.thonly.reverie_dreams.util.PlatformContext;

@SuppressWarnings("SpellCheckingInspection")
public class ReverieDreamsNeoForgeCompats extends ReverieDreamsCompats {
    public static void initialize() {
        ReverieDreamsCompats.initialize();
        if (PlatformContext.isModLoaded("roughlyenoughitems")) {
            load("roughlyenoughitems", "cc.thonly.reverie_dreams.compat.rei.loader.IReiCompatLoader");
            if (PlatformContext.isClientSide()) {
                load("roughlyenoughitems", "cc.thonly.reverie_dreams.compat.rei.loader.IClientReiCompatLoader");
            }
        }
        initializeNPCModels("cc.thonly.reverie_dreams.neoforge.compat.ysm.ClientInvoker");
    }
}
