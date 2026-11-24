package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.variant.RabbitUnitLoadout;
import cc.thonly.reverie_dreams.item.weapon.LunarWeaponType;
import org.junit.jupiter.api.Test;
import java.util.EnumMap;
import java.util.EnumSet;
import static org.junit.jupiter.api.Assertions.*;

class RabbitUnitLoadoutTest {
    @Test void everySpawnSlotMatchesTheDeclaredMixWithSwordShieldAndAllFiveGuns(){
        var counts=new EnumMap<RabbitUnitLoadout,Integer>(RabbitUnitLoadout.class);
        for(int roll=0;roll<100;roll++)counts.merge(RabbitUnitLoadout.fromRoll(roll),1,Integer::sum);
        assertEquals(6,counts.size());
        for(var loadout:RabbitUnitLoadout.values())assertEquals(loadout.weight(),counts.get(loadout).intValue());
        assertEquals(25,counts.get(RabbitUnitLoadout.SWORD_SHIELD));
        assertThrows(IllegalArgumentException.class,()->RabbitUnitLoadout.fromRoll(-1));
        assertThrows(IllegalArgumentException.class,()->RabbitUnitLoadout.fromRoll(100));
    }
    @Test void eachGunHasOneReachableTacticalProfile(){
        var types=EnumSet.noneOf(LunarWeaponType.class);
        for(var loadout:RabbitUnitLoadout.values())if(loadout.weapon()!=null){
            assertTrue(types.add(loadout.weapon()));assertSame(loadout,RabbitUnitLoadout.forWeapon(loadout.weapon()));
            assertTrue(loadout.minimumRange()>0&&loadout.minimumRange()<loadout.maximumRange());
            assertTrue(loadout.maximumRange()<=32&&loadout.aimTicks()>0&&loadout.burstSize()>0&&loadout.burstPause()>0);
        }
        assertEquals(EnumSet.allOf(LunarWeaponType.class),types);
    }
    @Test void closeRangeAndLongRangeGunsUseDifferentEngagements(){
        assertTrue(RabbitUnitLoadout.SHOTGUN.maximumRange()<RabbitUnitLoadout.RIFLE.maximumRange());
        assertTrue(RabbitUnitLoadout.SNIPER.minimumRange()>RabbitUnitLoadout.SHOTGUN.maximumRange());
        assertTrue(RabbitUnitLoadout.SNIPER.aimTicks()>RabbitUnitLoadout.PISTOL.aimTicks());
        assertEquals(3,RabbitUnitLoadout.RIFLE.burstSize());assertEquals(5,RabbitUnitLoadout.SMG.burstSize());
        assertEquals(1,RabbitUnitLoadout.SNIPER.burstSize());
    }
}
