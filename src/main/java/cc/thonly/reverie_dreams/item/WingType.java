package cc.thonly.reverie_dreams.item;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum WingType implements StringRepresentable {
    NONE("none"),
    FLAN("flan"),
    ;
    public static final Codec<WingType> CODEC = StringRepresentable.fromEnum(WingType::values);
    public final String name;

    WingType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
