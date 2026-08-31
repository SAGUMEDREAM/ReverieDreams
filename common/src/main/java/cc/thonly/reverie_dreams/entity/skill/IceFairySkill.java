package cc.thonly.reverie_dreams.entity.skill;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.IceFairy;
import net.minecraft.resources.Identifier;

public class IceFairySkill extends Skill<IceFairy> {
    public static final Identifier KEY = ReverieDreams.id("ice_fairy");

    @Override
    public void onStarted(SkillContainer<IceFairy> container) {

    }

    @Override
    public void onTick(SkillContainer<IceFairy> container) {

    }

    @Override
    public void onEnd(SkillContainer<IceFairy> container) {

    }

    @Override
    public Identifier id() {
        return KEY;
    }
}
