package cc.thonly.reverie_dreams.entity.npc.container;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Map;
import java.util.UUID;

public class FavorabilityContainer {
    public static final Identifier KEY = ReverieDreams.id("favorability");
    public static final Double MIN_VALUE = 0.0;
    public static final Double MAX_VALUE = 200.0;
    public static final Double DEFAULT_VALUE = 50.0;

    @Getter
    private final Map<UUID, Double> data = new Object2ObjectLinkedOpenHashMap<>();

    final NPCSimpleEntity npc;
    final RandomSource randomSource;

    public FavorabilityContainer(
            NPCSimpleEntity npc,
            RandomSource randomSource
    ) {
        this.npc = npc;
        this.randomSource = randomSource.fork();
    }

    public void tick() {

    }

    public double normalizedValue(double number) {
        return Math.min(
                MAX_VALUE,
                Math.max(number, MIN_VALUE)
        );
    }

    public double get(UUID uuid) {
        return this.data.getOrDefault(uuid, DEFAULT_VALUE);
    }

    public void add(UUID uuid, double number) {
        double previous = this.data.computeIfAbsent(uuid, _ -> DEFAULT_VALUE);
        this.data.put(uuid, this.normalizedValue(previous + number));
    }

    public void set(UUID uuid, double number) {
        this.data.put(uuid, this.normalizedValue(number));
    }

    public boolean contains(UUID uuid) {
        return this.data.containsKey(uuid);
    }

    public void remove(UUID uuid) {
        this.data.remove(uuid);
    }

    public void clear() {
        this.data.clear();
    }

    public Identifier getId() {
        return KEY;
    }

    public void readAdditionalSaveData(
            ValueInput view
    ) {
        this.data.clear();

        view.listOrEmpty(
                "Favorability",
                FavorabilityEntry.CODEC
        ).forEach(entry -> {
            if (entry.uuid() == null) {
                return;
            }

            double value = this.normalizedValue(entry.value());

            this.data.put(entry.uuid(), value);
        });
    }

    public void addAdditionalSaveData(
            ValueOutput view
    ) {
        view.store(
                "Favorability",
                FavorabilityEntry.CODEC.listOf(),
                this.data.entrySet()
                         .stream()
                         .map(entry -> new FavorabilityEntry(entry.getKey(), this.normalizedValue(entry.getValue())))
                         .toList()
        );
    }

    public record FavorabilityEntry(
            UUID uuid,
            double value
    ) {
        public static final Codec<FavorabilityEntry> CODEC =
                RecordCodecBuilder.create(
                        instance -> instance.group(
                                Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("uuid").forGetter(FavorabilityEntry::uuid),
                                Codec.DOUBLE.fieldOf("value").forGetter(FavorabilityEntry::value)
                        ).apply(instance, FavorabilityEntry::new)
                );
    }
}