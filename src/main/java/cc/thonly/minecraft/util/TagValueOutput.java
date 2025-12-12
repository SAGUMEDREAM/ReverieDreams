package cc.thonly.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class TagValueOutput implements ValueOutput {
    private final ProblemReporter problemReporter;
    private final DynamicOps<Tag> ops;
    private final CompoundTag output;

    public TagValueOutput(ProblemReporter problemReporter, DynamicOps<Tag> dynamicOps, CompoundTag compoundTag) {
        this.problemReporter = problemReporter;
        this.ops = dynamicOps;
        this.output = compoundTag;
    }

    public static TagValueOutput createWithContext(ProblemReporter problemReporter, HolderLookup.Provider provider) {
        return new TagValueOutput(problemReporter, provider.createSerializationContext(NbtOps.INSTANCE), new CompoundTag());
    }

    public static TagValueOutput createWithoutContext(ProblemReporter problemReporter) {
        return new TagValueOutput(problemReporter, NbtOps.INSTANCE, new CompoundTag());
    }

    public <T> void store(String string, Codec<T> codec, T object) {
        DataResult<Tag> var10000 = codec.encodeStart(this.ops, object);
        switch (var10000) {
            case DataResult.Success<Tag> success:
                this.output.put(string, (Tag)success.value());
                break;
            case DataResult.Error<Tag> error:
                this.problemReporter.report(error.message());
                error.partialValue().ifPresent((tag) -> {
                    this.output.put(string, tag);
                });
                break;
        }

    }

    public <T> void storeNullable(String string, Codec<T> codec, @Nullable T object) {
        if (object != null) {
            this.store(string, codec, object);
        }

    }

    public <T> void store(MapCodec<T> mapCodec, T object) {
        DataResult<Tag> var10000 = mapCodec.encoder().encodeStart(this.ops, object);
        switch (var10000) {
            case DataResult.Success<Tag> success:
                this.output.merge((CompoundTag)success.value());
                break;
            case DataResult.Error<Tag> error:
                this.problemReporter.report(error.message());
                error.partialValue().ifPresent((tag) -> {
                    this.output.merge((CompoundTag)tag);
                });
                break;
        }

    }

    public void putBoolean(String string, boolean bl) {
        this.output.putBoolean(string, bl);
    }

    public void putByte(String string, byte b) {
        this.output.putByte(string, b);
    }

    public void putShort(String string, short s) {
        this.output.putShort(string, s);
    }

    public void putInt(String string, int i) {
        this.output.putInt(string, i);
    }

    public void putLong(String string, long l) {
        this.output.putLong(string, l);
    }

    public void putFloat(String string, float f) {
        this.output.putFloat(string, f);
    }

    public void putDouble(String string, double d) {
        this.output.putDouble(string, d);
    }

    public void putString(String string, String string2) {
        this.output.putString(string, string2);
    }

    public void putIntArray(String string, int[] is) {
        this.output.putIntArray(string, is);
    }

    private ProblemReporter reporterForChild(String string) {
        return this.problemReporter.forChild(string);
    }

    public ValueOutput child(String string) {
        CompoundTag compoundTag = new CompoundTag();
        this.output.put(string, compoundTag);
        return new TagValueOutput(this.reporterForChild(string), this.ops, compoundTag);
    }

    public ValueOutput.ValueOutputList childrenList(String string) {
        ListTag listTag = new ListTag();
        this.output.put(string, listTag);
        return new ListWrapper(string, this.problemReporter, this.ops, listTag);
    }

    public <T> ValueOutput.TypedOutputList<T> list(String string, Codec<T> codec) {
        ListTag listTag = new ListTag();
        this.output.put(string, listTag);
        return new TypedListWrapper<>(this.problemReporter, string, this.ops, codec, listTag);
    }

    public void discard(String string) {
        this.output.remove(string);
    }

    public boolean isEmpty() {
        return this.output.isEmpty();
    }

    public CompoundTag buildResult() {
        return this.output;
    }

    static class ListWrapper implements ValueOutput.ValueOutputList {
        private final String fieldName;
        private final ProblemReporter problemReporter;
        private final DynamicOps<Tag> ops;
        private final ListTag output;

        ListWrapper(String string, ProblemReporter problemReporter, DynamicOps<Tag> dynamicOps, ListTag listTag) {
            this.fieldName = string;
            this.problemReporter = problemReporter;
            this.ops = dynamicOps;
            this.output = listTag;
        }

        public ValueOutput addChild() {
            int i = this.output.size();
            CompoundTag compoundTag = new CompoundTag();
            this.output.add(compoundTag);
            return new TagValueOutput(this.problemReporter.forChild(this.fieldName), this.ops, compoundTag);
        }

        public void discardLast() {
            this.output.removeLast();
        }

        public boolean isEmpty() {
            return this.output.isEmpty();
        }
    }

    static class TypedListWrapper<T> implements ValueOutput.TypedOutputList<T> {
        private final ProblemReporter problemReporter;
        private final String name;
        private final DynamicOps<Tag> ops;
        private final Codec<T> codec;
        private final ListTag output;

        TypedListWrapper(ProblemReporter problemReporter, String string, DynamicOps<Tag> dynamicOps, Codec<T> codec, ListTag listTag) {
            this.problemReporter = problemReporter;
            this.name = string;
            this.ops = dynamicOps;
            this.codec = codec;
            this.output = listTag;
        }

        public void add(T object) {
            DataResult<Tag> var10000 = this.codec.encodeStart(this.ops, object);
            switch (var10000) {
                case DataResult.Success<Tag> success:
                    this.output.add((Tag)success.value());
                    break;
                case DataResult.Error<Tag> error:
                    this.problemReporter.report(error.message());
                    Optional<Tag> var6 = error.partialValue();
                    ListTag var10001 = this.output;
                    Objects.requireNonNull(var10001);
                    var6.ifPresent(var10001::add);
                    break;
            }

        }

        public boolean isEmpty() {
            return this.output.isEmpty();
        }
    }
}
