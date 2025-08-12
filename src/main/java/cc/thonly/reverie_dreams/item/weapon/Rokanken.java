package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.item.base.BasicPolymerSwordItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class Rokanken extends BasicPolymerSwordItem implements YoumuSwordUsing{
    public static final ToolMaterial ROKANKEN = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 1250, 8.0f, 5.5f, 10, ModTags.ItemTypeTag.SILVER_BLOCK);

    public Rokanken(String path, float attackDamage, float attackSpeed, Settings settings) {
        super(path, ROKANKEN, attackDamage + 1f, attackSpeed - 2.4f, settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        return this.useItem(world, user, hand);
    }
}
