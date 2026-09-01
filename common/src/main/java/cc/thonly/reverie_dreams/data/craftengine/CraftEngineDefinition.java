package cc.thonly.reverie_dreams.data.craftengine;

import lombok.Getter;

import java.util.List;

public class CraftEngineDefinition {
    @Getter
    final List<ItemDefinitionList> itemDefinitions;
    @Getter
    final List<BlockDefinitionList> blockDefinitions;

    public CraftEngineDefinition(List<ItemDefinitionList> itemDefinitions, List<BlockDefinitionList> blockDefinitions) {
        this.itemDefinitions = itemDefinitions;
        this.blockDefinitions = blockDefinitions;
    }
}
