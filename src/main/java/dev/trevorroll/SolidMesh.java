package dev.trevorroll;

import java.util.ArrayList;
import java.util.List;

/** Immutable lit geometry. Coordinates and normals are real 3D; no icon texture planes. */
public record SolidMesh(Vertex[] vertices) {
    public record V(double x,double y,double z) {
        V add(V b){return new V(x+b.x,y+b.y,z+b.z);}
        V sub(V b){return new V(x-b.x,y-b.y,z-b.z);}
        V mul(double s){return new V(x*s,y*s,z*s);}
        V cross(V b){return new V(y*b.z-z*b.y,z*b.x-x*b.z,x*b.y-y*b.x);}
        V unit(){double l=Math.sqrt(x*x+y*y+z*z);return l<1e-10?new V(0,0,1):mul(1/l);}
    }
    public record Vertex(float x,float y,float z,float nx,float ny,float nz,int color) {}
    public static final class Builder {
        private final List<Vertex> out=new ArrayList<>();
        public SolidMesh build(){return new SolidMesh(out.toArray(Vertex[]::new));}
        private void vertex(V p,V n,int color){out.add(new Vertex((float)p.x,(float)p.y,(float)p.z,(float)n.x,(float)n.y,(float)n.z,color));}
        public void quad(V a,V b,V c,V d,int color){
            V n=b.sub(a).cross(c.sub(a)).unit();
            vertex(a,n,color);vertex(b,n,color);vertex(c,n,color);vertex(d,n,color);
        }
        private void smooth(V a,V b,V c,V d,V na,V nb,V nc,V nd,int color){
            vertex(a,na,color);vertex(b,nb,color);vertex(c,nc,color);vertex(d,nd,color);
        }
        public void box(double x,double y,double z,double w,double h,double depth,double bevel,int color){
            double b=Math.min(bevel,Math.min(depth/3,Math.min(w,h)/4));
            double[][] outer=rect(x,y,w,h,b),inner=rect(x,y,w-2*b,h-2*b,b*.4);
            if(b>0){wall(inner,z-depth/2,outer,z-depth/2+b,color);wall(outer,z+depth/2-b,inner,z+depth/2,color);}
            wall(outer,z-depth/2+b,outer,z+depth/2-b,color);
            cap(inner,z+depth/2,false,color);cap(inner,z-depth/2,true,color);
        }
        /** True rounded edges/corners with continuous normals, baked once per object. */
        public void roundedBox(double x,double y,double z,double w,double h,double depth,double radius,int color){
            double[] half={w/2,h/2,depth/2};
            double r=Math.min(radius,Math.min(half[0],Math.min(half[1],half[2]))*.92);
            if(r<=0){box(x,y,z,w,h,depth,0,color);return;}
            for(int axis=0;axis<3;axis++)for(int sign:new int[]{-1,1}){
                int ua=(axis+1)%3,va=(axis+2)%3;double[] us=edgeSamples(half[ua],r),vs=edgeSamples(half[va],r);
                for(int i=0;i<us.length-1;i++)for(int j=0;j<vs.length-1;j++){
                    V[] p=new V[4],n=new V[4];double[][] uv={{us[i],vs[j]},{us[i+1],vs[j]},{us[i+1],vs[j+1]},{us[i],vs[j+1]}};
                    for(int k=0;k<4;k++){
                        double[] q={0,0,0};q[axis]=sign*half[axis];q[ua]=uv[k][0];q[va]=uv[k][1];
                        double[] inner=new double[3];for(int a=0;a<3;a++)inner[a]=Math.clamp(q[a],-half[a]+r,half[a]-r);
                        n[k]=new V(q[0]-inner[0],q[1]-inner[1],q[2]-inner[2]).unit();
                        p[k]=new V(x+inner[0]+n[k].x*r,y+inner[1]+n[k].y*r,z+inner[2]+n[k].z*r);
                    }
                    if(sign>0)smooth(p[0],p[1],p[2],p[3],n[0],n[1],n[2],n[3],color);
                    else smooth(p[3],p[2],p[1],p[0],n[3],n[2],n[1],n[0],color);
                }
            }
        }
        private static double[] edgeSamples(double half,double r){return new double[]{-half,-half+r*.25,-half+r*.5,-half+r*.75,-half+r,half-r,half-r*.75,half-r*.5,half-r*.25,half};}
        public void torus(V center,V axis,double major,double minor,double depth,int color){
            V normal=axis.unit(),right=normal.cross(Math.abs(normal.y)<.9?new V(0,1,0):new V(1,0,0)).unit(),up=normal.cross(right).unit();
            for(int u=0;u<64;u++)for(int v=0;v<12;v++){
                V[] p=new V[4],n=new V[4];int[][] uv={{u,v},{u+1,v},{u+1,v+1},{u,v+1}};
                for(int k=0;k<4;k++){
                    double a=uv[k][0]*Math.PI*2/64,b=uv[k][1]*Math.PI*2/12;
                    V radial=right.mul(Math.cos(a)).add(up.mul(Math.sin(a)));
                    p[k]=center.add(radial.mul(major+minor*Math.cos(b))).add(normal.mul(depth*Math.sin(b)));
                    n[k]=radial.mul(Math.cos(b)/minor).add(normal.mul(Math.sin(b)/depth)).unit();
                }
                smooth(p[0],p[1],p[2],p[3],n[0],n[1],n[2],n[3],color);
            }
        }
        /** Hollow bell with a curved centre line; the mouth stays open. */
        public void bell(V[] path,double[] radii,int outside,int inside){
            int segments=48;
            V[][] outer=new V[path.length][segments],inner=new V[path.length][segments];
            V[] axes=new V[path.length];
            for(int i=0;i<path.length;i++){
                V axis=(i==0?path[1].sub(path[0]):i==path.length-1?path[i].sub(path[i-1]):path[i+1].sub(path[i-1])).unit();axes[i]=axis;
                V right=axis.cross(new V(0,0,1)).unit(),up=axis.cross(right).unit();
                for(int j=0;j<segments;j++){
                    double a=j*Math.PI*2/segments;V radial=right.mul(Math.cos(a)).add(up.mul(Math.sin(a)));
                    outer[i][j]=path[i].add(radial.mul(radii[i]));inner[i][j]=path[i].add(radial.mul(Math.max(.2,radii[i]-.45)));
                }
            }
            for(int i=0;i<path.length-1;i++)for(int j=0;j<segments;j++){
                int k=(j+1)%segments;quad(outer[i][j],outer[i][k],outer[i+1][k],outer[i+1][j],outside);
                quad(inner[i][j],inner[i+1][j],inner[i+1][k],inner[i][k],inside);
            }
            torus(path[path.length-1],axes[path.length-1],radii[radii.length-1]-.2,.38,.38,outside);
        }
        private static double[][] rect(double x,double y,double w,double h,double b){
            if(b==0)return new double[][]{{x-w/2,y-h/2},{x+w/2,y-h/2},{x+w/2,y+h/2},{x-w/2,y+h/2}};
            return new double[][]{{x-w/2+b,y-h/2},{x+w/2-b,y-h/2},{x+w/2,y-h/2+b},{x+w/2,y+h/2-b},
                {x+w/2-b,y+h/2},{x-w/2+b,y+h/2},{x-w/2,y+h/2-b},{x-w/2,y-h/2+b}};
        }
        private static V point(double[] xy,double z){return new V(xy[0],xy[1],z);}
        public void wall(double[][] a,double za,double[][] b,double zb,int color){
            for(int i=0;i<a.length;i++){int j=(i+1)%a.length;quad(point(a[i],za),point(a[j],za),point(b[j],zb),point(b[i],zb),color);}
        }
        public void cap(double[][] ring,double z,boolean reverse,int color){
            double x=0,y=0;for(var p:ring){x+=p[0];y+=p[1];}V center=new V(x/ring.length,y/ring.length,z);
            for(int i=0;i<ring.length;i++){int j=(i+1)%ring.length;V a=point(ring[i],z),b=point(ring[j],z);if(reverse)quad(center,b,a,a,color);else quad(center,a,b,b,color);}
        }
        public void extrude(double[][] ring,double z,double depth,int color){
            wall(ring,z-depth/2,ring,z+depth/2,color);cap(ring,z-depth/2,true,color);cap(ring,z+depth/2,false,color);
        }
        public void sphere(double x,double y,double z,double rx,double ry,double rz,int color){
            int around=Math.max(rx,Math.max(ry,rz))<2?20:40,rows=around/2;
            for(int v=0;v<rows;v++)for(int u=0;u<around;u++){
                double t0=Math.PI*v/rows,t1=Math.PI*(v+1)/rows,p0=2*Math.PI*u/around,p1=2*Math.PI*(u+1)/around;
                V a=spherePoint(t0,p0),b=spherePoint(t1,p0),c=spherePoint(t1,p1),d=spherePoint(t0,p1);
                smooth(stretch(a,x,y,z,rx,ry,rz),stretch(b,x,y,z,rx,ry,rz),stretch(c,x,y,z,rx,ry,rz),stretch(d,x,y,z,rx,ry,rz),
                    new V(a.x/rx,a.y/ry,a.z/rz).unit(),new V(b.x/rx,b.y/ry,b.z/rz).unit(),
                    new V(c.x/rx,c.y/ry,c.z/rz).unit(),new V(d.x/rx,d.y/ry,d.z/rz).unit(),color);
            }
        }
        private static V spherePoint(double t,double p){return new V(Math.sin(t)*Math.cos(p),Math.sin(t)*Math.sin(p),Math.cos(t));}
        private static V stretch(V a,double x,double y,double z,double rx,double ry,double rz){return new V(x+a.x*rx,y+a.y*ry,z+a.z*rz);}
        public void cylinder(V start,V end,double r0,double r1,int color,boolean caps){
            V axis=end.sub(start).unit(),right=axis.cross(Math.abs(axis.y)<.9?new V(0,1,0):new V(1,0,0)).unit(),up=axis.cross(right).unit();
            int segments=Math.max(r0,r1)<.5?8:Math.max(r0,r1)<2?20:64;
            for(int i=0;i<segments;i++){
                double t0=i*Math.PI*2/segments,t1=(i+1)*Math.PI*2/segments;
                V n0=right.mul(Math.cos(t0)).add(up.mul(Math.sin(t0))),n1=right.mul(Math.cos(t1)).add(up.mul(Math.sin(t1)));
                V a=start.add(n0.mul(r0)),b=start.add(n1.mul(r0)),c=end.add(n1.mul(r1)),d=end.add(n0.mul(r1));
                V normal0=n0.add(axis.mul((r0-r1)/Math.sqrt(end.sub(start).x*end.sub(start).x+end.sub(start).y*end.sub(start).y+end.sub(start).z*end.sub(start).z))).unit();
                V normal1=n1.add(axis.mul((r0-r1)/Math.sqrt(end.sub(start).x*end.sub(start).x+end.sub(start).y*end.sub(start).y+end.sub(start).z*end.sub(start).z))).unit();
                smooth(a,b,c,d,normal0,normal1,normal1,normal0,color);
                if(caps){quad(start,b,a,a,color);quad(end,d,c,c,color);}
            }
        }
        public void disc(double x,double y,double z,double radius,double depth,int color){cylinder(new V(x,y,z-depth/2),new V(x,y,z+depth/2),radius,radius,color,true);}
        public void ring(double x,double y,double z,double outer,double inner,double depth,int color){
            for(int i=0;i<96;i++){
                double a=i*Math.PI*2/96,b=(i+1)*Math.PI*2/96;
                V oa=new V(x+outer*Math.cos(a),y+outer*Math.sin(a),z+depth/2),ob=new V(x+outer*Math.cos(b),y+outer*Math.sin(b),z+depth/2);
                V ia=new V(x+inner*Math.cos(a),y+inner*Math.sin(a),z+depth/2),ib=new V(x+inner*Math.cos(b),y+inner*Math.sin(b),z+depth/2);
                quad(oa,ob,ib,ia,color);
                V oab=new V(oa.x,oa.y,z-depth/2),obb=new V(ob.x,ob.y,z-depth/2),iab=new V(ia.x,ia.y,z-depth/2),ibb=new V(ib.x,ib.y,z-depth/2);
                quad(oab,iab,ibb,obb,color);quad(oab,obb,ob,oa,color);quad(ibb,iab,ia,ib,color);
            }
        }
        /** Palette annulus has an actual open thumb hole through every depth layer. */
        public void palette(int wood,int edge){
            int n=96;double[][] outer=new double[n][2],hole=new double[n][2];
            for(int i=0;i<n;i++){double a=2*Math.PI*i/n;outer[i]=new double[]{11.5*Math.cos(a)*(1+.08*Math.sin(2*a)),9*Math.sin(a)};hole[i]=new double[]{5.4+2.1*Math.cos(a),3+2.1*Math.sin(a)};}
            wall(outer,-2,outer,2,edge);wall(hole,2,hole,-2,edge);
            for(int i=0;i<n;i++){int j=(i+1)%n;quad(point(outer[i],2),point(outer[j],2),point(hole[j],2),point(hole[i],2),wood);
                quad(point(outer[j],-2),point(outer[i],-2),point(hole[i],-2),point(hole[j],-2),wood);}
        }
    }
}
