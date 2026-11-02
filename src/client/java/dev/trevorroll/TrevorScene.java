package dev.trevorroll;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.monster.dragon.EnderDragonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** A private display scene: it creates no entities, blocks or biome changes in the world. */
final class TrevorScene {
    private TrevorScene() {}
    private static final Identifier DRAGON=Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon.png");
    private static EnderDragonModel dragon;
    static void submit(PoseStack p,SubmitNodeCollector nodes,double seconds,double progress){
        int stage=ElusiveSequence.stage(progress);double local=ElusiveSequence.local(progress);
        p.mulPose(Axis.XP.rotationDegrees(10));
        p.mulPose(Axis.YP.rotationDegrees((float)(168+8*Math.sin(seconds*.3))));
        int tint=stage<2?0xFFFFFFFF:stage<4?0xFFBBA0E1:0xFFFFE9B8;
        SolidModelRenderer.mesh(nodes,p,SceneModelLibrary.terrainFor(stage),tint);
        if(stage==0){
            double npcX=local<.28?-38+32*ElusiveSequence.smooth(local/.28):local>.75?-6+44*ElusiveSequence.smooth((local-.75)/.25):-6;
            p.pushPose();p.translate((float)npcX,-1,2);p.mulPose(Axis.YP.rotationDegrees(-12));
            SolidModelRenderer.mesh(nodes,p,SceneModelLibrary.TREVOR,tint);
            part(nodes,p,SceneModelLibrary.ARM,-4.5,-4.7,0,0,0,-145+15*Math.sin(seconds*12),tint);
            part(nodes,p,SceneModelLibrary.ARM,4.5,-4.7,0,0,0,8,tint);
            for(int side:new int[]{-1,1})part(nodes,p,SceneModelLibrary.LEG,side*1.8,3.8,0,0,0,0,tint);
            p.popPose();
        }else if(stage<3){
            p.pushPose();double run=(stage-1+local)/2;
            p.translate((float)(-15+30*run),(float)(4.5-1.8*Math.abs(Math.sin(seconds*22))),3);
            p.mulPose(Axis.YP.rotationDegrees((float)(-72+60*run)));
            SolidModelRenderer.mesh(nodes,p,SceneModelLibrary.RABBIT_BODY,tint);
            for(int side:new int[]{-1,1}){
                double stride=Math.sin(seconds*42+side*.9);
                part(nodes,p,SceneModelLibrary.FORELEG,side*2.1,1.5,3,55*stride,0,0,tint);
                part(nodes,p,SceneModelLibrary.HINDLEG,side*2.8,1.7,-3.5,-65*stride,0,0,tint);
                part(nodes,p,SceneModelLibrary.RABBIT_EAR,side*1.4,-3.8,4.3,8*Math.sin(seconds*17),0,side*8,tint);
            }
            p.popPose();
        }else if(stage==3){
            if(dragon==null)dragon=new EnderDragonModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ENDER_DRAGON));
            var state=new EnderDragonRenderState();state.flapTime=(float)(seconds*1.7);state.ageInTicks=(float)(seconds*20);
            state.partialTicks=1;for(int i=0;i<64;i++)state.flightHistory.record(Math.sin(seconds+i*.12)*.4,0);
            p.pushPose();p.translate(0,(float)(-8+4*local),(float)(-18+36*ElusiveSequence.smooth(local)));
            p.mulPose(Axis.YP.rotationDegrees(180));float scale=(float)(1.5+3.3*ElusiveSequence.smooth(local));p.scale(scale,scale,scale);
            nodes.submitModel(dragon,state,p,DRAGON,15728880,OverlayTexture.NO_OVERLAY,0,null);p.popPose();
        }else{
            p.pushPose();p.translate(0,-4,0);p.mulPose(Axis.ZP.rotationDegrees(20));p.mulPose(Axis.YP.rotation((float)(seconds*2.6)));
            p.scale(.75f,.75f,.75f);SolidModelRenderer.icon(nodes,p,1,0xFFFFF2CB);p.popPose();
        }
    }
    private static void part(SubmitNodeCollector nodes,PoseStack p,SolidMesh mesh,double x,double y,double z,double rx,double ry,double rz,int tint){
        p.pushPose();p.translate((float)x,(float)y,(float)z);p.mulPose(Axis.ZP.rotationDegrees((float)rz));
        p.mulPose(Axis.YP.rotationDegrees((float)ry));p.mulPose(Axis.XP.rotationDegrees((float)rx));
        SolidModelRenderer.mesh(nodes,p,mesh,tint);p.popPose();
    }
}
