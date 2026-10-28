package dev.trevorroll;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

/** Real client trigger gating, cleanup, all four render paths and persisted controls. */
public final class ElusiveRewardChecks {
    private ElusiveRewardChecks() {}
    public static void run(ClientGameTestContext ctx) throws Exception {
        ctx.runOnClient(mc -> {
            var c=TrevorRollClient.settings();c.durationMillis=1000;c.elusiveReward=true;c.elusiveMillis=2000;
        });
        for(var rarity:Hunt.Rarity.values()) {
            command(ctx,"trevorroll test "+rarity.name().toLowerCase(java.util.Locale.ROOT));
            ctx.waitTicks(25);
            ctx.runOnClient(mc -> require(TrevorRollClient.isElusiveRewardActive()==(rarity==Hunt.Rarity.ELUSIVE),"Elusive-only live trigger: "+rarity));
            command(ctx,"trevorroll skip");
            ctx.runOnClient(mc -> require(!TrevorRollClient.isElusiveRewardActive(),"Skip clears reward"));
        }
        for(var style:ElusiveReward.Style.values()) {
            ctx.runOnClient(mc -> { TrevorRollClient.settings().elusiveStyle=style; mc.gui.hud.getChat().clearMessages(false); });
            command(ctx,"trevormod preview");ctx.waitTicks(10);
            ctx.runOnClient(mc -> require(TrevorRollClient.isElusiveRewardActive(),"Style preview active"));
            ctx.takeScreenshot("elusive-reward-"+style.name().toLowerCase(java.util.Locale.ROOT));
            command(ctx,"trevorroll skip");
        }
        for(var sound:RewardSound.values()){
            ctx.runOnClient(mc -> {TrevorRollClient.settings().elusiveSoundStyle=sound;TrevorRollClient.previewRewardSound();});
            ctx.waitTicks(12);
        }
        ctx.runOnClient(mc -> {TrevorRollClient.settings().elusiveSoundStyle=RewardSound.DRAGON;TrevorRollClient.settings().elusiveReward=false;});
        command(ctx,"trevorroll test elusive");ctx.waitTicks(25);
        ctx.runOnClient(mc -> require(!TrevorRollClient.isElusiveRewardActive(),"Automatic rewards can be disabled"));
        command(ctx,"trevorroll skip");
        command(ctx,"trevormod preview");ctx.waitTicks(2);
        ctx.runOnClient(mc -> require(TrevorRollClient.isElusiveRewardActive(),"Explicit preview works while disabled"));
        command(ctx,"trevorroll skip");
        ctx.runOnClient(mc -> {
            var c=TrevorRollClient.settings();c.elusiveMillis=2500;c.elusiveIntensity=.5;c.elusiveVolume=.3;c.elusiveStyle=ElusiveReward.Style.CLASSIC;c.save();
            var loaded=RollConfig.load();require(loaded.elusiveMillis==2500&&loaded.elusiveIntensity==.5&&loaded.elusiveVolume==.3&&loaded.elusiveStyle==ElusiveReward.Style.CLASSIC,"Reward controls persist");
            c.elusiveReward=true;c.elusiveMillis=1500;c.save();
        });
        command(ctx,"trevormod preview");ctx.waitTicks(40);
        ctx.runOnClient(mc -> require(!TrevorRollClient.isElusiveRewardActive(),"Reward expires independently"));
        ctx.runOnClient(mc -> { var c=TrevorRollClient.settings();c.elusiveMillis=3500;c.elusiveIntensity=.8;c.elusiveVolume=.7;c.elusiveStyle=ElusiveReward.Style.EXTRA;c.save(); });
        System.out.println("PASS: Elusive-only reward trigger, four styles, disable, preview, skip, duration and saved settings");
    }
    private static void command(ClientGameTestContext ctx,String text) throws Exception {
        ctx.runOnClient(mc -> require(ClientCommands.getActiveDispatcher().execute(text,(FabricClientCommandSource)mc.player.connection.getSuggestionsProvider())==1,text));
    }
    private static void require(boolean ok,String message) {if(!ok)throw new AssertionError(message);}
}
