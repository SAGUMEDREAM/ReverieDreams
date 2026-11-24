package cc.thonly.reverie_dreams.item.weapon;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LunarWeaponBalanceTest {
    @org.junit.jupiter.api.BeforeAll static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }
    @Test void experienceIncreasesEveryLevelAndStopsAtThirtyWithoutNegativeBonus() {
        for(var type:LunarWeaponType.values()) {
            assertEquals(type.damage(),type.damageAtLevel(0),1e-6);
            assertEquals(type.damage(),type.damageAtLevel(-100),1e-6);
            for(int level=1;level<=30;level++) assertTrue(type.damageAtLevel(level)>type.damageAtLevel(level-1));
            assertEquals(type.damage()*1.25f,type.damageAtLevel(30),1e-5);
            assertEquals(type.damageAtLevel(30),type.damageAtLevel(Integer.MAX_VALUE));
        }
        assertEquals(35,LunarWeaponType.SNIPER.damageAtLevel(30),1e-6);
        assertTrue(LunarWeaponType.SNIPER.damage()>LunarWeaponType.SHOTGUN.damage()*LunarWeaponType.SHOTGUN.pellets());
    }
    @Test void fasterWeaponsTradePerShotDamageForCadence() {
        assertTrue(LunarWeaponType.SNIPER.shotInterval()>LunarWeaponType.PISTOL.shotInterval());
        assertTrue(LunarWeaponType.PISTOL.damage()>LunarWeaponType.RIFLE.damage());
        assertTrue(LunarWeaponType.RIFLE.damage()>LunarWeaponType.SMG.damage());
        assertEquals(8,LunarWeaponType.SHOTGUN.pellets());
    }
    @Test void sweptEntityMarginIsLargerButStaysBoundedAcrossAllGuns() {
        // Same scaled margin used by BaseDanmakuEntity + ProjectileUtilMixin.
        float oldMargin=.3f+.5f*(.25f-1)*.5f;
        AABB target=new AABB(1.5,0,-.3,2.1,1.8,.3);
        Vec3 from=new Vec3(0,1,.49),to=new Vec3(4,1,.49);
        assertTrue(target.inflate(oldMargin).clip(from,to).isEmpty());
        for(var type:LunarWeaponType.values()) {
            float margin=.3f+.5f*(type.bulletHitboxScale()-1)*.5f;
            assertTrue(margin>oldMargin && margin<=.3f);
            assertTrue(target.inflate(margin).clip(from,to).isPresent(),type.name());
            assertTrue(target.inflate(margin).clip(new Vec3(0,1,1),new Vec3(4,1,1)).isEmpty());
            // Wall-clipped trace stops before a target further along the same tick's path.
            assertTrue(target.inflate(margin).clip(from,new Vec3(1,1,.49)).isEmpty());
        }
    }
    @Test void everyLoadedRoundIsSpentAndAnEmptyGunCannotFire() {
        for(var type:LunarWeaponType.values()) {
            int remaining=type.capacity(),shots=0;
            while(WeaponOfTheMoon.roundsAfterShot(remaining)>=0){remaining=WeaponOfTheMoon.roundsAfterShot(remaining);shots++;}
            assertEquals(type.capacity(),shots);assertEquals(0,remaining);
        }
        assertEquals(-1,WeaponOfTheMoon.roundsAfterShot(0));
    }
    @Test void creativeReloadKeepsTheSourceAndDoesNotDuplicateReturnedRounds() {
        var swap=WeaponOfTheMoon.planMagazineSwap(7,30,30,true);
        assertNotNull(swap);assertEquals(30,swap.loadedRounds());
        assertFalse(swap.consumeSource());assertEquals(0,swap.returnedRounds());
    }
    @Test void survivalSwapPreservesTheOriginalRemainingRoundAccounting() {
        var swap=WeaponOfTheMoon.planMagazineSwap(7,30,30,false);
        assertNotNull(swap);assertTrue(swap.consumeSource());
        assertEquals(30,swap.loadedRounds());assertEquals(7,swap.returnedRounds());
        assertNull(WeaponOfTheMoon.planMagazineSwap(0,0,30,true));
        assertNull(WeaponOfTheMoon.planMagazineSwap(30,30,30,true));
    }
}
