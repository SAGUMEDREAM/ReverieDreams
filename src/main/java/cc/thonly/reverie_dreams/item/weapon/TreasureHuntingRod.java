package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.entity.misc.OreEspEntity;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class TreasureHuntingRod extends SwordItem {
    public static final List<TagKey<Block>> ORE_BLOCK_TAGS = new ArrayList<>();
    public static final ToolMaterial MATERIAL = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 300, 4.0f, 4.5f, 5, ItemTags.DIAMOND_TOOL_MATERIALS);

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

    public TreasureHuntingRod(float attackDamage, float attackSpeed, Settings settings) {
        super(MATERIAL, attackDamage, attackSpeed, settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            ServerPlayerEntity player = (ServerPlayerEntity) user;
            ItemStack stack = player.getStackInHand(hand);
            ItemCooldownManager cooldown = player.getItemCooldownManager();

            if (player.isSneaking()) {
                player.swingHand(hand);

                BlockPos origin = player.getBlockPos();
                int radius = 8;

                // 记录最近矿物数据
                double minDistance = Double.MAX_VALUE;
                BlockPos closestOrePos = null;
                Block closestOreBlock = null;

                for (BlockPos pos : BlockPos.iterate(
                        origin.add(-radius, -radius, -radius),
                        origin.add(radius, radius, radius))) {

                    if (!world.isInBuildLimit(pos)) continue;

                    BlockState state = world.getBlockState(pos);
                    if (isOre(state)) {
                        int dx = pos.getX() - origin.getX();
                        int dy = pos.getY() - origin.getY();
                        int dz = pos.getZ() - origin.getZ();
                        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                        if (distance < minDistance) {
                            minDistance = distance;
                            closestOrePos = pos.toImmutable();
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

                    MutableText message = Text.translatable(
                            "message.treasure_hunting_rod.find", roundedDistance, dx, dy, dz
                    ).append(" ").append(Text.translatable(closestOreBlock.getTranslationKey()));

//                    entity.sendMessage(message, false);
                    OreEspEntity oreEspEntity = ModEntities.ORE_ESP_ENTITY_TYPE.create(world, SpawnReason.EVENT);
                    if (oreEspEntity != null) {
                        oreEspEntity.setBlockState(world.getBlockState(closestOrePos));
                        oreEspEntity.setPosition(new Vec3d(closestOrePos));
                        oreEspEntity.setGlowing(true);
                        world.spawnEntity(oreEspEntity);
                    }

                    world.playSound(null, player.getX(), player.getEyeY(), player.getZ(),
                            SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                            SoundCategory.PLAYERS, 1.0f, 1.0f);
                } else {
                    player.sendMessage(Text.translatable("message.treasure_hunting_rod.not_found"), false);
                }

                // 伤害和冷却
                if (!player.isInCreativeMode()) {
                    stack.damage(1, player);
                }

                cooldown.set(stack, 35); // 设置冷却
                return ActionResult.SUCCESS_SERVER;
            }
        }

        return super.use(world, user, hand);
    }


    public static boolean isOre(BlockState blockState) {
        for (TagKey<Block> tag : ORE_BLOCK_TAGS) {
            if (blockState.isIn(tag)) {
                return true;
            }
        }
        return false;
    }

}
