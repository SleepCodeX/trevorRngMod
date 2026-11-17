package dev.trevorroll;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.trevorroll.mixin.PipTextureAccess;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import com.mojang.math.Axis;

/** Native Blaze3D lit mesh rendering, real depth attachment, and 2x linear downsampling. */
public final class SolidModelRenderer extends PictureInPictureRenderer<SolidModelState> {
    static final Identifier WHITE=Identifier.fromNamespaceAndPath("trevorroll","textures/gui/model_white.png");
    static long renderedModels,renderedCinematics;
    private static final Identifier CREST=Identifier.fromNamespaceAndPath("trevorroll","textures/gui/trapper_accessory.png");
    public static void register(){PictureInPictureRendererRegistry.register(context->new SolidModelRenderer());}
    @Override public Class<SolidModelState> getRenderStateClass(){return SolidModelState.class;}
    @Override protected float getTranslateY(int height,int guiScale){return height/2f;}
    @Override protected String getTextureLabel(){return "Trevor solid models";}
    @Override protected void renderToTexture(SolidModelState s,PoseStack poses,SubmitNodeCollector nodes){
        Minecraft.getInstance().gameRenderer.lighting().setupFor(s.model()==8&&ElusiveSequence.stage(s.progress())>=1
            ?Lighting.Entry.ITEMS_3D:Lighting.Entry.PLAYER_SKIN);
        poses.mulPose(Axis.XP.rotationDegrees(22));
        if(s.model()==8){renderedCinematics++;TrevorScene.submit(poses,nodes,s.seconds(),s.progress());return;}
        renderedModels++;
        poses.mulPose(Axis.ZP.rotationDegrees(s.model()==1?32:s.model()==2?-13:0));
        poses.mulPose(Axis.YP.rotation((float)(Math.PI+s.angle())));
        icon(nodes,poses,s.model(),0xFFFFFFFF);
    }
    static void icon(SubmitNodeCollector nodes,PoseStack poses,int model,int tint){
        mesh(nodes,poses,MenuModelLibrary.get(model),tint);
        if(model!=1)return;
        nodes.submitCustomGeometry(poses,RenderTypes.entityCutout(CREST),(pose,buffer)->{
            for(int face:new int[]{-1,1}){
                float z=face*8.55f;
                float[] xs=face>0?new float[]{-9.5f,9.5f,9.5f,-9.5f}:new float[]{9.5f,-9.5f,-9.5f,9.5f};
                float[] ys={-9.5f,-9.5f,9.5f,9.5f};
                for(int i=0;i<4;i++)buffer.addVertex(pose,xs[i],ys[i],z).setColor(tint)
                    .setUv(i==1||i==2?1:0,i>=2?1:0).setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(15728880).setNormal(pose,0,0,face);
            }
        });
    }
    static void mesh(SubmitNodeCollector nodes,PoseStack poses,SolidMesh mesh,int tint){
        nodes.submitCustomGeometry(poses,RenderTypes.entitySolid(WHITE),(pose,buffer)->{
            for(var v:mesh.vertices()) {
                int color=multiply(v.color(),tint);
                buffer.addVertex(pose,v.x(),v.y(),v.z()).setColor(color).setUv(.5f,.5f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose,v.nx(),v.ny(),v.nz());
            }
        });
    }
    private static int multiply(int a,int b){
        return 0xFF000000|(((a>>16&255)*(b>>16&255)/255)<<16)|(((a>>8&255)*(b>>8&255)/255)<<8)|((a&255)*(b&255)/255);
    }
    @Override protected void blitTexture(SolidModelState s,GuiRenderState output){
        var texture=((PipTextureAccess)(Object)this).trevorroll$textureView();
        int channel=(int)Math.round(255*Math.clamp(s.opacity(),0,1));
        int color=channel*0x01010101; // Premultiplied alpha preserves smooth menu/scene fades.
        output.addBlitToCurrentLayer(new BlitRenderState(RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(texture,RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR)),
            s.pose(),s.x0(),s.y0(),s.x1(),s.y1(),0,1,1,0,color,s.scissorArea(),s.bounds()));
    }
}
