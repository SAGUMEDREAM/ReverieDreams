package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.entity.RabbitUnit;
import cc.thonly.reverie_dreams.item.weapon.LunarWeaponType;
import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Spawn mix and NPC tactics, independent of the soldier's three cosmetic variants. */
public enum RabbitUnitLoadout {
    SWORD_SHIELD(null,25,0,0,0,0,0),
    RIFLE(LunarWeaponType.RIFLE,25,7,20,6,3,14),
    SMG(LunarWeaponType.SMG,20,4,14,5,5,12),
    SHOTGUN(LunarWeaponType.SHOTGUN,15,2.5,8,5,1,32),
    PISTOL(LunarWeaponType.PISTOL,10,3,12,4,1,16),
    SNIPER(LunarWeaponType.SNIPER,5,12,28,16,1,48);

    private final LunarWeaponType weapon;
    private final int weight,aimTicks,burstSize,burstPause;
    private final double minimumRange,maximumRange;

    RabbitUnitLoadout(LunarWeaponType weapon,int weight,double minimumRange,double maximumRange,int aimTicks,int burstSize,int burstPause) {
        this.weapon=weapon;this.weight=weight;this.minimumRange=minimumRange;this.maximumRange=maximumRange;
        this.aimTicks=aimTicks;this.burstSize=burstSize;this.burstPause=burstPause;
    }
    public LunarWeaponType weapon(){return this.weapon;}
    public int weight(){return this.weight;}
    public double minimumRange(){return this.minimumRange;}
    public double maximumRange(){return this.maximumRange;}
    public int aimTicks(){return this.aimTicks;}
    public int burstSize(){return this.burstSize;}
    public int burstPause(){return this.burstPause;}
    public static RabbitUnitLoadout random(RandomSource random){return fromRoll(random.nextInt(100));}
    public static RabbitUnitLoadout fromRoll(int roll){
        if(roll<0||roll>=100)throw new IllegalArgumentException("Loadout roll must be in [0,100)");
        for(var loadout:values()){if(roll<loadout.weight)return loadout;roll-=loadout.weight;}
        throw new IllegalStateException("Loadout weights must total 100");
    }
    public static RabbitUnitLoadout forWeapon(LunarWeaponType type){
        for(var loadout:values())if(loadout.weapon==type)return loadout;
        throw new IllegalArgumentException("Unknown lunar weapon: "+type);
    }
    public void equip(RabbitUnit unit){
        var item=switch(this){
            case SWORD_SHIELD->RDItems.SILVER_SWORD;case RIFLE->RDItems.LUNAR_TYPE_1_RIFLE;case PISTOL->RDItems.LUNAR_TYPE_2_PISTOL;
            case SHOTGUN->RDItems.LUNAR_TYPE_3_SHOTGUN;case SMG->RDItems.LUNAR_TYPE_4_SMG;case SNIPER->RDItems.LUNAR_TYPE_5_SNIPER;
        };
        ItemStack stack=item.createStack();
        if(stack.getItem() instanceof WeaponOfTheMoon gun)gun.setRounds(stack,gun.capacity());
        unit.setItemSlot(EquipmentSlot.MAINHAND,stack);
        unit.setItemSlot(EquipmentSlot.OFFHAND,this==SWORD_SHIELD?new ItemStack(Items.SHIELD):ItemStack.EMPTY);
    }
}
