package cc.thonly.reverie_dreams.paper.specialweapons;

/** Pure source fan and Entity.turn math. Angles are deliberately not guessed from variable names. */
public final class WeaponMath {
    public record Rotation(float yaw,float pitch){}
    public static float[] fan(boolean aimed){return aimed?new float[]{-6,-3,0,3,6}:new float[]{-10,-5,0,5,10};}
    /** Native26.1 Entity.turn(yawInput,pitchInput) multiplies EACH input by0.15f. */
    public static Rotation turn(float yaw,float pitch,float yawInput,float pitchInput){return new Rotation(yaw+yawInput*.15f,Math.clamp(pitch+pitchInput*.15f,-90,90));}
    private WeaponMath(){}
}
