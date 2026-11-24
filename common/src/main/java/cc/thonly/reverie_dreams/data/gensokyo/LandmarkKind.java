package cc.thonly.reverie_dreams.data.gensokyo;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Classification of a Gensokyo landmark. The icon is a vanilla item so the
 * dialog list renders identically on vanilla and modded clients.
 */
public enum LandmarkKind implements StringRepresentable {
    REALM_REGION("realm_region", Items.ENDER_EYE),
    WATER_REGION("water_region", Items.WATER_BUCKET),
    REGION("region", Items.GRASS_BLOCK),
    TERRAIN("terrain", Items.STONE),
    SITE("site", Items.BELL),
    WATERFALL("waterfall", Items.PRISMARINE_SHARD),
    UNCERTAIN_LABEL("uncertain_label", Items.MAP),
    ENTRANCE("entrance", Items.SPRUCE_DOOR),
    WATER_SITE("water_site", Items.OAK_BOAT),
    SUBTERRANEAN("subterranean", Items.DEEPSLATE),
    REALM_PORTAL("realm_portal", Items.END_PORTAL_FRAME),
    ROUTE_REGION("route_region", Items.RAIL),
    HEIGHT_UNKNOWN_SITE("height_unknown_site", Items.SCAFFOLDING),
    SITE_LABEL("site_label", Items.OAK_SIGN),
    AERIAL("aerial", Items.PHANTOM_MEMBRANE);

    public static final Codec<LandmarkKind> CODEC = StringRepresentable.fromEnum(LandmarkKind::values);

    private final String name;
    private final Item icon;

    LandmarkKind(String name, Item icon) {
        this.name = name;
        this.icon = icon;
    }

    public Item icon() {
        return this.icon;
    }

    public Component displayName() {
        return Component.translatable("landmark_kind.reverie_dreams." + this.name);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
