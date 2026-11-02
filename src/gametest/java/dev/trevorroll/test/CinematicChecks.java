package dev.trevorroll;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.opengl.GL11;

/** True native rendering plus actual command/style gating and timeline completion. */
public final class CinematicChecks {
    private CinematicChecks() {}
    public static void run(ClientGameTestContext ctx){
        var r=ctx.computeOnClient(mc->TrevorRollClient.settings());
        var oldStyle=r.elusiveStyle;boolean oldEnabled=r.elusiveCinematic,oldReward=r.elusiveReward,oldSound=r.sound;
        int oldDuration=r.durationMillis,oldCinematicDuration=r.elusiveCinematicMillis;
        try {
            ctx.getInput().resizeWindow(1280,720);ctx.runOnClient(mc->{mc.options.guiScale().set(2);r.elusiveCinematic=true;r.elusiveReward=true;r.sound=false;r.durationMillis=1000;r.elusiveCinematicMillis=5000;mc.gui.setScreen(null);});
            for(var style:ElusiveReward.Style.values()){
                ctx.runOnClient(mc->{r.elusiveStyle=style;command(mc,"trevorroll test elusive");
                    require(reel().durationMillis==(style==ElusiveReward.Style.JACKPOT?5000:1000),"Style gates actual roll duration");});
                long before=SolidModelRenderer.renderedCinematics;ctx.waitTicks(4);
                if(style!=ElusiveReward.Style.JACKPOT)require(SolidModelRenderer.renderedCinematics==before,"Minimal/Classic/Extra never render the cinematic");
                ctx.runOnClient(mc->command(mc,"trevorroll skip"));
            }
            ctx.runOnClient(mc->r.elusiveStyle=ElusiveReward.Style.JACKPOT);
            for(var rarity:Hunt.Rarity.values())if(rarity!=Hunt.Rarity.ELUSIVE){
                ctx.runOnClient(mc->{command(mc,"trevorroll test "+rarity.name().toLowerCase(java.util.Locale.ROOT));require(reel().durationMillis==1000,"Other rarities keep normal timing in Extreme mode");});
                long before=SolidModelRenderer.renderedCinematics;ctx.waitTicks(3);
                require(SolidModelRenderer.renderedCinematics==before,"Other rarities never render cinematic meshes");
                ctx.runOnClient(mc->command(mc,"trevorroll skip"));
            }
            long before=SolidModelRenderer.renderedCinematics;
            ctx.runOnClient(mc->command(mc,"trevorroll test elusive"));ctx.waitTicks(8);
            require(SolidModelRenderer.renderedCinematics>before,"Extreme Elusive reaches the real native 3D renderer");
            ctx.waitTicks(110);
            ctx.runOnClient(mc->{require(reel()==null||reel().finished(System.nanoTime()/1_000_000),"Real cinematic roll completes");command(mc,"trevorroll skip");
                r.elusiveCinematic=false;command(mc,"trevorroll test elusive");require(reel().durationMillis==1000,"Cinematic toggle preserves the normal reel");command(mc,"trevorroll skip");});
            // Fixed timeline samples use the actual client/GPU renderer for reviewable chapters.
            for(int stage=0;stage<5;stage++){
                final double progress=(stage+.55)/5;
                ctx.setScreen(()->new SceneScreen(progress));ctx.waitTicks(4);
                ctx.runOnClient(mc->require(GL11.glGetError()==GL11.GL_NO_ERROR,"No GPU error in cinematic chapter"));
                ctx.takeScreenshot("elusive-scene-stage-"+stage);
            }
            for(int frame=0;frame<8;frame++){
                final double progress=.23+frame*.013;
                ctx.setScreen(()->new SceneScreen(progress));ctx.waitTicks(2);
                ctx.takeScreenshot("elusive-rabbit-motion-"+frame);
            }
            System.out.println("PASS: Extreme-only and Elusive-only live command/HUD routing, other styles, completion, toggle, five native scene chapters and rabbit poses");
        }finally{
            ctx.runOnClient(mc->{mc.gui.setScreen(null);command(mc,"trevorroll skip");r.elusiveStyle=oldStyle;r.elusiveCinematic=oldEnabled;r.elusiveReward=oldReward;r.sound=oldSound;r.durationMillis=oldDuration;r.elusiveCinematicMillis=oldCinematicDuration;r.save();});
        }
    }
    private static Reel reel(){try{var f=TrevorRollClient.class.getDeclaredField("instance");f.setAccessible(true);var instance=f.get(null);f=TrevorRollClient.class.getDeclaredField("reel");f.setAccessible(true);return (Reel)f.get(instance);}catch(Exception e){throw new AssertionError(e);}}
    private static void command(Minecraft mc,String text){try{ClientCommands.getActiveDispatcher().execute(text,(FabricClientCommandSource)mc.player.connection.getSuggestionsProvider());}catch(CommandSyntaxException e){throw new AssertionError(e);}}
    private static void require(boolean value,String text){if(!value)throw new AssertionError(text);}
    private static final class SceneScreen extends Screen {
        private final double progress;
        SceneScreen(double progress){super(Component.literal("Elusive chapter preview"));this.progress=progress;}
        @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){ElusiveCinematicHud.drawFrame(g,progress,progress*8.5,width,height,1,"Overgrown Mushroom Cave");}
        @Override public boolean isPauseScreen(){return false;}
    }
}
