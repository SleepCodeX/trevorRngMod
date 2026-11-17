package dev.trevorroll;

import static dev.trevorroll.SolidMesh.*;

/** Cached display environments; Beth uses the separately baked CC0 character rig. */
public final class SceneModelLibrary {
    private SceneModelLibrary() {}
    private static final SolidMesh TRAIL=terrain(false),END=terrain(true);
    public static SolidMesh terrainFor(int stage){return stage==0?TRAIL:END;}
    private static SolidMesh terrain(boolean end){
        var b=new Builder();
        b.cylinder(new V(0,11.5,0),new V(0,14.2,0),24,21.5,end?0xFF403446:0xFF87694B,true);
        b.cylinder(new V(0,10.6,0),new V(0,11.7,0),24.3,24,end?0xFFA89C9C:0xFFD5BA86,true);
        for(int i=0;i<9;i++){
            double a=i*2*Math.PI/9,x=21*Math.cos(a),z=21*Math.sin(a);
            if(end){
                double h=7+(i%3)*3.2;
                b.box(x,10-h/2,z,3.1,h,3.1,.45,0xFF282235);
                b.box(x,8.6-h,z,1.7,2.1,1.7,.3,0xFF9E76BE);
            }else{
                double size=2.5+i%3;
                b.sphere(x,10-size*.3,z,size,size*.62,size*.72,i%2==0?0xFFB08660:0xFFAA835C);
                if(i%3==0){
                    b.cylinder(new V(x-1,10,z),new V(x-1,3,z),.7,.65,0xFF66816A,true);
                    b.sphere(x-1,3,z,.65,.65,.65,0xFF66816A);
                    b.cylinder(new V(x-1,6.8,z),new V(x+1,6.8,z),.45,.45,0xFF66816A,true);
                    b.cylinder(new V(x+1,6.8,z),new V(x+1,4.8,z),.45,.4,0xFF66816A,true);
                }else{
                    for(int branch=0;branch<3;branch++){
                        double dx=Math.cos(branch*2.1)*1.3;
                        b.cylinder(new V(x-2,10,z+1),new V(x-2+dx,7.8,z+1+branch*.3),.2,.12,0xFF77816A,true);
                    }
                }
            }
        }
        for(int i=0;i<8;i++){
            double x=((i*17)%29)-14,z=((i*11)%27)-13;
            b.sphere(x,10.6,z,1.3,.35,.9,end?0xFF826A98:0xFFE4CA96);
        }
        // A subtle contact patch anchors the rig without a square plinth.
        b.sphere(0,10.55,1,4.8,.08,3,end?0xFF897C88:0xFFBA9B6A);
        return b.build();
    }
}
