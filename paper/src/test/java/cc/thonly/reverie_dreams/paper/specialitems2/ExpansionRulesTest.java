package cc.thonly.reverie_dreams.paper.specialitems2;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ExpansionRulesTest {
    @Test void chequeExchangeConservesValueAndRefusesAnUnrepresentableDeposit() {
        for (int amount = 0; amount <= 50_000; amount++) {
            var coins = MoneyRules.calculate(amount);
            assertEquals(amount, coins.value());
            assertTrue(coins.silver() >= 0 && coins.silver() < 10);
            assertTrue(coins.copper() >= 0 && coins.copper() < 10);
        }
        assertEquals(Integer.MAX_VALUE, MoneyRules.calculate(Integer.MAX_VALUE).value());
        assertEquals(Integer.MAX_VALUE, MoneyRules.add(Integer.MAX_VALUE - 10, 10).orElseThrow());
        assertTrue(MoneyRules.add(Integer.MAX_VALUE, 1).isEmpty());
        assertTrue(MoneyRules.add(1, Long.MAX_VALUE).isEmpty());
        assertTrue(MoneyRules.add(5, -1).isEmpty());
    }

    @Test void mixedOreIdsShareAVeinButDiagonalContactDoesNotJoinIt() {
        var scan = OreScanRules.build(List.of(ore(0,0,0,"diamond"), ore(1,0,0,"iron"), ore(2,1,0,"silver")), .5,.5,.5);
        assertEquals(2, scan.veins().size());
        assertEquals(List.of("diamond","iron"), scan.veins().getFirst().stream().map(OreScanRules.Ore::id).toList());
        assertEquals(3, scan.targets().size());
        assertEquals(0, scan.nearestDistance());
        assertEquals(3, scan.distribution().stream().mapToInt(OreScanRules.Distribution::count).sum());
    }

    @Test void scanMarksAtMostTwelveFromNearestVeinAndTwentyOverall() {
        var ores = new ArrayList<OreScanRules.Ore>();
        for (int x=0;x<16;x++) ores.add(ore(x,0,0,"diamond"));
        for (int z=0;z<30;z+=3) ores.add(ore(0,0,z+3,"coal"));
        var scan = OreScanRules.build(ores,.5,.5,.5);
        assertEquals(20, scan.targets().size());
        assertEquals(12, scan.targets().stream().filter(ore -> ore.id().equals("diamond")).count());
        assertEquals(26, scan.ores().size());
        assertEquals(11, scan.veins().size());
    }
    private static OreScanRules.Ore ore(int x,int y,int z,String id) {
        return new OreScanRules.Ore(new OreScanRules.Position(x,y,z),id,0);
    }
}
