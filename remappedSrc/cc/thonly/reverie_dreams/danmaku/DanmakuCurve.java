package cc.thonly.reverie_dreams.danmaku;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class DanmakuCurve {
    MotionCalculation x;
    MotionCalculation y;
    MotionCalculation z;

    public DanmakuCurve() {
        this.x = (age, px) -> px;
        this.y = (age, py) -> py;
        this.z = (age, pz) -> pz;
    }
}
