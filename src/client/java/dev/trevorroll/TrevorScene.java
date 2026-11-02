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

/** Private hunter-to-dragon display scene; no entities or world mutations. */
final class TrevorScene {
    private TrevorScene() {}
    private static final Identifier DRAGON=Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon.png");
    private static EnderDragonModel dragon;
    static void submit(PoseStack p,SubmitNodeCollector nodes,double seconds,double progress){
        int stage=ElusiveSequence.stage(progress);double local=ElusiveSequence.local(progress);
        p.mulPose(Axis.XP.rotationDegrees(10));
        p.mulPose(Axis.YP.rotationDegrees((float)(168+4*Math.sin(seconds*.25))));
        int tint=stage==0?0xFFFFE7C6:stage==1?0xFFD6C3EC:0xFFEAD6E4;
        SolidModelRenderer.mesh(nodes,p,SceneModelLibrary.terrainFor(stage),tint);
        if(stage==0)hunter(p,nodes,seconds,local,tint);
        else dragon(p,nodes,seconds,stage==1?ElusiveSequence.smooth(local):1);
    }
    private static void hunter(PoseStack p,SubmitNodeCollector nodes,double seconds,double local,int tint){
        double x=local<.2?-34+34*ElusiveSequence.smooth(local/.2):local>.8?38*ElusiveSequence.smooth((local-.8)/.2):0;
        double salute=ElusiveSequence.smooth((local-.25)/.16)*(1-ElusiveSequence.smooth((local-.66)/.16));
        boolean walking=local<.2||local>.8;
        p.pushPose();p.translate(0,10.6,0);p.scale(1.2f,1.2f,1.2f);p.translate(0,-10.6,0);
        p.translate((float)x,-.3,1);p.mulPose(Axis.YP.rotationDegrees(-10));
        p.mulPose(Axis.ZP.rotationDegrees((float)(.7*Math.sin(seconds*2))));
        SolidModelRenderer.mesh(nodes,p,SceneModelLibrary.TREVOR,tint);
        p.pushPose();p.mulPose(Axis.XP.rotationDegrees((float)(-6*salute)));
        SolidModelRenderer.mesh(nodes,p,SceneModelLibrary.HAT,tint);p.popPose();
        arm(p,nodes,-3.5,-4.7,7,-19,tint);
        arm(p,nodes,3.5,-4.7,-8-162*salute,5*salute,tint);
        for(int side:new int[]{-1,1}){
            p.pushPose();p.translate(side*1.4,2.8,0);
            p.mulPose(Axis.XP.rotationDegrees((float)(walking?15*Math.sin(seconds*7+side*Math.PI/2):0)));
            SolidModelRenderer.mesh(nodes,p,SceneModelLibrary.LEG,tint);p.popPose();
        }
        p.popPose();
    }
    private static void arm(PoseStack p,SubmitNodeCollector nodes,double x,double y,double shoulder,double elbow,int tint){
        p.pushPose();p.translate((float)x,(float)y,0);p.mulPose(Axis.ZP.rotationDegrees((float)shoulder));
        SolidModelRenderer.mesh(nodes,p,SceneModelLibrary.ARM,tint);p.translate(0,3.6,0);
        p.mulPose(Axis.ZP.rotationDegrees((float)elbow));SolidModelRenderer.mesh(nodes,p,SceneModelLibrary.FOREARM,tint);
        p.translate(0,2.9,0);SolidModelRenderer.mesh(nodes,p,SceneModelLibrary.HAND,tint);p.popPose();
    }
    private static void dragon(PoseStack p,SubmitNodeCollector nodes,double seconds,double approach){
        if(dragon==null)dragon=new EnderDragonModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ENDER_DRAGON));
        var state=new EnderDragonRenderState();state.flapTime=(float)(seconds*1.7);state.ageInTicks=(float)(seconds*20);
        state.partialTicks=1;for(int i=0;i<64;i++)state.flightHistory.record(Math.sin(seconds+i*.12)*.4,0);
        p.pushPose();p.translate(0,(float)(-2+4*approach),(float)(-18+30*approach));
        p.mulPose(Axis.YP.rotationDegrees(180));float scale=(float)(1.5+1.85*approach);p.scale(scale,scale,scale);
        nodes.submitModel(dragon,state,p,DRAGON,15728880,OverlayTexture.NO_OVERLAY,0,null);p.popPose();
    }
}
