package cc.thonly.reverie_dreams.entity.npc;


import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FishingContainer {
    public static final Identifier KEY = ReverieDreams.id("fishing");
    final NPCSimpleEntity npc;
    final RandomSource randomSource;

    public FishingContainer(NPCSimpleEntity npc, RandomSource randomSource) {
        this.npc = npc;
        this.randomSource = randomSource.fork();
    }

    public Identifier getId() {
        return KEY;
    }

    public void readAdditionalSaveData(ValueInput view) {

    }

    public void addAdditionalSaveData(ValueOutput view) {

    }
}
