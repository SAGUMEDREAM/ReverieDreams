package cc.thonly.reverie_dreams.world.trading_card;

import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

import java.util.List;
import java.util.stream.Stream;

@Getter
public class TradingCardManager {
    public static final Codec<Entries> CODEC = Codec.unit(Entries::new);
    private final ServerPlayerEntity player;
    private final Entries entries;

    private TradingCardManager() {
        this(null);
    }

    public TradingCardManager(ServerPlayerEntity player) {
        this.player = player;
        this.entries = new Entries();
    }

    public void read(ReadView view) {

    }

    public void write(WriteView view) {

    }

    @Getter
    public static class Entries {
        public static Codec<Entries> EMPTY = Codec.unit(Entries::new);
        public static Codec<Entries> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStackWrapper.CODEC.listOf().fieldOf("list").forGetter(Entries::getList)
        ).apply(instance, Entries::new));
        private List<ItemStackWrapper> list = new ObjectArrayList<>();

        public Entries() {

        }

        public Entries(List<ItemStackWrapper> list) {
            this.list = new ObjectArrayList<>(list);
        }

        public void add(ItemStack itemStack) {

        }

        public Stream<ItemStackWrapper> stream() {
            return this.list.stream();
        }

    }
}
