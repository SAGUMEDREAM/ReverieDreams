package cc.thonly.minecraft.util;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ProblemReporter;

import java.util.function.Consumer;

public class TagValueFunction {
    public static void ofInput(CompoundTag compoundTag, RegistryAccess access, Consumer<ValueInput> function) {
        function.accept(new TagValueInput(new ProblemReporter.Collector(), new ValueInputContextHelper(access, NbtOps.INSTANCE), compoundTag));
    }

    public static void ofOutput(CompoundTag compoundTag, RegistryAccess access, Consumer<ValueOutput> function) {
        function.accept(new TagValueOutput(new ProblemReporter.Collector(), NbtOps.INSTANCE, compoundTag));
    }
}
