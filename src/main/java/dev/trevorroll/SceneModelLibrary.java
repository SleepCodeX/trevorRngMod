package dev.trevorroll;

import static dev.trevorroll.SolidMesh.*;

/** Cached sculpted cowboy rig and two environments; the scene contains no reward token. */
public final class SceneModelLibrary {
    private SceneModelLibrary() {}
    private static final int LEATHER=0xFF79513C,DARK=0xFF3C302C,TAN=0xFFBC9365,SKIN=0xFFD5A47E,
        SHIRT=0xFFD9CCAE,DENIM=0xFF465B61,SCARF=0xFFAA5545,GOLD=0xFFE8BD72;
    public static final SolidMesh TREVOR=hunter(),HAT=hat(),ARM=upperArm(),FOREARM=forearm(),HAND=hand(),LEG=leg();
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
    private static SolidMesh hunter(){
        var b=new Builder();
        // Rounded shoulder/torso volumes and a tailored waist give the coat real depth.
        b.sphere(0,-2.0,-.15,3.45,4.15,2.5,LEATHER);
        b.sphere(0,-4.35,-.3,3.85,1.85,2.65,LEATHER);
        b.sphere(0,1.8,-.15,2.75,2.3,2.25,DARK);
        b.sphere(0,-2.2,2.0,2.2,3.65,.75,SHIRT);
        for(int side:new int[]{-1,1}){
            double x=side*2.45;
            b.sphere(x,4.1,-.85,1.45,4.7,2.05,LEATHER);
            double[][] lapel={{side*.9,-5.3},{side*2.5,-5.5},{side*2.4,-1.0},{side*.65,-2.3}};
            if(side<0){double[] a=lapel[0];lapel[0]=lapel[3];lapel[3]=a;a=lapel[1];lapel[1]=lapel[2];lapel[2]=a;}
            b.extrude(lapel,2.62,.4,0xFFB4875F);
            b.cylinder(new V(side*3,-4.2,1.5),new V(side*2.9,1.7,1.9),.16,.12,0xFFCEA77A,true);
        }
        // Open vest, neckerchief, diagonal field strap and stitched leather belt.
        b.sphere(0,-6.2,0,1.25,1.3,1.2,SKIN);
        b.sphere(0,-5.6,.2,1.65,.7,1.35,SCARF);
        b.extrude(new double[][]{{-1.5,-5.2},{1.5,-5.2},{.55,-2.9}},3.0,.32,SCARF);
        b.extrude(new double[][]{{-3.0,-5.0},{-2.35,-5.15},{2.9,2.4},{2.25,2.65}},2.9,.3,DARK);
        for(int i=0;i<3;i++)b.disc(-.35,-1.5+i*1.1,2.85,.16,.15,GOLD);
        b.box(0,2.6,.1,5.9,.9,4.7,.3,DARK);
        b.box(0,2.6,2.65,1.35,1.05,.4,.2,GOLD);
        b.box(0,2.6,2.9,.7,.5,.15,.1,DARK);
        b.sphere(-3.2,3.3,.5,.9,1.55,1.1,0xFF9B704B);
        b.box(-3.2,2.4,1.25,1.45,.6,.7,.2,TAN);
        for(int i=0;i<3;i++)b.ring(3.5,3.5+i*.18,1.3,1.25,.99,.18,0xFFC49E65);
        // A sculpted head, chin, nose, ears, eyes and brows replace the cuboid face.
        b.sphere(0,-9.75,-.1,2.5,3.05,2.35,SKIN);
        b.sphere(0,-8.0,.65,1.85,1.45,1.75,0xFFC79571);
        b.sphere(0,-11.2,-.45,2.55,1.8,2.35,0xFF4B372E);
        for(int side:new int[]{-1,1}){
            b.sphere(side*2.38,-9.8,0,.5,.78,.55,0xFFBF8D69);
            b.sphere(side*1.0,-10.2,2.08,.64,.37,.32,0xFFF5E4CA);
            b.sphere(side*1.0,-10.2,2.38,.25,.3,.12,0xFF35504A);
            b.sphere(side*1.0,-10.2,2.48,.13,.22,.055,0xFF231F23);
            b.sphere(side*1.0-.07,-10.32,2.53,.075,.07,.035,0xFFFFFFFF);
            b.cylinder(new V(side*.45,-10.82,2.25),new V(side*1.65,-10.72,2.1),.14,.17,0xFF49342C,true);
            b.sphere(side*1.85,-9.25,1.05,.34,1.15,.85,0xFF70503D);
            b.sphere(side*.53,-8.65,2.3,.65,.23,.28,0xFF594031);
        }
        b.sphere(0,-9.55,2.15,.43,.72,.95,0xFFE3B38B);
        b.sphere(0,-7.55,1.83,1.1,.4,.38,0xFF76513D);
        b.cylinder(new V(-.55,-8.07,2.22),new V(.55,-8.07,2.22),.075,.075,0xFF684133,true);
        // Small raised star badge, never a reward coin.
        double[][] star=new double[10][2];
        for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5,r=i%2==0?.6:.28;star[i]=new double[]{1.65+Math.cos(a)*r,-2.6+Math.sin(a)*r};}
        b.extrude(star,2.85,.2,GOLD);
        return b.build();
    }
    private static SolidMesh hat(){
        var b=new Builder();
        b.sphere(0,-13.05,-.1,6.1,.37,4.1,0xFFAC7E4F);
        b.sphere(0,-13.2,-.1,5.8,.25,3.95,TAN);
        for(int side:new int[]{-1,1})b.sphere(side*5.25,-13.5,-.2,.85,.62,2.4,TAN);
        b.sphere(0,-14.5,-.25,3.35,1.95,2.5,0xFFAA7B4B);
        b.sphere(0,-13.55,-.2,3.48,.48,2.62,DARK);
        b.sphere(-1.0,-15.8,-.2,.6,.35,1.8,0xFFC49D6D);
        b.sphere(1.0,-15.8,-.2,.6,.35,1.8,0xFFC49D6D);
        b.box(0,-13.65,2.37,1.1,.7,.25,.18,GOLD);
        return b.build();
    }
    private static SolidMesh upperArm(){
        var b=new Builder();b.sphere(0,.5,0,1.35,1.35,1.45,LEATHER);
        b.cylinder(new V(0,.4,0),new V(0,3.6,0),1.15,.95,LEATHER,true);
        b.sphere(0,3.6,0,.97,.8,1.03,LEATHER);return b.build();
    }
    private static SolidMesh forearm(){
        var b=new Builder();b.cylinder(new V(0,0,0),new V(0,2.8,0),.95,.68,0xFF8A6246,true);
        b.sphere(0,2.7,0,.8,.5,.88,TAN);return b.build();
    }
    private static SolidMesh hand(){
        var b=new Builder();b.sphere(0,.6,0,.72,.95,.38,0xFFD2B184);
        for(int i=0;i<4;i++){
            double x=-.48+i*.32,length=i==0||i==3?.95:1.25;
            b.cylinder(new V(x,.7,0),new V(x,1.0+length,.04),.16,.13,0xFFD2B184,true);
            b.sphere(x,1.0+length,.04,.13,.15,.16,0xFFDBBC91);
        }
        b.cylinder(new V(-.6,.3,.05),new V(-1.03,1,.18),.22,.17,0xFFD2B184,true);return b.build();
    }
    private static SolidMesh leg(){
        var b=new Builder();b.cylinder(new V(0,.4,0),new V(0,5.4,.2),1.13,.85,DENIM,true);
        b.sphere(0,3.2,.1,1.0,1.3,1.1,DENIM);
        b.cylinder(new V(0,4.8,.1),new V(0,7.0,.2),.95,1,DARK,true);
        b.sphere(0,7.1,1.0,1.05,.82,1.9,0xFF5F4031);
        b.box(0,7.75,.8,2.15,.4,3.8,.18,0xFF302924);
        b.box(0,7.7,-.45,1.7,.7,1.2,.18,0xFF3F3028);return b.build();
    }
}
