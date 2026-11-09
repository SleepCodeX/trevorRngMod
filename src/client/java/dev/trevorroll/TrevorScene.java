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

/** Private Beth-to-dragon display scene; no entities or world mutations. */
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
        if(stage==0)beth(p,nodes,seconds,local);
        else dragon(p,nodes,seconds,stage==1?ElusiveSequence.smooth(local):1);
    }
    private static void beth(PoseStack p,SubmitNodeCollector nodes,double seconds,double local){
        double x=local<.2?-34+34*ElusiveSequence.smooth(local/.2):local>.8?38*ElusiveSequence.smooth((local-.8)/.2):0;
        p.pushPose();p.translate((float)x,10.6,1);p.mulPose(Axis.YP.rotationDegrees(-10));
        BethRenderer.submit(p,nodes,seconds,local);p.popPose();
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
