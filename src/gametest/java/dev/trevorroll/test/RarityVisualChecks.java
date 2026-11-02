package dev.trevorroll;

import java.util.Random;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Exact result poses in the real renderer, including colored pelt coins and rounded text. */
public final class RarityVisualChecks {
    private RarityVisualChecks(){}
    public static void run(ClientGameTestContext ctx){
        ctx.getInput().resizeWindow(1280,720);ctx.runOnClient(mc->mc.options.guiScale().set(2));
        for(var rarity:Hunt.Rarity.values()){
            ctx.setScreen(()->new RarityScreen(rarity));ctx.waitTicks(3);
            ctx.takeScreenshot("rarity-result-"+rarity.name().toLowerCase(java.util.Locale.ROOT));
        }
        ctx.setScreen(()->null);
    }
    private static final class RarityScreen extends Screen {
        private final Reel reel;
        RarityScreen(Hunt.Rarity rarity){super(Component.literal("Rarity result preview"));reel=new Reel(new Hunt(rarity,"Overgrown Mushroom Cave"),0,1000,true,new Random(7),4500);}
        @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){
            g.fill(0,0,width,height,0xFF151B27);
            HudPainter.draw(new MinecraftCanvas(g,1,true,true),reel,1600,width,height,1,.5,.32,true);
        }
    }
}
