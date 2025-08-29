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
    public static final Codec<Entry> CODEC = Codec.unit(Entry::new);
    private final ServerPlayerEntity player;
    private final Entry entry;

    private TradingCardManager() {
        this(null);
    }

    public TradingCardManager(ServerPlayerEntity player) {
        this.player = player;
        this.entry = new Entry();
    }

    public void read(ReadView view) {

    }

    public void write(WriteView view) {

    }

    @Getter
    public static class Entry {
        public static Codec<Entry> EMPTY = Codec.unit(Entry::new);
        public static Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStackWrapper.CODEC.listOf().fieldOf("list").forGetter(Entry::getList)
        ).apply(instance, Entry::new));
        private List<ItemStackWrapper> list = new ObjectArrayList<>();

        public Entry() {

        }

        public Entry(List<ItemStackWrapper> list) {
            this.list = new ObjectArrayList<>(list);
        }

        public void add(ItemStack itemStack) {

        }

        public Stream<ItemStackWrapper> stream() {
            return this.list.stream();
        }

    }
}
