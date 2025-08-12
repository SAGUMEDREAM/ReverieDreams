package cc.thonly.mystias_izakaya.datagen;

import cc.thonly.mystias_izakaya.datagen.generator.DrinkProvider;
import cc.thonly.mystias_izakaya.registry.DrinkProperties;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class MIDrinkProvider extends DrinkProvider {
    public MIDrinkProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> future) {
        super(output, future);
    }

    @Override
    protected void configured() {
        this.createFactory(DrinkProperties.UNDEFINED)
                .build();
        this.createFactory(DrinkProperties.ALCOHOL_FREE)
                .build();
        this.createFactory(DrinkProperties.LOW_ALCOHOL)
                .build();
        this.createFactory(DrinkProperties.MID_ALCOHOL)
                .build();
        this.createFactory(DrinkProperties.HIGH_ALCOHOL)
                .build();
        this.createFactory(DrinkProperties.CAN_ADD_ICE)
                .build();
        this.createFactory(DrinkProperties.CAN_HEATED)
                .build();
        this.createFactory(DrinkProperties.COCKTAIL)
                .build();
        this.createFactory(DrinkProperties.WESTERN_WINE)
                .build();
        this.createFactory(DrinkProperties.FRUIT)
                .build();
        this.createFactory(DrinkProperties.SWEET)
                .build();
        this.createFactory(DrinkProperties.BITTER)
                .build();
        this.createFactory(DrinkProperties.SOJU)
                .build();
        this.createFactory(DrinkProperties.SAKE)
                .build();
        this.createFactory(DrinkProperties.PUNGENT)
                .build();
        this.createFactory(DrinkProperties.BUBBLE)
                .build();
        this.createFactory(DrinkProperties.BEER)
                .build();
        this.createFactory(DrinkProperties.DIRECT_DRINKING)
                .build();
        this.createFactory(DrinkProperties.LIQUEUR)
                .build();
        this.createFactory(DrinkProperties.REFRESHING)
                .build();
        this.createFactory(DrinkProperties.CLASSICAL)
                .build();
        this.createFactory(DrinkProperties.MODERN)
                .build();

    }

    @Override
    public String getName() {
        return "Drink";
    }
}
