package cc.thonly.minecraft.util.tvio;

import cc.thonly.minecraft.util.ValueInput;
import cc.thonly.minecraft.util.ValueInputContextHelper;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.util.ProblemReporter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class TagValueInput implements ValueInput {
    private final ProblemReporter problemReporter;
    private final ValueInputContextHelper context;
    private final CompoundTag input;

    public TagValueInput(ProblemReporter problemReporter, ValueInputContextHelper valueInputContextHelper, CompoundTag compoundTag) {
        this.problemReporter = problemReporter;
        this.context = valueInputContextHelper;
        this.input = compoundTag;
    }

    public static ValueInput create(ProblemReporter problemReporter, HolderLookup.Provider provider, CompoundTag compoundTag) {
        return new TagValueInput(problemReporter, new ValueInputContextHelper(provider, NbtOps.INSTANCE), compoundTag);
    }

    public static ValueInput.ValueInputList create(ProblemReporter problemReporter, HolderLookup.Provider provider, List<CompoundTag> list) {
        return new CompoundListWrapper(problemReporter, new ValueInputContextHelper(provider, NbtOps.INSTANCE), list);
    }

    public <T> Optional<T> read(String string, Codec<T> codec) {
        Tag tag = this.input.get(string);
        if (tag == null) {
            return Optional.empty();
        } else {
            DataResult<T> var10000 = codec.parse(this.context.ops(), tag);
            return switch (var10000) {
                case DataResult.Success<T> success -> Optional.of(success.value());
                case DataResult.Error<T> error -> {
                    this.problemReporter.report(error.message());
                    yield error.partialValue();
                }
            };
        }
    }

    public <T> Optional<T> read(MapCodec<T> mapCodec) {
        DynamicOps<Tag> dynamicOps = this.context.ops();
        DataResult<T> var10000 = dynamicOps.getMap(this.input).flatMap((mapLike) -> {
            return mapCodec.decode(dynamicOps, mapLike);
        });

        return switch (var10000) {
            case DataResult.Success<T> success -> Optional.of(success.value());
            case DataResult.Error<T> error -> {
                this.problemReporter.report(error.message());
                yield error.partialValue();
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T extends Tag> T getOptionalTypedTag(String string, TagType<T> tagType) {
        Tag tag = this.input.get(string);
        if (tag == null) {
            return null;
        } else {
            TagType<?> tagType2 = tag.getType();
            if (tagType2 != tagType) {
                this.problemReporter.report(string);
                return null;
            } else {
                return (T) tag;
            }
        }
    }

    @Nullable
    private NumericTag getNumericTag(String string) {
        Tag tag = this.input.get(string);
        if (tag == null) {
            return null;
        } else if (tag instanceof NumericTag) {
            return (NumericTag) tag;
        } else {
            this.problemReporter.report(string);
            return null;
        }
    }

    public Optional<ValueInput> child(String string) {
        CompoundTag compoundTag = (CompoundTag) this.getOptionalTypedTag(string, CompoundTag.TYPE);
        return compoundTag != null ? Optional.of(this.wrapChild(string, compoundTag)) : Optional.empty();
    }

    public ValueInput childOrEmpty(String string) {
        CompoundTag compoundTag = (CompoundTag) this.getOptionalTypedTag(string, CompoundTag.TYPE);
        return compoundTag != null ? this.wrapChild(string, compoundTag) : this.context.empty();
    }

    public Optional<ValueInput.ValueInputList> childrenList(String string) {
        ListTag listTag = (ListTag) this.getOptionalTypedTag(string, ListTag.TYPE);
        return listTag != null ? Optional.of(this.wrapList(string, this.context, listTag)) : Optional.empty();
    }

    public ValueInput.ValueInputList childrenListOrEmpty(String string) {
        ListTag listTag = (ListTag) this.getOptionalTypedTag(string, ListTag.TYPE);
        return listTag != null ? this.wrapList(string, this.context, listTag) : this.context.emptyList();
    }

    public <T> Optional<ValueInput.TypedInputList<T>> list(String string, Codec<T> codec) {
        ListTag listTag = (ListTag) this.getOptionalTypedTag(string, ListTag.TYPE);
        return listTag != null ? Optional.of(this.wrapTypedList(string, listTag, codec)) : Optional.empty();
    }

    public <T> ValueInput.TypedInputList<T> listOrEmpty(String string, Codec<T> codec) {
        ListTag listTag = (ListTag) this.getOptionalTypedTag(string, ListTag.TYPE);
        return listTag != null ? this.wrapTypedList(string, listTag, codec) : this.context.emptyTypedList();
    }

    public boolean getBooleanOr(String string, boolean bl) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? numericTag.getAsByte() != 0 : bl;
    }

    public byte getByteOr(String string, byte b) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? numericTag.getAsByte() : b;
    }

    public int getShortOr(String string, short s) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? numericTag.getAsByte() : s;
    }

    public Optional<Integer> getInt(String string) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? Optional.of(numericTag.getAsInt()) : Optional.empty();
    }

    public int getIntOr(String string, int i) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? numericTag.getAsInt() : i;
    }

    public long getLongOr(String string, long l) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? numericTag.getAsLong() : l;
    }

    public Optional<Long> getLong(String string) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? Optional.of(numericTag.getAsLong()) : Optional.empty();
    }

    public float getFloatOr(String string, float f) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? numericTag.getAsFloat() : f;
    }

    public double getDoubleOr(String string, double d) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? numericTag.getAsDouble() : d;
    }

    public Optional<String> getString(String string) {
        StringTag stringTag = (StringTag) this.getOptionalTypedTag(string, StringTag.TYPE);
        return stringTag != null ? Optional.of(stringTag.getAsString()) : Optional.empty();
    }

    public String getStringOr(String string, String string2) {
        StringTag stringTag = (StringTag) this.getOptionalTypedTag(string, StringTag.TYPE);
        return stringTag != null ? stringTag.getAsString() : string2;
    }

    public Optional<int[]> getIntArray(String string) {
        IntArrayTag intArrayTag = (IntArrayTag) this.getOptionalTypedTag(string, IntArrayTag.TYPE);
        return intArrayTag != null ? Optional.of(intArrayTag.getAsIntArray()) : Optional.empty();
    }

    public HolderLookup.Provider lookup() {
        return this.context.lookup();
    }

    private ValueInput wrapChild(String string, CompoundTag compoundTag) {
        return (ValueInput) (compoundTag.isEmpty() ? this.context.empty() : new TagValueInput(this.problemReporter.forChild(string), this.context, compoundTag));
    }

    static ValueInput wrapChild(ProblemReporter problemReporter, ValueInputContextHelper valueInputContextHelper, CompoundTag compoundTag) {
        return (ValueInput) (compoundTag.isEmpty() ? valueInputContextHelper.empty() : new TagValueInput(problemReporter, valueInputContextHelper, compoundTag));
    }

    private ValueInput.ValueInputList wrapList(String string, ValueInputContextHelper valueInputContextHelper, ListTag listTag) {
        return (ValueInput.ValueInputList) (listTag.isEmpty() ? valueInputContextHelper.emptyList() : new ListWrapper(this.problemReporter, string, valueInputContextHelper, listTag));
    }

    private <T> ValueInput.TypedInputList<T> wrapTypedList(String string, ListTag listTag, Codec<T> codec) {
        return (listTag.isEmpty() ? this.context.emptyTypedList() : new TypedListWrapper<>(this.problemReporter, string, this.context, codec, listTag));
    }

    private static class CompoundListWrapper implements ValueInput.ValueInputList {
        private final ProblemReporter problemReporter;
        private final ValueInputContextHelper context;
        private final List<CompoundTag> list;

        public CompoundListWrapper(ProblemReporter problemReporter, ValueInputContextHelper valueInputContextHelper, List<CompoundTag> list) {
            this.problemReporter = problemReporter;
            this.context = valueInputContextHelper;
            this.list = list;
        }

        ValueInput wrapChild(int i, CompoundTag compoundTag) {
            return TagValueInput.wrapChild(this.problemReporter.forChild(String.valueOf(i)), this.context, compoundTag);
        }

        public boolean isEmpty() {
            return this.list.isEmpty();
        }

        public Stream<ValueInput> stream() {
            return Streams.mapWithIndex(this.list.stream(), (compoundTag, l) -> {
                return this.wrapChild((int) l, compoundTag);
            });
        }

        public Iterator<ValueInput> iterator() {
            final ListIterator<CompoundTag> listIterator = this.list.listIterator();
            return new AbstractIterator<ValueInput>() {
                @Nullable
                protected ValueInput computeNext() {
                    if (listIterator.hasNext()) {
                        int i = listIterator.nextIndex();
                        CompoundTag compoundTag = (CompoundTag) listIterator.next();
                        return CompoundListWrapper.this.wrapChild(i, compoundTag);
                    } else {
                        return (ValueInput) this.endOfData();
                    }
                }
            };
        }
    }

    static class ListWrapper implements ValueInput.ValueInputList {
        private final ProblemReporter problemReporter;
        private final String name;
        final ValueInputContextHelper context;
        private final ListTag list;

        ListWrapper(ProblemReporter problemReporter, String string, ValueInputContextHelper valueInputContextHelper, ListTag listTag) {
            this.problemReporter = problemReporter;
            this.name = string;
            this.context = valueInputContextHelper;
            this.list = listTag;
        }

        public boolean isEmpty() {
            return this.list.isEmpty();
        }

        ProblemReporter reporterForChild(int i) {
            return this.problemReporter.forChild(this.name);
        }

        void reportIndexUnwrapProblem(int i, Tag tag) {
            this.problemReporter.report(this.name + " ErrorType:" + tag.getType());
        }

        public Stream<ValueInput> stream() {
            return Streams.mapWithIndex(this.list.stream(), (tag, l) -> {
                if (tag instanceof CompoundTag compoundTag) {
                    return TagValueInput.wrapChild(this.reporterForChild((int) l), this.context, compoundTag);
                } else {
                    this.reportIndexUnwrapProblem((int) l, tag);
                    return null;
                }
            }).filter(Objects::nonNull);
        }

        public Iterator<ValueInput> iterator() {
            final Iterator<Tag> iterator = this.list.iterator();
            return new AbstractIterator<ValueInput>() {
                private int index;

                @Nullable
                protected ValueInput computeNext() {
                    while (iterator.hasNext()) {
                        Tag tag = (Tag) iterator.next();
                        int i = this.index++;
                        if (tag instanceof CompoundTag compoundTag) {
                            return TagValueInput.wrapChild(ListWrapper.this.reporterForChild(i), ListWrapper.this.context, compoundTag);
                        }

                        ListWrapper.this.reportIndexUnwrapProblem(i, tag);
                    }

                    return (ValueInput) this.endOfData();
                }
            };
        }
    }

    private static class TypedListWrapper<T> implements ValueInput.TypedInputList<T> {
        private final ProblemReporter problemReporter;
        private final String name;
        final ValueInputContextHelper context;
        final Codec<T> codec;
        private final ListTag list;

        TypedListWrapper(ProblemReporter problemReporter, String string, ValueInputContextHelper valueInputContextHelper, Codec<T> codec, ListTag listTag) {
            this.problemReporter = problemReporter;
            this.name = string;
            this.context = valueInputContextHelper;
            this.codec = codec;
            this.list = listTag;
        }

        public boolean isEmpty() {
            return this.list.isEmpty();
        }

        void reportIndexUnwrapProblem(int i, Tag tag, DataResult.Error<?> error) {
            this.problemReporter.report(error.message());
        }

        @SuppressWarnings("unchecked")
        public Stream<T> stream() {
            return (Stream<T>) Streams.mapWithIndex(this.list.stream(), (tag, l) -> {
                DataResult<T> var10000 = this.codec.parse(this.context.ops(), tag);
                return switch (var10000) {
                    case DataResult.Success<T> success -> (Object) success.value();
                    case DataResult.Error<T> error -> {
                        this.reportIndexUnwrapProblem((int) l, tag, error);
                        yield error.partialValue().orElse((null));
                    }
                };
            }).filter(Objects::nonNull);
        }

        public Iterator<T> iterator() {
            final ListIterator<Tag> listIterator = this.list.listIterator();
            return new AbstractIterator<T>() {
                @Nullable
                protected T computeNext() {
                    while (true) {
                        if (listIterator.hasNext()) {
                            int i = listIterator.nextIndex();
                            Tag tag = (Tag) listIterator.next();
                            DataResult<T> var10000 = TypedListWrapper.this.codec.parse(TypedListWrapper.this.context.ops(), tag);
                            switch (var10000) {
                                case DataResult.Success<T> success:
                                    return success.value();
                                case DataResult.Error<T> error:
                                    TypedListWrapper.this.reportIndexUnwrapProblem(i, tag, error);
                                    if (error.partialValue().isEmpty()) {
                                        continue;
                                    }

                                    return error.partialValue().get();
                            }
                        }

                        return this.endOfData();
                    }
                }
            };
        }
    }

}
