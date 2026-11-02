package dev.trevorroll;

import static dev.trevorroll.SolidMesh.*;

/** Each icon is its own complete object. The palette, horn and hide have open silhouettes. */
public final class MenuModelLibrary {
    private MenuModelLibrary() {}
    private static final int GOLD=0xFFE5B65C,BRASS=0xFF9E6831,LIGHT=0xFFFFE6A6,PURPLE=0xFF65508B,
        INK=0xFF222738,ENAMEL=0xFFA8D3BC,CREAM=0xFFF6EDD9,WOOD=0xFF855A40;
    private static final class Cache { static final SolidMesh[] MODELS={machine(),crest(),palette(),layout(),coin(),gramophone(),hide(),keyboard()}; }
    public static SolidMesh get(int module){return Cache.MODELS[Math.floorMod(module,8)];}
    private static void paw(Builder b,double x,double y,double z,double s,int color){
        b.sphere(x,y+2*s,z,3.1*s,2.4*s,.65*s,color);
        b.sphere(x-3.2*s,y-.9*s,z,1.25*s,1.7*s,.6*s,color);
        b.sphere(x-1.25*s,y-2.7*s,z,1.25*s,1.65*s,.6*s,color);
        b.sphere(x+1.25*s,y-2.7*s,z,1.25*s,1.65*s,.6*s,color);
        b.sphere(x+3.2*s,y-.9*s,z,1.25*s,1.7*s,.6*s,color);
    }
    private static SolidMesh machine(){
        var b=new Builder();
        b.box(0,9,0,21,4,15,.65,BRASS);b.box(0,8.7,.4,19.7,2.8,14.2,.35,PURPLE);
        b.box(-8.7,-.9,0,3,18,13,.5,PURPLE);b.box(8.7,-.9,0,3,18,13,.5,PURPLE);
        b.box(0,-1,-5.3,17,18,2,.3,INK);b.box(0,-9,0,20,4,13,.55,GOLD);
        b.box(0,-9.2,7.1,17,2.8,1.8,.25,INK);b.box(0,-9.2,8.1,13,1.1,.45,.15,LIGHT);
        for(int i=-1;i<=1;i++){
            double x=i*5.4;
            b.cylinder(new V(x-2.25,0,2.9),new V(x+2.25,0,2.9),4.5,4.5,CREAM,true);
            b.cylinder(new V(x-2.45,0,2.9),new V(x-2.1,0,2.9),4.65,4.65,BRASS,true);
            paw(b,x,0,7.5,.45,i==0?ENAMEL:PURPLE);
        }
        for(double x:new double[]{-10,10})b.box(x,-1.5,6.8,1,14,.9,.15,GOLD);
        b.box(0,5.8,6.9,18,2,1.6,.25,GOLD);b.box(0,5.8,7.8,12,.7,.3,.1,INK);
        b.box(0,8,8.3,12,1.3,3,.2,BRASS);
        b.cylinder(new V(10.4,2,0),new V(12.8,2,0),1.4,1.4,BRASS,true);
        b.cylinder(new V(12.8,2,0),new V(12.8,-6.1,0),.65,.65,GOLD,true);
        b.sphere(12.8,-7,0,1.8,1.8,1.8,0xFFB67CC8);
        for(double x:new double[]{-7,7})for(double z:new double[]{-4,4})b.box(x,11,z,3,1,3,.2,INK);
        for(int i=0;i<5;i++)b.box(0,-5+i*1.2,-6.4,9,.3,.35,.05,BRASS);
        return b.build();
    }
    private static SolidMesh crest(){
        var b=new Builder();
        b.box(0,0,0,24,24,15,.65,BRASS);
        for(int face:new int[]{-1,1}){
            b.box(0,0,face*7.25,23.5,23.5,1.4,.35,GOLD);
            b.box(0,0,face*8,20.4,20.4,.9,.25,ENAMEL);
            // The real accessory image is rendered as a decal on this solid face.
            for(double x:new double[]{-10.4,10.4})for(double y:new double[]{-10.4,10.4})b.disc(x,y,face*8,.65,.4,LIGHT);
        }
        // Raised side rails and screw heads remain geometry when the face turns away.
        for(double y:new double[]{-8,8})b.box(0,y,0,24.3,.6,10,.15,GOLD);
        return b.build();
    }
    private static SolidMesh palette(){
        var b=new Builder();b.palette(0xFFDFB98A,WOOD);
        double[][] wells={{-6,-3},{-2.5,-5.8},{2,-5.8},{6,-3.8},{-7,2.4}};
        int[] colors={0xFFC6A4F0,0xFF8ECAB0,0xFFEEBD60,0xFFE28E91,0xFF73BBCB};
        for(int i=0;i<wells.length;i++){
            double x=wells[i][0],y=wells[i][1];
            b.ring(x,y,2.35,2,1.5,.55,BRASS);b.sphere(x,y,2.55,1.55,1.55,.9,colors[i]);
        }
        return b.build();
    }
    private static SolidMesh layout(){
        var b=new Builder();
        b.box(-4,-4,-3,18,13,4,.5,PURPLE);b.box(-4,-4,-.7,16,11,.8,.3,INK);
        b.box(-4,-7,-.15,13,1.4,.6,.2,CREAM);b.box(-6,-2,-.15,8,3,.7,.2,ENAMEL);
        b.box(5,5,3,18,12,4,.5,BRASS);b.box(5,5,5.3,16,10,.8,.3,INK);
        b.box(5,2.5,5.9,13,1.4,.5,.2,GOLD);b.box(3,6.5,5.9,8,2.5,.6,.2,0xFFC7AFE4);
        return b.build();
    }
    private static SolidMesh coin(){
        var b=new Builder();b.disc(0,0,0,11.5,10.5,BRASS);
        for(int face:new int[]{-1,1}){
            b.disc(0,0,face*5.3,10.8,.7,GOLD);b.ring(0,0,face*5.9,10.8,9.5,.7,LIGHT);paw(b,0,0,face*6,1.55,BRASS);
        }
        for(int i=0;i<96;i++){
            double a=i*2*Math.PI/96,x=11.45*Math.cos(a),y=11.45*Math.sin(a);
            b.cylinder(new V(x,y,-4.4),new V(x,y,4.4),.2,.2,LIGHT,true);
        }
        return b.build();
    }
    private static SolidMesh gramophone(){
        var b=new Builder();b.box(0,6,0,19,8,14,.7,WOOD);b.box(0,1.8,0,19.5,1,14.5,.3,BRASS);
        b.cylinder(new V(-1,1.1,1),new V(-1,.1,1),6,6,INK,true);
        b.cylinder(new V(-1,.05,1),new V(-1,-.15,1),2.1,2.1,PURPLE,true);
        b.cylinder(new V(-1,-.15,1),new V(-1,-1,1),.3,.3,LIGHT,true);
        b.cylinder(new V(6,1,-4),new V(6,-3,-4),.6,.6,BRASS,true);
        b.cylinder(new V(6,-3,-4),new V(-2,-3,1),.5,.5,GOLD,true);
        b.cylinder(new V(5,1,-5),new V(5,-6,-5),1.1,1.1,GOLD,true);
        V throat=new V(5,-5,-4),mouth=new V(-4,-11,2.5);
        b.cylinder(throat,mouth,1,6,GOLD,false);b.cylinder(mouth,throat,5.55,.65,BRASS,false);
        // The lip is open, with a visible inner bell and narrow rolled rim.
        V tip=mouth.add(mouth.sub(throat).unit().mul(.35));b.cylinder(mouth,tip,6,6.1,LIGHT,false);
        for(double x:new double[]{-6.5,6.5})for(double z:new double[]{-4,4})b.box(x,10.4,z,2.8,1.5,2.8,.3,INK);
        return b.build();
    }
    private static SolidMesh hide(){
        var b=new Builder();
        double[][] shape={{-3,-8},{-5,-11},{-8,-9},{-6,-4},{-8,0},{-6,4},{-9,9},{-6,11},{-2,8},{2,8},{6,11},{9,9},{6,4},{8,0},{6,-4},{8,-9},{5,-11},{3,-8}};
        b.extrude(shape,0,5,WOOD);
        b.sphere(0,0,2.6,5.5,7,.8,0xFFBE9267);
        for(double x:new double[]{-3.5,3.5})for(double y:new double[]{-4.5,4.5})b.sphere(x,y,3.3,1.1,1.3,.3,CREAM);
        return b.build();
    }
    private static SolidMesh keyboard(){
        var b=new Builder();b.box(0,0,0,27,12,7,.7,INK);b.box(0,0,3.5,25.8,10.8,1,.5,PURPLE);
        for(int row=0;row<3;row++)for(int col=0;col<7;col++)b.box(-10.6+col*3.5,-3.4+row*2.8,4.4,2.8,2.1,1.6,.35,col==0?ENAMEL:CREAM);
        b.box(0,4.6,4.4,13,1.4,1.6,.3,GOLD);
        return b.build();
    }
}
