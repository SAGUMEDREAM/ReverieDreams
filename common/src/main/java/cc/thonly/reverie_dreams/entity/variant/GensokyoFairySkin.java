package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.Yousei;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.minecraft.resources.Identifier;

public enum GensokyoFairySkin {
    BLUE("yousei_01", "gensokyo_fairy"),
    ORANGE("yousei_02", "gensokyo_fairy_yousei_02"),
    GREEN("yousei_03", "gensokyo_fairy_yousei_03"),
    SUNFLOWER("sunflower_yousei", "gensokyo_fairy_sunflower_yousei");

    public static final Identifier FORMAL_BLUE_MODEL_ID = ReverieDreams.id("gensokyo_fairy_blue");
    public static final Identifier FORMAL_BLUE_TEXTURE_ID = ReverieDreams.id("textures/entity/gensokyo_fairy_blue.png");

    private final Identifier skinId;
    private final Identifier modelId;
    private final Identifier textureId;

    GensokyoFairySkin(String skin, String model) {
        this.skinId = ReverieDreams.id(skin);
        this.modelId = ReverieDreams.id(model);
        this.textureId = ReverieDreams.id("textures/entity/" + model + ".png");
    }

    public Identifier modelId() {
        return this.modelId;
    }

    public Identifier textureId() {
        return this.textureId;
    }

    public static boolean usesFormalBlueModel(boolean ordinaryYousei, Identifier skinId) {
        return ordinaryYousei && BLUE.skinId.equals(skinId);
    }

    public static boolean usesFormalBlueModel(BaseNPCLikeEntity entity) {
        return usesFormalBlueModel(entity instanceof Yousei, entity.getSkinType().getId());
    }

    public static GensokyoFairySkin of(Identifier skinId) {
        for (GensokyoFairySkin skin : values()) {
            if (skin.skinId.equals(skinId)) {
                return skin;
            }
        }
        return BLUE;
    }
}
