package cc.thonly.reverie_dreams.data.craftengine;

import lombok.Getter;

public class CraftEngineDefinition {
    @Getter
    final ItemDefinitionList itemDefinitions;
    @Getter
    final BlockDefinitionList blockDefinitions;

    public CraftEngineDefinition(ItemDefinitionList itemDefinitions, BlockDefinitionList blockDefinitions) {
        this.itemDefinitions = itemDefinitions;
        this.blockDefinitions = blockDefinitions;
    }
}
