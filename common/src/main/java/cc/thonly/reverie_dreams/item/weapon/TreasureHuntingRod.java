package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.keine.tag.ConventionalBlockTags;
import cc.thonly.reverie_dreams.entity.misc.OreEspEntity;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.mixin.accessor.BlockDisplayAccessor;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class TreasureHuntingRod extends SwordItem {
    public static final List<TagKey<Block>> ORE_BLOCK_TAGS = new ArrayList<>();
    public static final ToolMaterial MATERIAL = new ToolMaterial(RDBlockTags.EMPTY, 300, 4.0f, 4.5f, 5, ItemTags.DIAMOND_TOOL_MATERIALS);

    static {
        ORE_BLOCK_TAGS.add(BlockTags.GOLD_ORES);
        ORE_BLOCK_TAGS.add(BlockTags.IRON_ORES);
        ORE_BLOCK_TAGS.add(BlockTags.DIAMOND_ORES);
        ORE_BLOCK_TAGS.add(BlockTags.REDSTONE_ORES);
        ORE_BLOCK_TAGS.add(BlockTags.LAPIS_ORES);
        ORE_BLOCK_TAGS.add(BlockTags.COAL_ORES);
        ORE_BLOCK_TAGS.add(BlockTags.EMERALD_ORES);
        ORE_BLOCK_TAGS.add(BlockTags.COPPER_ORES);
        ORE_BLOCK_TAGS.add(ConventionalBlockTags.ORES);
    }

    public TreasureHuntingRod(float attackDamage, float attackSpeed, Properties settings) {
        super(MATERIAL, attackDamage, attackSpeed, settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide()) {
            ServerPlayer player = (ServerPlayer) user;
            ItemStack stack = player.getItemInHand(hand);
            ItemCooldowns cooldown = player.getCooldowns();

            if (player.isShiftKeyDown()) {
                player.swing(hand);

                BlockPos origin = player.blockPosition();
                int radius = 8;

                // 记录最近矿物数据
                double minDistance = Double.MAX_VALUE;
                BlockPos closestOrePos = null;
                Block closestOreBlock = null;

                for (BlockPos pos : BlockPos.betweenClosed(
                        origin.offset(-radius, -radius, -radius),
                        origin.offset(radius, radius, radius))) {

                    if (!world.isInWorldBounds(pos)) continue;

                    BlockState state = world.getBlockState(pos);
                    if (isOre(state)) {
                        int dx = pos.getX() - origin.getX();
                        int dy = pos.getY() - origin.getY();
                        int dz = pos.getZ() - origin.getZ();
                        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                        if (distance < minDistance) {
                            minDistance = distance;
                            closestOrePos = pos.immutable();
                            closestOreBlock = state.getBlock();
                        }
                    }
                }

                // 找到了矿物
                if (closestOrePos != null) {
                    int dx = closestOrePos.getX() - origin.getX();
                    int dy = closestOrePos.getY() - origin.getY();
                    int dz = closestOrePos.getZ() - origin.getZ();
                    int roundedDistance = (int) minDistance;

                    MutableComponent message = Component.translatable(
                            "message.treasure_hunting_rod.find", roundedDistance, dx, dy, dz
                    ).append(" ").append(Component.translatable(closestOreBlock.getDescriptionId()));
                    if (user instanceof ServerPlayer) {
                        ((ServerPlayer) user).sendSystemMessage(message, false);
                    }
                    OreEspEntity oreEspEntity = RDEntityTypes.ORE_ESP.asHolder().value().create(world, EntitySpawnReason.EVENT);
                    if (oreEspEntity != null) {
                        ((BlockDisplayAccessor) oreEspEntity).reverie_dreams$setBlockState(world.getBlockState(closestOrePos));
                        oreEspEntity.setPos(new Vec3(closestOrePos));
                        oreEspEntity.setGlowingTag(true);
                        world.addFreshEntity(oreEspEntity);
                    }

                    world.playSound(null, player.getX(), player.getEyeY(), player.getZ(),
                            SoundEvents.NOTE_BLOCK_PLING.value(),
                            SoundSource.PLAYERS, 1.0f, 1.0f);
                } else {
                    player.sendSystemMessage(Component.translatable("message.treasure_hunting_rod.not_found"), false);
                }

                // 伤害和冷却
                if (!player.hasInfiniteMaterials()) {
                    stack.hurtWithoutBreaking(1, player);
                }

                cooldown.addCooldown(stack, 35); // 设置冷却
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        return super.use(world, user, hand);
    }


    public static boolean isOre(BlockState blockState) {
        for (TagKey<Block> tag : ORE_BLOCK_TAGS) {
            if (blockState.is(tag)) {
                return true;
            }
        }
        return false;
    }

}
