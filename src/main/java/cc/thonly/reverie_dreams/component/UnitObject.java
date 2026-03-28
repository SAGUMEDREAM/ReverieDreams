package cc.thonly.reverie_dreams.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UnitObject {
    public static final Codec<UnitObject> CODEC = RecordCodecBuilder.create(x -> x.group(
            Codec.BYTE.optionalFieldOf("field", (byte) 0).forGetter(UnitObject::getField)
    ).apply(x, UnitObject::new));
    private byte field;

    private byte getField() {
        return this.field;
    }
}
