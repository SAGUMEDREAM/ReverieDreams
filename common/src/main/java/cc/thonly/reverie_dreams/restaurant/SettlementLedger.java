package cc.thonly.reverie_dreams.restaurant;

import cc.thonly.reverie_dreams.server.dialog.NPCSpeech;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerFactory;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Bounded pending settlements carried by the serving NPC until the beneficiary claims them. */
public final class SettlementLedger {
    public static final int LIMIT = 256;

    private final List<Settlement> pending = new ArrayList<>();

    public void add(Settlement settlement) {
        if (pending.size() < LIMIT) {
            pending.add(settlement);
            return;
        }
        for (int i = 0; i < pending.size(); i++) {
            Settlement old = pending.get(i);
            if (!old.beneficiary().equals(settlement.beneficiary())) {
                continue;
            }
            pending.set(i, new Settlement(old.beneficiary(), old.outcome(),
                    old.coins() + settlement.coins(), old.experience() + settlement.experience(),
                    old.feedback().isPresent() ? old.feedback() : settlement.feedback()));
            return;
        }
        pending.removeFirst();
        pending.add(settlement);
    }

    public boolean hasPending(UUID player) {
        for (Settlement settlement : pending) {
            if (settlement.belongsTo(player)) {
                return true;
            }
        }
        return false;
    }

    public Optional<Claim> claim(ServerPlayer player, Entity speaker) {
        List<Settlement> owned = new ArrayList<>();
        for (Iterator<Settlement> iterator = pending.iterator(); iterator.hasNext(); ) {
            Settlement settlement = iterator.next();
            if (settlement.belongsTo(player.getUUID())) {
                owned.add(settlement);
                iterator.remove();
            }
        }
        if (owned.isEmpty()) {
            return Optional.empty();
        }
        int coins = 0;
        int experience = 0;
        boolean angry = false;
        boolean expensive = false;
        for (Settlement settlement : owned) {
            coins += settlement.coins();
            experience += settlement.experience();
            angry |= settlement.outcome() == Settlement.Outcome.ANGRY;
            expensive |= settlement.outcome() == Settlement.Outcome.TOO_EXPENSIVE;
        }
        Settlement.Outcome outcome = angry ? Settlement.Outcome.ANGRY
                : expensive ? Settlement.Outcome.TOO_EXPENSIVE : Settlement.Outcome.SUCCESS;
        SettlementRewards.payCoins(speaker, player, coins);
        if (experience > 0) {
            player.giveExperiencePoints(experience);
        }
        SimpleTriggerFactory.create(SimpleTriggerKeys.WAITER).trigger(player);
        SettlementRewards.playOutcome(player, outcome);
        NPCSpeech.sayTo(speaker, player, Component.translatable(
                "gui.reverie_dreams.settlement.claimed", owned.size(), coins, experience));
        return Optional.of(new Claim(owned.size(), coins, experience, outcome));
    }

    public void read(ValueInput view) {
        pending.clear();
        for (Settlement settlement : view.listOrEmpty("Settlements", Settlement.CODEC)) {
            if (pending.size() >= LIMIT) {
                break;
            }
            pending.add(settlement);
        }
    }

    public void write(ValueOutput view) {
        ValueOutput.TypedOutputList<Settlement> list = view.list("Settlements", Settlement.CODEC);
        for (Settlement settlement : pending) {
            list.add(settlement);
        }
    }

    public record Claim(int orders, int coins, int experience, Settlement.Outcome outcome) {
    }
}
