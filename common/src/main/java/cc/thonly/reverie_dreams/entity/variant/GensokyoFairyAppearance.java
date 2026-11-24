package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.MaidYousei;
import cc.thonly.reverie_dreams.entity.SunflowerYousei;
import cc.thonly.reverie_dreams.entity.Yousei;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public enum GensokyoFairyAppearance {
    YOUSEI_BLUE("gensokyo_fairy_blue"),
    YOUSEI_ORANGE("gensokyo_fairy_orange"),
    YOUSEI_GREEN("gensokyo_fairy_green"),
    MAID_BLUE("gensokyo_fairy_maid_blue"),
    MAID_ORANGE("gensokyo_fairy_maid_orange"),
    MAID_GREEN("gensokyo_fairy_maid_green"),
    SUNFLOWER("gensokyo_fairy_sunflower");

    public enum Kind { YOUSEI, MAID, SUNFLOWER }

    private final Identifier modelId;
    private final Identifier resourceId;
    private final Identifier textureId;

    GensokyoFairyAppearance(String name) {
        this.modelId = ReverieDreams.id(name);
        this.resourceId = ReverieDreams.id("entity/" + name);
        this.textureId = ReverieDreams.id("textures/entity/" + name + ".png");
    }

    public Identifier modelId() { return this.modelId; }
    public Identifier resourceId() { return this.resourceId; }
    public Identifier textureId() { return this.textureId; }

    public static @Nullable GensokyoFairyAppearance of(Kind kind, @Nullable Identifier skinId) {
        if (skinId == null || !skinId.getNamespace().equals(ReverieDreams.MOD_ID)) {
            return null;
        }
        if (kind == Kind.SUNFLOWER) {
            return skinId.getPath().equals("sunflower_yousei") ? SUNFLOWER : null;
        }
        return switch (skinId.getPath()) {
            case "yousei_01" -> kind == Kind.MAID ? MAID_BLUE : YOUSEI_BLUE;
            case "yousei_02" -> kind == Kind.MAID ? MAID_ORANGE : YOUSEI_ORANGE;
            case "yousei_03" -> kind == Kind.MAID ? MAID_GREEN : YOUSEI_GREEN;
            default -> null;
        };
    }

    public static @Nullable GensokyoFairyAppearance of(BaseNPCLikeEntity entity) {
        Kind kind = entity instanceof MaidYousei ? Kind.MAID
                : entity instanceof SunflowerYousei ? Kind.SUNFLOWER
                : entity instanceof Yousei ? Kind.YOUSEI : null;
        return kind == null ? null : of(kind, entity.getSkinType().getId());
    }
}
