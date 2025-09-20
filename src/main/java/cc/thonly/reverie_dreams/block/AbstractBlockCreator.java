package cc.thonly.reverie_dreams.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
@Getter
public abstract class AbstractBlockCreator {
    public static final Map<Class<? extends Class<AbstractBlockCreator>>, List<AbstractBlockCreator>> INSTANCES = new Object2ObjectOpenHashMap<>();
    private final String name;
    private final Identifier id;

    public AbstractBlockCreator(String name, Identifier id) {
        this.name = name;
        this.id = id;
        List<AbstractBlockCreator> list = INSTANCES.computeIfAbsent((Class<? extends Class<AbstractBlockCreator>>) (Object) this.getClass(), x -> new ObjectArrayList<>());
        list.add(this);
    }

    protected Identifier prefix(String name) {
        return Identifier.of(this.id.getNamespace(), name + "_" + this.id.getPath());
    }

    protected Identifier suffix(String name) {
        return Identifier.of(this.id.getNamespace(), this.id.getPath() + "_" + name);
    }

    protected Identifier prefix(Identifier id, String name) {
        return Identifier.of(id.getNamespace(), name + "_" + id.getPath());
    }

    protected Identifier suffix(Identifier id, String name) {
        return Identifier.of(id.getNamespace(), id.getPath() + "_" + name);
    }

    protected abstract Stream<Block> stream();

    protected abstract AbstractBlockCreator build();
}
