package cc.thonly.reverie_dreams.neoforge.compat;

import cc.thonly.reverie_dreams.compat.IReverieDreamsCompats;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReverieDreamsNeoForgeCompats extends IReverieDreamsCompats {
    public static void initialize() {
        IReverieDreamsCompats.initialize();
//        if (PlatformContext.isModLoaded("roughlyenoughitems")) {
//            load("roughlyenoughitems", "cc.thonly.reverie_dreams.compat.rei.loader.IReiCompatLoader");
//            if (PlatformContext.isClientSide()) {
//                load("roughlyenoughitems", "cc.thonly.reverie_dreams.compat.rei.loader.IClientReiCompatLoader");
//            }
//        }
    }
}
