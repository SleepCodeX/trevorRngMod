package dev.trevorroll;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Native, lit, textured rendering of the selected CC0 KayKit character. */
final class BethRenderer {
    private BethRenderer() {}
    private static final Identifier TEXTURE=Identifier.fromNamespaceAndPath("trevorroll","textures/gui/beth.png");
    private static final class Loaded {
        static final BethModel MODEL=BethModel.get();
        static final BethModel.Pose POSE=MODEL.newPose();
    }
    static void submit(PoseStack poses,SubmitNodeCollector nodes,double seconds,double local){
        nodes.submitCustomGeometry(poses,RenderTypes.entityCutout(TEXTURE),(pose,buffer)->{
            // Sampling happens inside the ordered render callback. The reused scratch arrays
            // never escape into another frame's extracted state or deferred draw command.
            var model=Loaded.MODEL;var frame=Loaded.POSE;model.animate(frame,seconds,local);
            for(int i=0;i<model.indices.length;i+=3)for(int corner=0;corner<4;corner++){
                int vertex=model.indices[i+Math.min(corner,2)],at=vertex*6,uv=vertex*8+6;
                buffer.addVertex(pose,frame.vertices[at],frame.vertices[at+1],frame.vertices[at+2]).setColor(0xFFFFFFFF)
                    .setUv(model.vertices[uv],model.vertices[uv+1]).setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(15728880).setNormal(pose,frame.vertices[at+3],frame.vertices[at+4],frame.vertices[at+5]);
            }
        });
    }
}
