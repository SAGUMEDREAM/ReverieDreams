package cc.thonly.reverie_dreams.block.bundle;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.blay09.mods.balm.world.level.block.BalmBlockRegistrar;
import net.blay09.mods.balm.world.level.block.DeferredBlock;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
@Getter
public abstract class AbstractBlockBundle {
    public static final Map<Class<? extends Class<AbstractBlockBundle>>, List<AbstractBlockBundle>> INSTANCES = new Object2ObjectOpenHashMap<>();
    private final String name;
    private final Identifier id;

    public AbstractBlockBundle(String name, Identifier id) {
        this.name = name;
        this.id = id;
        List<AbstractBlockBundle> list = INSTANCES.computeIfAbsent((Class<? extends Class<AbstractBlockBundle>>) (Object) this.getClass(), x -> new ObjectArrayList<>());
        list.add(this);
    }

    protected Identifier prefix(String name) {
        return Identifier.fromNamespaceAndPath(this.id.getNamespace(), name + "_" + this.id.getPath());
    }

    protected Identifier suffix(String name) {
        return Identifier.fromNamespaceAndPath(this.id.getNamespace(), this.id.getPath() + "_" + name);
    }

    protected Identifier prefix(Identifier id, String name) {
        return Identifier.fromNamespaceAndPath(id.getNamespace(), name + "_" + id.getPath());
    }

    protected Identifier suffix(Identifier id, String name) {
        return Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_" + name);
    }

    protected abstract Collection<DeferredBlock> stream();

    protected abstract AbstractBlockBundle build(BalmBlockRegistrar registrar);
}
