package cc.thonly.reverie_dreams.neoforge.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Mixin(CreativeModeTab.class)
public class CreativeModeTabMixin {
    @Mutable
    @Shadow
    @Final
    public List<Identifier> tabsAfter;

    @Mutable
    @Shadow
    @Final
    public List<Identifier> tabsBefore;

    @Inject(method = "<init>(Lnet/minecraft/world/item/CreativeModeTab$Row;ILnet/minecraft/world/item/CreativeModeTab$Type;Lnet/minecraft/network/chat/Component;Ljava/util/function/Supplier;Lnet/minecraft/world/item/CreativeModeTab$DisplayItemsGenerator;Lnet/minecraft/resources/Identifier;ZILnet/minecraft/resources/Identifier;IILjava/util/List;Ljava/util/List;)V",
            at = @At("RETURN"))
    public void reverie_dreams$modifyList(CreativeModeTab.Row row, int column, CreativeModeTab.Type type, Component displayName, Supplier iconGenerator, CreativeModeTab.DisplayItemsGenerator displayItemGenerator, Identifier scrollerSpriteLocation, boolean hasSearchBar, int searchBarWidth, Identifier tabsImage, int labelColor, int slotColor, List tabsBefore, List tabsAfter, CallbackInfo ci) {
        this.reverie_dreams$modifyListInvoke();
    }

    @Inject(method = "<init>(Lnet/minecraft/world/item/CreativeModeTab$Builder;)V", at = @At("RETURN"))
    public void reverie_dreams$modifyList(CreativeModeTab.Builder builder, CallbackInfo ci) {
        this.reverie_dreams$modifyListInvoke();
    }

    @Unique
    private void reverie_dreams$modifyListInvoke() {
        this.tabsAfter = new ArrayList<>();
        this.tabsBefore = new ArrayList<>();
    }
}
