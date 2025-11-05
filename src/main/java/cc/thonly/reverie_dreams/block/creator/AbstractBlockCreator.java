package cc.thonly.reverie_dreams.block.creator;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
@Getter
public abstract class AbstractBlockCreator {
    public static final Map<Class<? extends Class<AbstractBlockCreator>>, List<AbstractBlockCreator>> INSTANCES = new Object2ObjectOpenHashMap<>();
    private final String name;
    private final ResourceLocation id;

    public AbstractBlockCreator(String name, ResourceLocation id) {
        this.name = name;
        this.id = id;
        List<AbstractBlockCreator> list = INSTANCES.computeIfAbsent((Class<? extends Class<AbstractBlockCreator>>) (Object) this.getClass(), x -> new ObjectArrayList<>());
        list.add(this);
    }

    protected ResourceLocation prefix(String name) {
        return ResourceLocation.fromNamespaceAndPath(this.id.getNamespace(), name + "_" + this.id.getPath());
    }

    protected ResourceLocation suffix(String name) {
        return ResourceLocation.fromNamespaceAndPath(this.id.getNamespace(), this.id.getPath() + "_" + name);
    }

    protected ResourceLocation prefix(ResourceLocation id, String name) {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), name + "_" + id.getPath());
    }

    protected ResourceLocation suffix(ResourceLocation id, String name) {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_" + name);
    }

    protected abstract Stream<Block> stream();

    protected abstract AbstractBlockCreator build();
}
