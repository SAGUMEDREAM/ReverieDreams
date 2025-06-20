package cc.thonly.reverie_dreams.entity.npc;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum NPCState {
    NORMAL(0),
    NO_WALK(1),
    SNAKING(2),
    SEATED(3),
    WORKING(4);
    private final Integer id;

    public static NPCState fromInt(Integer value) {
        return Arrays.stream(values())
                .filter(state -> state.id.equals(value))
                .findFirst()
                .orElse(null);
    }
}