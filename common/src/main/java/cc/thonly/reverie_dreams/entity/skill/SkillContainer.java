package cc.thonly.reverie_dreams.entity.skill;

import cc.thonly.reverie_dreams.util.math.ModMth;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@SuppressWarnings("LombokGetterMayBeUsed")
public class SkillContainer<T extends LivingEntity> {
    @Getter
    private final T livingEntity;
    private final RandomSource randomSource;
    private final List<Skill<T>> skills;
    private final Predicate<T> predicate;
    @Getter
    private final Map<String, Object> dataMap = new Object2ObjectOpenHashMap<>(8);

    @Setter
    private Skill<T> prev;

    private Skill<T> now;
    private boolean nextTrigger;
    private int count;

    @SafeVarargs
    public SkillContainer(
            T livingEntity,
            Predicate<T> predicate,
            Skill<T>... skills
    ) {
        this.livingEntity = livingEntity;
        this.randomSource = livingEntity.getRandom();
        this.predicate = predicate;
        this.skills = new ArrayList<>(Arrays.asList(skills));
    }

    public SkillContainer(
            T livingEntity,
            Predicate<T> predicate,
            List<Skill<T>> skills
    ) {
        this.livingEntity = livingEntity;
        this.randomSource = livingEntity.getRandom();
        this.predicate = predicate;
        this.skills = new ArrayList<>(skills);
    }

    public void tick() {
        if (!this.canStart() || this.skills.isEmpty()) {
            return;
        }

        this.count++;
        if (this.count >= 20) {
            this.count = 0;
        }

        if (this.now == null) {
            this.next();
            return;
        }

        if (this.nextTrigger) {
            this.next();
            return;
        }

        this.now.onTick(this);
    }

    public boolean canStart() {
        return this.predicate == null
                || this.predicate.test(this.livingEntity);
    }

    public Skill<T> next() {
        Skill<T> old = this.now;

        if (old != null) {
            this.dataMap.clear();
            old.onEnd(this);
        }

        this.prev = old;

        Skill<T> next = this.selectNextSkill(old);
        this.now = next;
        this.nextTrigger = false;

        if (next != null) {
            next.onStarted(this);
        }

        return old;
    }

    private Skill<T> selectNextSkill(Skill<T> old) {
        if (this.skills.isEmpty()) {
            return null;
        }

        if (this.skills.size() == 1) {
            return this.skills.getFirst();
        }

        List<Skill<T>> candidates = new ArrayList<>(this.skills);

        if (old != null) {
            candidates.remove(old);
        }

        if (candidates.isEmpty()) {
            candidates = this.skills;
        }

        return ModMth.getRandomElement(this.randomSource, candidates);
    }

    public void triggerNext() {
        this.nextTrigger = true;
    }

    public boolean isNextTriggered() {
        return this.nextTrigger;
    }

    public void stop() {
        if (this.now != null) {
            this.now.onEnd(this);
        }

        this.prev = this.now;
        this.now = null;
        this.nextTrigger = false;
        this.count = 0;
    }

    public Skill<T> now() {
        return this.now;
    }

    public Skill<T> previous() {
        return this.prev;
    }

    public int getCount() {
        return this.count;
    }

    public void resetCount() {
        this.count = 0;
    }

    public List<Skill<T>> getSkills() {
        return List.copyOf(this.skills);
    }

    public void addSkill(Skill<T> skill) {
        if (skill == null) {
            return;
        }

        this.skills.add(skill);
    }

    public void removeSkill(Skill<T> skill) {
        this.skills.remove(skill);

        if (this.now == skill) {
            this.stop();
        }
    }

    public void addAdditionalSaveData(ValueOutput view) {
        view.storeNullable("Skill.Id", Skill.CODEC, this.now);
        view.putBoolean("Skill.NextTrigger", this.nextTrigger);
        view.putInt("Skill.Count", this.count);
    }

    @SuppressWarnings("unchecked")
    public void readAdditionalSaveData(ValueInput view) {
        view.read("Skill.Id", Skill.CODEC).ifPresent(skill -> this.now = (Skill<T>) skill);
        this.nextTrigger = view.getBooleanOr("Skill.NextTrigger", false);
        this.count = view.getIntOr("Skill.Count", 0);
    }
}