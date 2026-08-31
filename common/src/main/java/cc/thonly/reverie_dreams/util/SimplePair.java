package cc.thonly.reverie_dreams.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class SimplePair<K, V> {
    private K key;
    private V value;
}
