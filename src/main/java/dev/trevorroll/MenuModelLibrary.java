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
        b.roundedBox(0,0,0,24,24,15,1.0,WOOD);
        for(int face:new int[]{-1,1}){
            double z=face*7.6;
            b.roundedBox(0,0,z,23.7,23.7,1.2,.5,BRASS);
            b.roundedBox(0,0,face*8.0,20.6,20.6,.7,.35,ENAMEL);
            for(int side:new int[]{-1,1}){
                b.roundedBox(side*11,0,face*8.1,1.2,21.5,1.1,.35,GOLD);
                b.roundedBox(0,side*11,face*8.1,21.5,1.2,1.1,.35,GOLD);
                for(int other:new int[]{-1,1}){
                    b.box(side*10.3,other*10.3,face*8.0,3.4,3.4,1.8,.7,BRASS);
                    b.sphere(side*10.2,other*10.2,face*9,.62,.62,.38,LIGHT);
                }
            }
        }
        // Carved side rails remain recognizable through the full revolution.
        for(double y:new double[]{-7.5,7.5})b.roundedBox(0,y,0,24.2,1.2,11,.4,GOLD);
        for(double x:new double[]{-11.7,11.7})b.box(x,0,0,.8,8,7,.25,PURPLE);
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
        b.roundedBox(-4,-4,-3.3,18.5,14,4.8,1.1,PURPLE);
        b.roundedBox(-4,-4,-.6,16,11.6,.8,.35,INK);
        b.roundedBox(-4,-8,-.05,14.2,1.4,.5,.25,CREAM);
        for(int i=0;i<3;i++)b.sphere(-9+i*1.4,-8,.35,.38,.38,.23,i==0?GOLD:ENAMEL);
        b.roundedBox(-5,-2,.1,10,3.6,.6,.3,ENAMEL);
        b.roundedBox(4.2,5,3.1,18.8,13.3,5.4,1.0,WOOD);
        b.roundedBox(4.2,5,6,16,10.7,.9,.4,INK);
        b.roundedBox(4.2,1.9,6.55,13.4,1.4,.55,.25,GOLD);
        b.roundedBox(3.1,5.2,6.65,10.4,2.8,.7,.35,PURPLE);
        b.roundedBox(1.5,5.2,7.08,6.4,.8,.3,.13,CREAM);
        for(int x:new int[]{-1,1})for(int y:new int[]{-1,1})b.box(4.2+x*8.2,5+y*5.5,6.7,1.4,1.4,1.3,.3,LIGHT);
        double[][] cursor={{10,6},{10,13},{11.8,11.5},{13.5,14.1},{14.7,13.4},{13.1,10.9},{15.4,10.6}};
        b.extrude(cursor,8,.9,BRASS);for(var a:cursor){a[0]-=.25;a[1]-=.3;}b.extrude(cursor,8.55,.45,CREAM);
        return b.build();
    }
    private static SolidMesh coin(){
        var b=new Builder();
        b.disc(0,0,0,11.2,9.2,BRASS);
        for(int face:new int[]{-1,1}){
            b.disc(0,0,face*4.6,10.9,.9,GOLD);
            b.torus(new V(0,0,face*5.0),new V(0,0,face),10.0,1.15,.9,LIGHT);
            paw(b,0,0,face*5.75,1.55,0xFFFFDEA0);
        }
        // Broad stamped edge marks read as illustrated gold, rather than fine metal milling.
        for(int i=0;i<12;i++){
            double a=i*Math.PI/6,x=11.05*Math.cos(a),y=11.05*Math.sin(a);
            b.cylinder(new V(x,y,-3.4),new V(x,y,3.4),.38,.38,GOLD,true);
        }
        return b.build();
    }
    private static SolidMesh gramophone(){
        var b=new Builder();
        b.roundedBox(0,6.5,0,20,7.5,15,1.3,WOOD);
        b.roundedBox(0,2.5,0,20.5,1.7,15.5,.65,BRASS);
        b.roundedBox(0,1.9,0,18.8,.8,13.8,.3,GOLD);
        b.cylinder(new V(-1,1.4,1),new V(-1,.5,1),6.3,6.3,INK,true);
        b.torus(new V(-1,.45,1),new V(0,1,0),5.2,.18,.18,PURPLE);
        b.cylinder(new V(-1,.35,1),new V(-1,.05,1),1.9,1.9,PURPLE,true);
        b.sphere(-1,-.2,1,.35,.5,.35,LIGHT);
        b.cylinder(new V(6,2,-3),new V(6,-2.3,-3),.55,.55,BRASS,true);
        b.cylinder(new V(6,-2.3,-3),new V(-1,-2.3,1.8),.42,.42,GOLD,true);
        b.sphere(-1,-2,1.8,.65,.5,.8,CREAM);
        V[] curve={new V(5,1,-5),new V(5,-3,-5),new V(4,-7,-3.5),new V(1,-10,-.5),new V(-3,-12,3.5)};
        b.bell(curve,new double[]{.85,1.1,2.25,4.2,6.2},GOLD,0xFFB57D3E);
        for(double x:new double[]{-7,7})for(double z:new double[]{-4.7,4.7})b.sphere(x,10.5,z,1.55,1,1.55,BRASS);
        b.roundedBox(0,6.4,7.65,13.4,3.3,.5,.2,BRASS);
        for(int i=0;i<5;i++)b.roundedBox(-4+i*2,6.4,8,1,1.7,.3,.12,INK);
        b.sphere(8,5,7.9,.9,.9,.5,GOLD);
        return b.build();
    }
    private static SolidMesh hide(){
        var b=new Builder();
        double[][] controls={{-2.4,-8},{-5.1,-11},{-8.1,-8.7},{-6.6,-3.9},{-7.5,.2},{-6.1,4.1},{-8.7,9},{-5.8,10.6},{-1.8,8.2},{1.8,8.2},{5.8,10.6},{8.7,9},{6.1,4.1},{7.5,.2},{6.6,-3.9},{8.1,-8.7},{5.1,-11},{2.4,-8}};
        double[][] edge=new double[controls.length*4][2],inner=new double[edge.length][2];
        for(int i=0;i<edge.length;i++){
            int k=i/4;double t=(i%4)/4.,t2=t*t,t3=t2*t;
            for(int axis=0;axis<2;axis++){
                double a=controls[(k+controls.length-1)%controls.length][axis],c=controls[k][axis],d=controls[(k+1)%controls.length][axis],e=controls[(k+2)%controls.length][axis];
                edge[i][axis]=.5*((2*c)+(-a+d)*t+(2*a-5*c+4*d-e)*t2+(-a+3*c-3*d+e)*t3);
                inner[i][axis]=edge[i][axis]*.89;
            }
        }
        b.wall(edge,-1.6,edge,1.6,WOOD);b.wall(edge,1.6,inner,2.6,0xFFB28158);b.wall(inner,-2.6,edge,-1.6,WOOD);
        b.cap(inner,2.6,false,0xFFC39465);b.cap(inner,-2.6,true,0xFFAA7B52);
        b.sphere(0,.4,2.35,5.0,7.1,1.3,0xFFCF9F6B);
        // Small raised folds and stitching follow the hide's own outline.
        for(int i=0;i<edge.length;i+=6){
            int j=(i+2)%edge.length;
            b.cylinder(new V(edge[i][0]*.93,edge[i][1]*.93,2.2),new V(edge[j][0]*.93,edge[j][1]*.93,2.2),.12,.12,CREAM,true);
        }
        paw(b,0,.3,3.7,.7,0xFF9F6B44);
        return b.build();
    }
    private static SolidMesh keyboard(){
        var b=new Builder();
        b.roundedBox(0,0,0,28,14,7.3,1.35,WOOD);
        b.roundedBox(0,0,3.35,26.5,12.4,1.2,.5,PURPLE);
        b.roundedBox(-.4,0,4.0,24.7,10.7,.6,.28,INK);
        for(int row=0;row<3;row++)for(int col=0;col<7;col++){
            double x=-10.5+col*3.35,y=-3.7+row*2.8,z=4.7+(2-row)*.15;
            int color=col==0?ENAMEL:row==0?0xFFEADCC7:CREAM;
            b.roundedBox(x,y,z,2.75,2.15,1.8,.45,color);
            b.box(x,y-.25,z+.94,.85,.2,.13,.05,0xFFB8A387);
        }
        b.roundedBox(0,4.6,4.7,11.5,1.65,1.6,.5,GOLD);
        for(int i=0;i<3;i++)b.sphere(-11+i*1.6,-5.7,4.5,.35,.3,.25,i==0?GOLD:ENAMEL);
        b.disc(12,-4.1,4.8,1.15,1.8,BRASS);
        b.torus(new V(12,-4.1,5.8),new V(0,0,1),.8,.25,.25,GOLD);
        for(double x:new double[]{-9.5,9.5})for(double y:new double[]{-4.5,4.5})b.sphere(x,y,-3.7,1.6,1.2,.6,INK);
        b.roundedBox(0,-7.05,0,4.4,.5,2.4,.2,BRASS);b.roundedBox(0,-7.36,0,3,.25,1.15,.1,INK);
        return b.build();
    }
}
