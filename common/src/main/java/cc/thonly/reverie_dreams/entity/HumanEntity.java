package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.world.settlement.SettlementDutyGoal;
import cc.thonly.reverie_dreams.world.settlement.SettlementGroundNavigation;
import cc.thonly.reverie_dreams.world.settlement.SettlementPilotData;
import cc.thonly.reverie_dreams.world.settlement.SettlementTradeSession;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class HumanEntity extends BaseNPCLikeEntity {
    private final SettlementTradeSession settlementTradeSession = new SettlementTradeSession();

    public SettlementTradeSession settlementTradeSession() {
        return this.settlementTradeSession;
    }
    public HumanEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level, MobSkinTypes.DEFAULT);
    }

    @Override
    public void init() {
        super.init();
        var scale = this.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.setBaseValue(1.0);
        }
    }

    @Override
    public Boolean canPickItem() {
        return false;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new SettlementGroundNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(SettlementDutyGoal.PRIORITY, new SettlementDutyGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) this.settlementTradeSession.tick(this);
        SettlementDutyGoal.tickSleepingResident(this);
        super.tick();
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.dead) {
            this.settlementTradeSession.close();
            SettlementPilotData.unassign(this);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && !ItemUtils.shouldPass(player, hand)
                && player instanceof ServerPlayer customer && this.settlementTradeSession.open(this, customer)) {
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.mobInteract(player, hand);
    }
}
