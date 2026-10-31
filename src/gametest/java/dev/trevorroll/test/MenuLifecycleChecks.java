package dev.trevorroll;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.input.KeyEvent;

/** Actual screen lifecycle: closing fades retain the screen, then release it once. */
public final class MenuLifecycleChecks {
    private MenuLifecycleChecks() {}
    public static void run(ClientGameTestContext ctx) {
        boolean old=ctx.computeOnClient(mc->TrevorRollClient.settings().menuAnimations);
        try {
            ctx.runOnClient(mc->{TrevorRollClient.settings().menuAnimations=true;mc.gui.setScreen(new TrevorSettingsScreen());});
            ctx.waitTicks(8);
            final net.minecraft.client.gui.components.AbstractWidget[] row={null};
            final int[] oldY={0};
            ctx.runOnClient(mc->{
                var screen=(TrevorSettingsScreen)mc.gui.screen();screen.selectModule(0);
                row[0]=screen.children().stream().filter(net.minecraft.client.gui.components.AbstractWidget.class::isInstance)
                    .map(net.minecraft.client.gui.components.AbstractWidget.class::cast)
                    .filter(w->w.getMessage().getString().startsWith("Roll duration:")).findFirst().orElseThrow();
                oldY[0]=row[0].getY();
                screen.mouseScrolled(screen.width/2.,(screen.viewportTop()+screen.viewportBottom())/2.,0,-1);
                require(row[0].getY()==oldY[0],"Wheel starts a continuous transition rather than jumping");
            });
            ctx.waitTicks(8);
            ctx.runOnClient(mc->{
                require(row[0].getY()<oldY[0],"Real rendered frames advance smooth wheel scrolling");
                var screen=(TrevorSettingsScreen)mc.gui.screen();screen.onClose();
                require(mc.gui.screen()==screen,"Close keeps the real menu during its fade");
                require(screen.keyPressed(new KeyEvent(258,0,0)),"Closing consumes input");
                screen.onClose(); // A second Escape must not restart or double-close the fade.
            });
            ctx.waitTicks(8);
            ctx.runOnClient(mc->{
                require(mc.gui.screen()==null,"Close completes and returns to gameplay");
                require(MenuFont.opacity()==1,"Rendering restores shared text opacity");
                TrevorRollClient.settings().menuAnimations=false;
                var screen=new TrevorSettingsScreen();mc.gui.setScreen(screen);screen.onClose();
                require(mc.gui.screen()==null,"Reduced motion closes immediately");
            });
            System.out.println("PASS: animated close, repeated Escape, input guard, opacity cleanup and instant reduced motion");
        } finally {ctx.runOnClient(mc->{TrevorRollClient.settings().menuAnimations=old;mc.gui.setScreen(null);});}
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
