package cc.thonly.reverie_dreams.paper.npc;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.DamageTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.EquipmentSlot;

/** NPCSimpleEntity.hurtArmor/hurtHelmet; native Wolf only wears BODY equipment. */
final class NPCArmorWearBridge implements Listener {
    private final NPCAiService ai;
    NPCArmorWearBridge(NPCAiService ai) { this.ai=ai; }
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void damage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf) || !ai.service.isNPC(wolf)) return;
        float helmet=(float)event.getDamage();
        helmet+=(float)modifier(event,DamageModifier.INVULNERABILITY_REDUCTION);
        helmet+=(float)modifier(event,DamageModifier.BLOCKING);
        helmet+=(float)modifier(event,DamageModifier.FREEZING);
        boolean changed=false;
        if (tag(event,DamageTypeTagKeys.DAMAGES_HELMET)) changed=wear(wolf,event,helmet,EquipmentSlot.HEAD);
        if (!tag(event,DamageTypeTagKeys.BYPASSES_ARMOR)) {
            float armor=helmet; armor+=(float)modifier(event,DamageModifier.HARD_HAT);
            for(var slot:new EquipmentSlot[]{EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD}) changed=wear(wolf,event,armor,slot)||changed;
        }
        if(changed) ai.syncNativeEquipment(wolf);
    }
    private static double modifier(EntityDamageEvent event,DamageModifier modifier) { return event.isApplicable(modifier)?event.getDamage(modifier):0; }
    static int wearAmount(float amount) { return amount<=0?0:(int)Math.max(1f,amount/4f); }
    private static boolean tag(EntityDamageEvent event,TagKey<DamageType> key) {
        var registry=RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE);
        return registry.getTag(key).contains(TypedKey.create(RegistryKey.DAMAGE_TYPE,event.getDamageSource().getDamageType().getKey().asString()));
    }
    private static boolean wear(Wolf wolf,EntityDamageEvent event,float amount,EquipmentSlot slot) {
        int damage=wearAmount(amount); if(damage==0)return false;
        var item=wolf.getEquipment().getItem(slot);if(item==null||item.isEmpty())return false;var equippable=item.getData(DataComponentTypes.EQUIPPABLE);
        if(equippable==null||!equippable.damageOnHurt()||item.getDataOrDefault(DataComponentTypes.MAX_DAMAGE,0)<=0||!item.hasData(DataComponentTypes.DAMAGE)||item.hasData(DataComponentTypes.UNBREAKABLE))return false;
        var resistant=item.getData(DataComponentTypes.DAMAGE_RESISTANT);
        if(resistant!=null&&resistant.types().contains(TypedKey.create(RegistryKey.DAMAGE_TYPE,event.getDamageSource().getDamageType().getKey().asString())))return false;
        wolf.damageItemStack(slot,damage);return true;
    }
}
