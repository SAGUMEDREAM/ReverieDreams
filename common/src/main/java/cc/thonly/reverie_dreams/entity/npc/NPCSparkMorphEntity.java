package cc.thonly.reverie_dreams.entity.npc;
public interface NPCSparkMorphEntity {

    String reverie_dreams$getModelId();

    void reverie_dreams$setModelId(String modelId);

    String reverie_dreams$getTexture();

    void reverie_dreams$setTexture(String texture);

    default boolean reverie_dreams$hasModel() {
        String modelId = reverie_dreams$getModelId();
        return modelId != null && !modelId.isBlank();
    }
}