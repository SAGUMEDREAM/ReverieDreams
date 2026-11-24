package cc.thonly.reverie_dreams.world.gensokyo.landmark;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LandmarkDistributionTest {
    @Test void densitiesStayBoundedAndTheSunflowerCoreIsDenserThanTheMeadow() {
        double meadow=0,sun=0;int empty=0;
        for(int z=-256;z<256;z+=2)for(int x=-256;x<256;x+=2) {
            double a=LandmarkDistribution.density(LandmarkVegetationConfiguration.Theme.PHANTOM_MEADOW,177,x,z);
            double b=LandmarkDistribution.density(LandmarkVegetationConfiguration.Theme.GARDEN_OF_THE_SUN,177,x,z);
            assertTrue(a>=0&&a<=1&&b>=0&&b<=1);meadow+=a;sun+=b;if(b==0)empty++;
        }
        assertTrue(sun>meadow*1.5);assertTrue(empty>1000&&empty<15000,"Open lanes must exist without consuming the field");
    }
    @Test void theFieldChangesSmoothlyAcrossChunkBoundariesAndNegativeCoordinates() {
        for(int z=-80;z<80;z++)for(int x=-80;x<80;x++) {
            double first=LandmarkDistribution.field(63,x,z,44),next=LandmarkDistribution.field(63,x+1,z,44);
            assertTrue(Math.abs(first-next)<.04);
            assertEquals(first,LandmarkDistribution.field(63,x,z,44));
        }
    }
    @Test void differentWorldSeedsChangeTheLayoutWhileEachSeedHasBroadPatches() {
        double sameNeighbour=0,differentSeed=0;
        for(int z=0;z<512;z+=8)for(int x=0;x<512;x+=8) {
            double a=LandmarkDistribution.field(1,x,z,44);
            sameNeighbour+=Math.abs(a-LandmarkDistribution.field(1,x+1,z,44));
            differentSeed+=Math.abs(a-LandmarkDistribution.field(2,x,z,44));
        }
        assertTrue(differentSeed>sameNeighbour*5);
    }
}
