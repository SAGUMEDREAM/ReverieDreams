package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.resources.Identifier;
import java.util.Locale;

public enum TenguAppearance {
    WOLF_RED(false,false,0), WOLF_BLUE(false,false,1), WOLF_MOSS(false,false,2), WOLF_PLUM(false,false,3),
    CROW_RED(true,false,0), CROW_BLUE(true,false,1), CROW_OCHRE(true,false,2), CROW_PLUM(true,false,3),
    WOLF_FEMALE_RED(false,true,0), WOLF_FEMALE_BLUE(false,true,1), WOLF_FEMALE_MOSS(false,true,2), WOLF_FEMALE_PLUM(false,true,3),
    CROW_FEMALE_RED(true,true,0), CROW_FEMALE_BLUE(true,true,1), CROW_FEMALE_OCHRE(true,true,2), CROW_FEMALE_PLUM(true,true,3);

    private final boolean crow;
    private final boolean female;
    private final int index;
    private final Identifier modelId;
    TenguAppearance(boolean crow,boolean female,int index) {
        this.crow=crow;this.female=female;this.index=index;
        this.modelId=ReverieDreams.id((crow?"crow_tengu_":"white_wolf_tengu_")+(female?"female_":"")+String.format(Locale.ROOT,"%02d",index+1));
    }
    public boolean crow(){return this.crow;}
    public boolean female(){return this.female;}
    public int index(){return this.index;}
    public Identifier modelId(){return this.modelId;}
    public Identifier resourceId(){return this.modelId.withPrefix("entity/");}
    public Identifier textureId(){return this.modelId.withPrefix("textures/entity/").withSuffix(".png");}
    public static TenguAppearance of(boolean crow,int index){return of(crow,false,index);}
    public static TenguAppearance of(boolean crow,boolean female,int index){return values()[(female?8:0)+(crow?4:0)+Math.floorMod(index,4)];}
    public static TenguAppearance of(boolean crow,Identifier id){
        for(var v:values())if(v.crow==crow&&v.modelId.equals(id))return v;
        return of(crow,0);
    }
}
