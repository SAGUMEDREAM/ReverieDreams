package cc.thonly.reverie_dreams.paper.danmaku;

import cc.thonly.reverie_dreams.paper.util.Ids;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/** Pure Java translation of the original ordinary trajectory loops; no Bukkit/Minecraft/loader dependencies. */
public final class DanmakuMath {
    public record V(double x, double y, double z) {
        public V add(V v) { return new V(x + v.x, y + v.y, z + v.z); }
        public V scale(double n) { return new V(x * n, y * n, z * n); }
        public double length() { return Math.sqrt(x*x + y*y + z*z); }
        public V normal() { double n = length(); return n < 1e-8 ? new V(0,0,0) : scale(1/n); }
        public V cross(V v) { return new V(y*v.z-z*v.y,z*v.x-x*v.z,x*v.y-y*v.x); }
    }
    public record Config(int count, float spread, float radius, int layers, float spacing, float forward, int faceOutward) {
        public static final Config EMPTY = new Config(-1,-1,-1,-1,-1,-1,-1);
        int count(int fallback) { return count > 0 ? Math.clamp(count,1,64) : fallback; }
        float spread(float fallback) { return spread >= 0 ? Math.clamp(spread,0,360) : fallback; }
        float radius(float fallback) { return radius >= 0 ? Math.clamp(radius,0,16) : fallback; }
        int layers(int fallback) { return layers >= 0 ? Math.clamp(layers,0,8) : fallback; }
        float spacing(float fallback) { return spacing >= 0 ? Math.clamp(spacing,0,4) : fallback; }
        float forward(float fallback) { return forward >= 0 ? Math.clamp(forward,0,16) : fallback; }
        boolean outward(boolean fallback) { return faceOutward < 0 ? fallback : faceOutward != 0; }
    }
    public record Shot(V offset, V direction, float pitch, float yaw) {}
    private static final String[] STAR = {"##   ##"," ## ## "," # # # ","##   ##","  # #  ","   #   "};
    private static final String[] HEART = {"   #   ","  # #  "," # # # ","# # # #"," #   # "};
    private static final String[] X = {"#   #"," # # ","  #  "," # # ","#   #"};
    private DanmakuMath() {}
    public static V direction(float pitch, float yaw) {
        double p = Math.toRadians(pitch), y = Math.toRadians(yaw);
        return new V(-Math.sin(y)*Math.cos(p), -Math.sin(p), Math.cos(y)*Math.cos(p));
    }
    public static List<Shot> volley(String trajectory, float pitch, float yaw, Config config, int repeats) {
        if(trajectory.indexOf(':')>=0&&!trajectory.startsWith(Ids.MOD_ID + ":"))throw new IllegalArgumentException("Trajectory not migrated: "+trajectory);
        String name = trajectory.substring(trajectory.indexOf(':')+1);
        List<Shot> result = new ArrayList<>();
        for (int repeat=0; repeat<Math.min(64,repeats) && result.size()<64; repeat++) emit(result,name,pitch,yaw,config);
        return List.copyOf(result);
    }
    private static void add(List<Shot> list,V origin,float pitch,float yaw,double offset) {
        if (list.size()>=64) return;
        V dir=direction(pitch,yaw); list.add(new Shot(origin.add(dir.scale(offset)),dir,pitch,yaw));
    }
    private static void emit(List<Shot> out,String name,float pitch,float yaw,Config c) {
        V zero=new V(0,0,0), forward=direction(pitch,yaw), up=new V(0,1,0), right=forward.cross(up);
        right=right.length()<.001 ? new V(1,0,0) : right.normal();
        switch(name) {
            case "single" -> add(out,zero,pitch,yaw,.25);
            case "triple","fan" -> {
                int count=c.count(name.equals("triple")?3:5); float spread=c.spread(name.equals("triple")?30:40);
                float step=count==1?0:spread/(count-1),start=-spread*.5f;
                for(int i=0;i<count;i++) add(out,zero,pitch,yaw+start+step*i,.25);
            }
            case "bullet" -> {
                int layers=c.layers(2); float spacing=c.spacing(.4f);
                for(int layer=0;layer<=layers;layer++) for(int side=-layer;side<=layer;side++) if(Math.abs(side)==layer)
                    add(out,right.scale(side*spacing).add(forward.scale(-layer*spacing)),pitch,yaw,.25);
            }
            case "star","heart","x" -> {
                String[] pattern=name.equals("star")?STAR:name.equals("heart")?HEART:X;
                int h=pattern.length,w=0; for(String line:pattern) w=Math.max(w,line.length());
                V screenUp=right.cross(forward).normal(),base=forward.scale(Math.max(.25,c.forward(2)));
                float spacing=c.spacing(1);
                for(int row=0;row<h;row++) for(int col=0;col<pattern[row].length();col++) if(pattern[row].charAt(col)=='#')
                    add(out,base.add(right.scale((col-(w-1)/2.0)*spacing)).add(screenUp.scale(((h-1)/2.0-row)*spacing)),pitch,yaw,0);
            }
            case "ring","ring_horizontal" -> {
                int count=c.count(name.equals("ring")?24:16); double radius=c.radius(name.equals("ring")?1:1.5f);
                for(int i=0;i<count;i++) {
                    float degrees=360f/count*i,angle=(float)Math.toRadians(degrees);
                    double ox=radius*Math.cos(angle),oz=radius*Math.sin(angle);
                    float targetYaw=c.outward(true)?(float)Math.toDegrees(Math.atan2(oz,ox))-90:yaw;
                    add(out,new V(ox,0,oz),pitch,targetYaw,.25);
                }
            }
            case "round","ring_view" -> {
                int count=c.count(name.equals("round")?12:16); float radius=c.radius(1);
                for(int i=0;i<count;i++) {
                    float angle=(float)Math.toRadians(360f/count*i);
                    // Source quaternion rotateY(-yaw).rotateX(-pitch): rotateX is applied first.
                    double ax=(float)Math.cos(angle),ay=0,az=(float)Math.sin(angle);
                    double p=Math.toRadians(-pitch),y=Math.toRadians(-yaw);
                    double by=ay*Math.cos(p)-az*Math.sin(p),bz=ay*Math.sin(p)+az*Math.cos(p);
                    V dir=new V(ax*Math.cos(y)+bz*Math.sin(y),by,-ax*Math.sin(y)+bz*Math.cos(y)).normal();
                    float targetYaw=(float)Math.toDegrees(Math.atan2(-dir.x,dir.z)),targetPitch=(float)Math.toDegrees(-Math.asin(Math.clamp(dir.y,-1,1)));
                    add(out,dir.scale(radius),targetPitch,targetYaw,.25);
                }
            }
            default -> throw new IllegalArgumentException("Trajectory not migrated: "+name);
        }
    }
    /** Actual26.1.2 Projectile#getMovementToShoot triangular divergence, not a guessed Gaussian. */
    public static V velocity(Shot shot,double speed,RandomGenerator random) {
        double spread=.0172275*.05;
        return shot.direction.normal().add(new V((random.nextDouble()-random.nextDouble())*spread,
                (random.nextDouble()-random.nextDouble())*spread,(random.nextDouble()-random.nextDouble())*spread)).scale(speed);
    }
    public static double hitMargin(int age,double scale) { return Math.max(.02,Math.max(0,Math.min(.3,(age-2)/20.0))+.25*(scale-1)); }
}
