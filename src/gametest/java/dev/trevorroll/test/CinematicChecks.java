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
            boolean handoff=false;
            for(int tick=0;tick<160;tick++){
                if(ctx.computeOnClient(mc->reel()==null&&TrevorRollClient.isElusiveRewardActive())){handoff=true;break;}
                ctx.waitTick();
            }
            require(handoff,"Extreme completes directly into its dragon reward");
            ctx.runOnClient(mc->{require(reward().style()==ElusiveReward.Style.JACKPOT&&reward().sceneSecondsOffset()==5,"Dragon handoff retains the cinematic clock");});
            ctx.waitTicks(2);ctx.takeScreenshot("extreme-live-dragon-reward");
            ctx.runOnClient(mc->{require(reel()==null,"Extreme never leaves a coin result reel after completion");command(mc,"trevorroll skip");
                r.elusiveCinematic=false;command(mc,"trevorroll test elusive");require(reel().durationMillis==1000,"Cinematic toggle preserves the normal reel");});
            ctx.waitTicks(25);
            ctx.runOnClient(mc->{require(reel()==null&&reward()!=null&&reward().style()==ElusiveReward.Style.JACKPOT,"Disabling the intro still ends on the dragon, never a coin result");command(mc,"trevorroll skip");});
            // Fixed timeline samples use the actual client/GPU renderer for reviewable chapters.
            double[] phases={.2,.6,.92};
            for(int stage=0;stage<phases.length;stage++){
                final double progress=phases[stage];
                ctx.setScreen(()->new SceneScreen(progress));ctx.waitTicks(4);
                ctx.runOnClient(mc->require(GL11.glGetError()==GL11.GL_NO_ERROR,"No GPU error in cinematic chapter"));
                ctx.takeScreenshot("elusive-scene-stage-"+stage);
            }
            // Time-sampled native GPU frames for a complete, reproducible visual sequence.
            // Fixed times avoid encoding capture/IO delays as animation stutter in media.
            for(int frame=0;frame<85;frame++){
                final double progress=(frame+.5)/85;
                ctx.setScreen(()->new SceneScreen(progress));ctx.waitTick();
                ctx.takeScreenshot("elusive-journey-"+String.format(java.util.Locale.ROOT,"%02d",frame));
            }
            ctx.getInput().resizeWindow(960,720);ctx.runOnClient(mc->mc.options.guiScale().set(3));
            for(double progress:new double[]{.2,.92}){
                ctx.setScreen(()->new SceneScreen(progress));ctx.waitTicks(3);
                ctx.takeScreenshot(progress<.38?"cowboy-compact":"dragon-reveal-compact");
            }
            System.out.println("PASS: Extreme-only routing, cowboy/dragon/reveal scenes, seamless dragon reward handoff and no coin result reel");
        }finally{
            ctx.runOnClient(mc->{mc.gui.setScreen(null);command(mc,"trevorroll skip");r.elusiveStyle=oldStyle;r.elusiveCinematic=oldEnabled;r.elusiveReward=oldReward;r.sound=oldSound;r.durationMillis=oldDuration;r.elusiveCinematicMillis=oldCinematicDuration;r.save();});
        }
    }
    private static Reel reel(){try{var f=TrevorRollClient.class.getDeclaredField("instance");f.setAccessible(true);var instance=f.get(null);f=TrevorRollClient.class.getDeclaredField("reel");f.setAccessible(true);return (Reel)f.get(instance);}catch(Exception e){throw new AssertionError(e);}}
    private static ElusiveReward reward(){try{var f=TrevorRollClient.class.getDeclaredField("instance");f.setAccessible(true);var instance=f.get(null);f=TrevorRollClient.class.getDeclaredField("reward");f.setAccessible(true);return (ElusiveReward)f.get(instance);}catch(Exception e){throw new AssertionError(e);}}
    private static void command(Minecraft mc,String text){try{ClientCommands.getActiveDispatcher().execute(text,(FabricClientCommandSource)mc.player.connection.getSuggestionsProvider());}catch(CommandSyntaxException e){throw new AssertionError(e);}}
    private static void require(boolean value,String text){if(!value)throw new AssertionError(text);}
    private static final class SceneScreen extends Screen {
        private final double progress;
        SceneScreen(double progress){super(Component.literal("Elusive chapter preview"));this.progress=progress;}
        @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){ElusiveCinematicHud.drawFrame(g,progress,progress*8.5,width,height,1,"Overgrown Mushroom Cave");}
        @Override public boolean isPauseScreen(){return false;}
    }
}
