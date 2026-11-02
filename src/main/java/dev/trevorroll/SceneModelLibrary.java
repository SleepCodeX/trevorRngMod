package dev.trevorroll;

import static dev.trevorroll.SolidMesh.*;

/** Cached terrain and rig parts, with complete three-dimensional silhouettes. */
public final class SceneModelLibrary {
    private SceneModelLibrary() {}
    public static final SolidMesh RABBIT_BODY=rabbitBody(),RABBIT_EAR=ear(),FORELEG=foreleg(),HINDLEG=hindleg();
    public static final SolidMesh TREVOR=trevor(),ARM=arm(),LEG=leg();
    private static final SolidMesh[] TERRAIN={terrain(0),terrain(1),terrain(2),terrain(3),terrain(4)};
    public static SolidMesh terrainFor(int stage){return TERRAIN[Math.clamp(stage,0,4)];}
    private static SolidMesh terrain(int stage){
        var b=new Builder();int rock=stage<2?0xFF65574A:stage==2?0xFF9D8868:stage==3?0xFF30233F:0xFF8B6C42;
        int ground=stage==0?0xFF658E66:stage==1?0xFFC4AB75:stage<4?0xFFB8B08C:0xFFEDC785;
        b.cylinder(new V(0,11.5,0),new V(0,15,0),24,21,rock,true);
        b.cylinder(new V(0,10.6,0),new V(0,11.7,0),24.3,24,ground,true);
        for(int i=0;i<11;i++){
            double a=i*2*Math.PI/11,x=21*Math.cos(a),z=21*Math.sin(a);
            if(stage==0){
                b.box(x,7,z,2,8,2,.3,0xFF7D5944);
                b.sphere(x,1.5,z,4.1,5.3,4,stage==0?0xFF3F7758:0xFF92A065);
                b.sphere(x+1,-.7,z,2.7,3,2.7,stage==0?0xFF81AF79:0xFFB5B071);
            }else if(stage==1){
                double h=3+(i%4)*1.8;
                b.box(x,10-h/2,z,5.5,h,4.5,.6,0xFFB58759);
                if(i%3==0){b.cylinder(new V(x,10-h,z),new V(x,2-h,z),.8,1,0xFF5E8764,true);}
                else b.sphere(x+2,9-h,z,3,1.8,2.3,0xFFE7C78D);
            }else if(stage==2){
                double h=5+(i%3)*2;
                b.cylinder(new V(x,10,z),new V(x,10-h,z),.6,.9,0xFFAAA3C9,true);
                b.sphere(x,9-h,z,3.8,1.8,3.8,i%2==0?0xFF7D68AC:0xFF76A6AC);
                b.sphere(x-1,8-h,z+.5,.5,.2,.5,0xFFE9DFBD);
            }else if(stage==3){
                double h=8+(i%3)*4;b.box(x,10-h/2,z,3.4,h,3.4,.35,0xFF241D32);
                b.box(x,8-h,z,2,3,2,.15,0xFF9766B6);
            }else{
                b.box(x,7,z,3,7,3,.5,0xFFDDB778);b.sphere(x,2.8,z,1.1,1.1,1.1,0xFFFFE8A4);
            }
        }
        for(int i=0;i<14;i++){
            double x=((i*17)%35)-17,z=((i*11)%31)-15;
            b.box(x,10.2,z,2.2,.8,2,.15,stage<2?0xFF8CAC72:stage<4?0xFF8B6B9D:0xFFFFE1A0);
        }
        if(stage==4){b.cylinder(new V(0,9.8,0),new V(0,10.9,0),8,8,0xFFB28142,true);}
        return b.build();
    }
    private static SolidMesh rabbitBody(){
        var b=new Builder();b.sphere(0,0,0,3.7,3,5.4,0xFFEFE6D5);b.sphere(0,-2,4.7,2.9,2.8,2.8,0xFFFFF6E5);
        b.sphere(0,.4,-5.2,1.9,1.9,1.9,0xFFFFFFFF);
        for(int side:new int[]{-1,1}){
            b.sphere(side*2.65,-2.7,5.3,.42,.7,.7,0xFF262136);
            b.sphere(side*2.92,-2.9,5.4,.13,.2,.2,0xFFFFFFFF);
            b.sphere(side*.7,-.8,7.1,.95,.8,.65,0xFFFFFFFF);
        }
        b.sphere(0,-1.1,7.7,.48,.35,.35,0xFFD69DA7);
        return b.build();
    }
    private static SolidMesh ear(){var b=new Builder();b.sphere(0,-3.6,0,1.05,4.8,.75,0xFFF7EBD8);b.sphere(0,-3.8,.65,.6,3.8,.18,0xFFDBA0AD);return b.build();}
    private static SolidMesh foreleg(){var b=new Builder();b.sphere(0,1.8,0,.8,2.5,.9,0xFFF0E6D4);b.sphere(0,4,1,.9,.7,1.9,0xFFFFFFFF);return b.build();}
    private static SolidMesh hindleg(){var b=new Builder();b.sphere(0,1.1,-.8,1.6,2,2,0xFFE2D3BE);b.sphere(0,3.2,1.3,1.2,.8,3.2,0xFFFFF5E5);return b.build();}
    private static SolidMesh trevor(){
        var b=new Builder();b.box(0,-1,0,7,9,3.8,.2,0xFF547A58);b.box(0,-1,2.2,5.5,8,.5,.1,0xFF775339);
        b.box(0,-8,0,6.2,6.2,5.8,.18,0xFFE0B99B);b.box(0,-11.1,-.4,6.7,1.4,6.4,.18,0xFF523F32);
        b.box(0,-7.1,3,4.5,2.1,.6,.1,0xFF805C42);
        for(int side:new int[]{-1,1}){b.box(side*1.5,-8.8,3,.9,.8,.4,0,0xFFF3EEE4);b.box(side*1.5,-8.8,3.25,.45,.6,.2,0,0xFF385B46);}
        b.box(0,3.4,0,7.2,1,4,.1,0xFF332D2A);b.box(0,3.4,2.2,1.4,1.2,.4,.1,0xFFD9AD5F);
        return b.build();
    }
    private static SolidMesh arm(){var b=new Builder();b.box(0,2,0,2.5,4,2.8,.15,0xFF547A58);b.box(0,5,0,2.3,2.5,2.5,.15,0xFFDBAF8D);return b.build();}
    private static SolidMesh leg(){var b=new Builder();b.box(0,3,0,2.8,6,3,.15,0xFF675445);b.box(0,6.3,.7,3,1.6,4.1,.18,0xFF342D2B);return b.build();}
}
