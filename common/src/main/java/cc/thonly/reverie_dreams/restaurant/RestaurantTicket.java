package cc.thonly.reverie_dreams.restaurant;

import cc.thonly.reverie_dreams.block.entity.RestaurantBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.*;
import java.util.*;
import java.util.function.*;

/** An accepted order retains its menu even after the owner edits the catalogue. */
public record RestaurantTicket(BlockPos bellPos, UUID bellId, BlockPos bookPos, UUID bookId,
                               List<Identifier> foods, List<Identifier> drinks) {
    public record Facility(UUID id, UUID partner, boolean bell) {}
    public enum Availability { READY, UNLOADED, REMOVED }
    public RestaurantTicket { bellPos = bellPos.immutable(); bookPos = bookPos.immutable(); foods = List.copyOf(foods); drinks = List.copyOf(drinks); }
    public static RestaurantTicket of(RestaurantService.Shop shop) {
        return new RestaurantTicket(shop.bell().getBlockPos(), shop.bell().id(), shop.book().getBlockPos(), shop.book().id(),
                shop.book().menu().available(false, id -> RestaurantService.valid(id, false)), shop.book().menu().available(true, id -> RestaurantService.valid(id, true)));
    }
    public boolean allows(Identifier item, boolean beverage) { return (beverage ? drinks : foods).contains(item); }
    public Availability availability(Predicate<BlockPos> loaded, Function<BlockPos, Facility> resolver) {
        if (!loaded.test(bellPos) || !loaded.test(bookPos)) return Availability.UNLOADED;
        var bell = resolver.apply(bellPos); var book = resolver.apply(bookPos);
        return bell != null && book != null && bell.bell && !book.bell && bellId.equals(bell.id) && bookId.equals(book.id)
                && bookId.equals(bell.partner) && bellId.equals(book.partner) ? Availability.READY : Availability.REMOVED;
    }
    public void save(ValueOutput out) {
        out.putLong("Restaurant.BellPos", bellPos.asLong()); out.putLong("Restaurant.BookPos", bookPos.asLong());
        out.putString("Restaurant.BellId", bellId.toString()); out.putString("Restaurant.BookId", bookId.toString());
        out.store("Restaurant.Foods", Identifier.CODEC.listOf(), foods); out.store("Restaurant.Drinks", Identifier.CODEC.listOf(), drinks);
    }
    public static RestaurantTicket load(ValueInput in) {
        var bell = RestaurantBlockEntity.uuid(in.getStringOr("Restaurant.BellId", ""), null);
        var book = RestaurantBlockEntity.uuid(in.getStringOr("Restaurant.BookId", ""), null);
        var bpos = in.getLong("Restaurant.BellPos"); var mpos = in.getLong("Restaurant.BookPos");
        if (bell == null || book == null || bpos.isEmpty() || mpos.isEmpty()) return null;
        return new RestaurantTicket(BlockPos.of(bpos.get()), bell, BlockPos.of(mpos.get()), book,
                in.read("Restaurant.Foods", Identifier.CODEC.listOf()).orElse(List.of()).stream().limit(RestaurantMenu.LIMIT).toList(),
                in.read("Restaurant.Drinks", Identifier.CODEC.listOf()).orElse(List.of()).stream().limit(RestaurantMenu.LIMIT).toList());
    }
}
