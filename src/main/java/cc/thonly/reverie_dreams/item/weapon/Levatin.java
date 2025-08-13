package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntityImpl;
import cc.thonly.reverie_dreams.item.base.BasicPolymerSwordItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class Levatin extends BasicPolymerSwordItem {
    public static final ToolMaterial LEVATIN = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 1561, 8.0f, 5.5f, 10, ItemTags.NETHERITE_TOOL_MATERIALS);

    public Levatin(String path, float attackDamage, float attackSpeed, Settings settings) {
        super(path, LEVATIN, attackDamage + 1, attackSpeed - 2.4f, settings);
    }
    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.getWorld().isClient){
            tryBreakEquipments(target);
        }

        super.postHit(stack, target, attacker);
    }
    private void tryBreakEquipments(LivingEntity target){
        //对玩家和女仆不生效 避免炸玩家护甲/工具
        if (target instanceof PlayerEntity||target instanceof NPCRoleEntityImpl)
            return;

        ServerWorld world = (ServerWorld) target.getWorld();
        int i = world.random.nextBetween(0, EquipmentSlot.values().length-1);
        breakSlot(target,EquipmentSlot.values()[i]);
    }
    private void breakSlot(LivingEntity target,EquipmentSlot eSlot){
        ServerWorld world = (ServerWorld) target.getWorld();

        ItemStack stack = target.getEquippedStack(eSlot);
        if (stack!=null&&stack.isDamageable()) {
            stack.damage(stack.getMaxDamage(), target, eSlot);
            world.playSound(target,target.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK.value(), SoundCategory.PLAYERS,1,1);
        }
    }

}
