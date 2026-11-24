package cc.thonly.reverie_dreams.data.gensokyo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public record GensokyoAdventure(
        int version,
        String template,
        BlockPos origin,
        List<Treasure> treasures,
        List<Encounter> encounters,
        List<Vec3> armorStands
) {
    public GensokyoAdventure {
        treasures = List.copyOf(treasures);
        encounters = List.copyOf(encounters);
        armorStands = List.copyOf(armorStands);
        if (treasures.stream().map(Treasure::id).distinct().count() != treasures.size()
                || encounters.stream().map(Encounter::id).distinct().count() != encounters.size()) {
            throw new IllegalArgumentException("Duplicate adventure point ID");
        }
        for (Treasure treasure : treasures) {
            if (!List.of("supply", "guard", "secret", "heart", "library").contains(treasure.tier())) {
                throw new IllegalArgumentException("Unknown treasure tier " + treasure.tier());
            }
        }
        for (Encounter encounter : encounters) {
            if (encounter.bounds().size() != 6 || !List.of("maid", "goblin").contains(encounter.kind())) {
                throw new IllegalArgumentException("Invalid encounter " + encounter.id());
            }
        }
    }

    public static final Codec<GensokyoAdventure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 1).fieldOf("version").forGetter(GensokyoAdventure::version),
            Codec.STRING.fieldOf("template").forGetter(GensokyoAdventure::template),
            BlockPos.CODEC.fieldOf("origin").forGetter(GensokyoAdventure::origin),
            Treasure.CODEC.listOf().fieldOf("treasures").forGetter(GensokyoAdventure::treasures),
            Encounter.CODEC.listOf().fieldOf("encounters").forGetter(GensokyoAdventure::encounters),
            Vec3.CODEC.listOf().fieldOf("armor_stands").forGetter(GensokyoAdventure::armorStands)
    ).apply(instance, GensokyoAdventure::new));

    public record Treasure(
            String id,
            String tier,
            BlockPos pos,
            BlockPos approach,
            String block,
            boolean newChest
    ) {
        public static final Codec<Treasure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Treasure::id),
                Codec.STRING.fieldOf("tier").forGetter(Treasure::tier),
                BlockPos.CODEC.fieldOf("pos").forGetter(Treasure::pos),
                BlockPos.CODEC.fieldOf("approach").forGetter(Treasure::approach),
                Codec.STRING.fieldOf("block").forGetter(Treasure::block),
                Codec.BOOL.optionalFieldOf("new", false).forGetter(Treasure::newChest)
        ).apply(instance, Treasure::new));
    }

    public record Barrier(
            BlockPos pos,
            String block
    ) {
        public static final Codec<Barrier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Barrier::pos),
                Codec.STRING.fieldOf("block").forGetter(Barrier::block)
        ).apply(instance, Barrier::new));
    }

    public record Encounter(
            String id,
            String kind,
            BlockPos pos,
            List<Integer> bounds,
            List<BlockPos> doors,
            int area,
            boolean friendly,
            List<Barrier> barriers
    ) {
        public static final Codec<Encounter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Encounter::id),
                Codec.STRING.fieldOf("kind").forGetter(Encounter::kind),
                BlockPos.CODEC.fieldOf("pos").forGetter(Encounter::pos),
                Codec.INT.listOf().fieldOf("bounds").forGetter(Encounter::bounds),
                BlockPos.CODEC.listOf().optionalFieldOf("doors", List.of()).forGetter(Encounter::doors),
                Codec.INT.optionalFieldOf("area", 0).forGetter(Encounter::area),
                Codec.BOOL.optionalFieldOf("friendly", false).forGetter(Encounter::friendly),
                Barrier.CODEC.listOf().optionalFieldOf("barriers", List.of()).forGetter(Encounter::barriers)
        ).apply(instance, Encounter::new));
    }

}
