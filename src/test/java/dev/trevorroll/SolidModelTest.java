package dev.trevorroll;

/** Geometry and rarity/style policy checks independent of the GPU. */
public final class SolidModelTest {
    public static void main(String[] args){
        // The user explicitly retained these two visual anchors from 1.9.0.
        require(signature(MenuModelLibrary.get(0)).equals("aa6015d46b99f4ac8201595b111a0f7875cb222c6a202b5d336a577702bdadfd"),"Roll machine geometry/materials stay unchanged");
        require(signature(MenuModelLibrary.get(2)).equals("5c4328206ad7495beeb8a3db8d62a7ede661715b39b347693a40a6183143aa6a"),"Paint palette geometry/materials stay unchanged");
        for(int model=0;model<8;model++){
            var mesh=MenuModelLibrary.get(model);require(mesh.vertices().length>100,"A complete model has real geometry");
            require(mesh.vertices().length%4==0,"Native quad stream is aligned");
            double minZ=Double.POSITIVE_INFINITY,maxZ=-minZ;
            for(var v:mesh.vertices()){
                require(Float.isFinite(v.x())&&Float.isFinite(v.y())&&Float.isFinite(v.z()),"Finite mesh coordinates");
                double length=v.nx()*v.nx()+v.ny()*v.ny()+v.nz()*v.nz();
                require(Math.abs(length-1)<1e-4,"Unit normals support consistent native lighting");
                minZ=Math.min(minZ,v.z());maxZ=Math.max(maxZ,v.z());
            }
            require(maxZ-minZ>=3,"Model is a solid object with thickness");
            if(model==1)require(maxZ-minZ>=15,"Crest is at least three times its previous depth");
        }
        var palette=MenuModelLibrary.get(2);require(!rayHits(palette,5.4,3),"Palette thumb hole is open through the actual mesh");
        require(rayHits(palette,-3,0),"Palette body remains solid around the hole");
        require(!rayHits(palette,11,8),"Palette has its own silhouette, without a square carrier");
        for(var rarity:Hunt.Rarity.values())for(var style:ElusiveReward.Style.values())for(boolean enabled:new boolean[]{false,true}){
            boolean expected=enabled&&rarity==Hunt.Rarity.ELUSIVE&&style==ElusiveReward.Style.JACKPOT;
            require(ElusiveSequence.eligible(rarity,enabled,style)==expected,"Only Extreme Elusive selects the cinematic");
            require(ElusiveSequence.duration(rarity,enabled,1200,8500,style)==(expected?8500:1200),"Other modes preserve normal timing");
        }
        require((PeltCoins.color(Hunt.Rarity.TRACKABLE)>>16&255)-(PeltCoins.color(Hunt.Rarity.TRACKABLE)&255)<30,"Trackable coin is neutral gray");
        require((PeltCoins.color(Hunt.Rarity.ENDANGERED)&255)>(PeltCoins.color(Hunt.Rarity.ENDANGERED)>>8&255),"Endangered coin is purple");
        require((PeltCoins.color(Hunt.Rarity.ELUSIVE)>>16&255)>(PeltCoins.color(Hunt.Rarity.ELUSIVE)&255),"Elusive coin is golden");
        require(!PeltCoins.tagline(Hunt.Rarity.ELUSIVE).isBlank(),"Elusive has its own reveal slogan");
        double[] midpoints={ElusiveSequence.DRAGON_START/2,(ElusiveSequence.DRAGON_START+ElusiveSequence.REVEAL_START)/2,(ElusiveSequence.REVEAL_START+1)/2};
        for(int stage=0;stage<3;stage++){
            double p=midpoints[stage];require(ElusiveSequence.stage(p)==stage,"Hunter, dragon and held reveal remain reachable");
            require(Math.abs(ElusiveSequence.local(p)-.5)<1e-8,"Each chapter has a stable local clock");
        }
        require(ElusiveSequence.stage(1)==2&&ElusiveSequence.stage(-1)==0,"Timeline bounds clamp");
        var beth=BethModel.get();var pose=beth.newPose();
        require(beth.vertexCount()>2000&&beth.vertexCount()<5000&&beth.jointCount()>20,"The complete textured KayKit rig is present within its rendering budget");
        require(beth.triangleCount()>3000&&beth.triangleCount()<6000,"Only the selected character and cape are included, without pack weapons");
        double motion=0;float[] previous=null;
        for(int i=0;i<=120;i++){
            beth.animate(pose,i/60.,i/120.);
            for(int v=0;v<pose.vertices.length;v+=6){
                for(int c=0;c<6;c++)require(Float.isFinite(pose.vertices[v+c]),"Skinning remains finite across every greeting phase");
                require(Math.abs(pose.vertices[v])<40&&Math.abs(pose.vertices[v+1])<50&&Math.abs(pose.vertices[v+2])<40,"Animated vertices remain inside the scene budget");
                double normal=0;for(int c=3;c<6;c++)normal+=pose.vertices[v+c]*pose.vertices[v+c];
                require(Math.abs(normal-1)<.001,"Blended animated normals remain unit length");
                if(previous!=null)motion+=Math.abs(pose.vertices[v]-previous[v])+Math.abs(pose.vertices[v+1]-previous[v+1]);
            }
            previous=pose.vertices.clone();
        }
        require(motion>100,"Authored animation actually moves the selected rig");
        double change=ElusiveSequence.DRAGON_START;
        require(ElusiveSequence.caption(change-1e-9).equals("beth notices you"),"Beth's exact requested caption stays until the model changes");
        require(ElusiveSequence.caption(change).equals("ELUSIVE MOB FOUND")&&ElusiveSequence.caption(1).equals("ELUSIVE MOB FOUND"),"The dragon heading persists through the reward");
        require(ElusiveSequence.ROAR_PROGRESS==change,"The roar starts on the same transition as the dragon and heading");
        require(ElusiveSequence.progress(-100,1000)==0&&ElusiveSequence.progress(Long.MAX_VALUE,1000)==1,"Elapsed-time bounds clamp");
        System.out.println("PASS: solid mesh geometry, open palette silhouette, crest depth, and Extreme/Elusive-only cinematic policy");
    }
    private static boolean rayHits(SolidMesh mesh,double x,double y){
        var a=mesh.vertices();for(int i=0;i<a.length;i+=4){
            if(inside(a[i],a[i+1],a[i+2],x,y)||inside(a[i],a[i+2],a[i+3],x,y))return true;
        }return false;
    }
    private static boolean inside(SolidMesh.Vertex a,SolidMesh.Vertex b,SolidMesh.Vertex c,double x,double y){
        double det=(b.y()-c.y())*(a.x()-c.x())+(c.x()-b.x())*(a.y()-c.y());
        if(Math.abs(det)<1e-10)return false;
        double u=((b.y()-c.y())*(x-c.x())+(c.x()-b.x())*(y-c.y()))/det;
        double v=((c.y()-a.y())*(x-c.x())+(a.x()-c.x())*(y-c.y()))/det;
        return u>1e-7&&v>1e-7&&u+v<1-1e-7;
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static String signature(SolidMesh mesh){
        try{
            var digest=java.security.MessageDigest.getInstance("SHA-256");var b=java.nio.ByteBuffer.allocate(28);
            for(var v:mesh.vertices()){
                b.clear();b.putFloat(v.x()).putFloat(v.y()).putFloat(v.z()).putFloat(v.nx()).putFloat(v.ny()).putFloat(v.nz()).putInt(v.color());digest.update(b.array());
            }
            return java.util.HexFormat.of().formatHex(digest.digest());
        }catch(java.security.NoSuchAlgorithmException e){throw new AssertionError(e);}
    }
}
