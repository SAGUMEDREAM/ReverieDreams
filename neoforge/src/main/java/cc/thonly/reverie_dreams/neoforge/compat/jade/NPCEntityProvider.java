package cc.thonly.reverie_dreams.neoforge.compat.jade;

import cc.thonly.reverie_dreams.entity.npc.NPCCompanionEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.neoforge.compat.jade.element.FoodElement;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.impl.ui.TextElementImpl;

import java.util.Objects;
import java.util.Optional;

public class NPCEntityProvider implements IEntityComponentProvider {
    public static final NPCEntityProvider INSTANCE = new NPCEntityProvider();

    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor accessor, IPluginConfig iPluginConfig) {
        if (!(accessor.getEntity() instanceof NPCSimpleEntity npc)) {
            return;
        }
        if (!(npc instanceof NPCCompanionEntity)) {
            return;
        }
        Optional<NPCServerData> optional = NPCServerDataProvider.INSTANCE.decodeFromData(accessor);
        if (optional.isEmpty()) {
            return;
        }
        NPCServerData data = optional.get();
        iTooltip.add(new FoodElement(data.nutrition()));
        if (data.tamed()) {
            Component owner = data.owner();
            if (!Objects.equals(PlainTextContents.EMPTY, owner.getContents())) {
                iTooltip.add(new TextElementImpl(Component.translatable("jade.owner", owner)));
            }
            if (Objects.equals(data.state(), NPCStates.WORKING)) {
                iTooltip.add(
                        new TextElementImpl(
                                Component.empty()
                                         .append(Component.translatable(data.state().translateKey()))
                                         .append(Component.literal(" / "))
                                         .append(Component.translatable(data.workMode().translateKey()))
                        )
                );
            } else {
                iTooltip.add(
                        new TextElementImpl(
                                Component.empty().append(Component.translatable(data.state().translateKey()))
                        )
                );
            }
            if (Objects.equals(data.state(), NPCStates.WORKING) && Objects.equals(data.workMode(), NPCWorkModes.CUSTOMER)) {
                for (Component component : data.foodTextComponents()) {
                    iTooltip.add(component);
                }
            }
        }
    }

    @Override
    public Identifier getUid() {
        return JadePlugin.NPC_DESCRIPTION_PROVIDER;
    }
}
