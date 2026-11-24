package cc.thonly.reverie_dreams.paper.world.forest;
import java.util.Random;
/** Exact target26.1.2 BIOME_INFO_NOISE: WorldgenRandom(Legacy2345), octave0, offsets disabled. */
public final class GensokyoForestNoise {
 private static final int[][] G={{1,1,0},{-1,1,0},{1,-1,0},{-1,-1,0},{1,0,1},{-1,0,1},{1,0,-1},{-1,0,-1},{0,1,1},{0,-1,1},{0,1,-1},{0,-1,-1}};
 private static final double SQRT3=Math.sqrt(3),F2=.5*(SQRT3-1),G2=(3-SQRT3)/6;
 private final int[] p=new int[256];
 public GensokyoForestNoise(){var random=new Random(2345L);random.nextDouble();random.nextDouble();random.nextDouble();for(int i=0;i<256;i++)p[i]=i;for(int i=0;i<256;i++){int other=i+random.nextInt(256-i),old=p[i];p[i]=p[other];p[other]=old;}}
 private int p(int v){return p[v&255];}
 private static int floor(double value){int cast=(int)value;return value<cast?cast-1:cast;}
 private static double corner(int g,double x,double z){double t=.5-x*x-z*z-0d*0d;if(t<0)return 0;t*=t;return t*t*(G[g][0]*x+G[g][1]*z+G[g][2]*0d);}
 public double value(double x,double z){double skew=(x+z)*F2;int i=floor(x+skew),j=floor(z+skew);double unskew=(i+j)*G2,ox=i-unskew,oz=j-unskew,dx=x-ox,dz=z-oz;int a=dx>dz?1:0,b=dx>dz?0:1;double x1=dx-a+G2,z1=dz-b+G2,x2=dx-1+2*G2,z2=dz-1+2*G2;int ii=i&255,jj=j&255;int g0=p(ii+p(jj))%12,g1=p(ii+a+p(jj+b))%12,g2=p(ii+1+p(jj+1))%12;return 70*(corner(g0,dx,dz)+corner(g1,x1,z1)+corner(g2,x2,z2));}
 public int attempts(GensokyoForestType type,int blockX,int blockZ){return (int)Math.ceil((value(blockX/96d,blockZ/96d)+(type==GensokyoForestType.MAGIC_FOREST?.85:.70))*type.densityScale());}
}
