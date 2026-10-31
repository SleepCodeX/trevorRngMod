package dev.trevorroll;

/** Deterministic opening/rotation checks independent of frame rate and game startup. */
public final class MenuMotionTest {
    public static void main(String[] args) {
        require(MenuMotion.openingOpacity(0, true) == 0, "Fade starts transparent");
        require(MenuMotion.openingOpacity(-1, true) == 0, "Negative elapsed time is clamped");
        require(MenuMotion.openingOpacity(MenuMotion.OPEN_NANOS, true) == 1, "Fade ends exactly");
        require(MenuMotion.openingOpacity(Long.MAX_VALUE, true) == 1, "Old screens remain visible");
        require(MenuMotion.openingOpacity(0, false) == 1, "Reduced motion bypasses the fade");
        double previous = 0;
        for (long elapsed = 0; elapsed <= MenuMotion.OPEN_NANOS; elapsed += 1_000_000) {
            double opacity = MenuMotion.openingOpacity(elapsed, true);
            require(opacity >= previous && opacity <= 1, "Fade is monotonic and bounded");
            previous = opacity;
        }
        require(MenuMotion.closingOpacity(0,true)==1,"Close starts visible");
        require(MenuMotion.closingOpacity(MenuMotion.CLOSE_NANOS,true)==0,"Close finishes exactly");
        require(MenuMotion.closingOpacity(0,false)==0,"Reduced motion closes immediately");
        double close=1;
        for(long t=0;t<=MenuMotion.CLOSE_NANOS;t+=1_000_000) {
            double next=MenuMotion.closingOpacity(t,true);require(next<=close&&next>=0,"Close is monotonic");close=next;
        }
        double at30=0,at144=0;
        for(int i=0;i<30;i++)at30=MenuMotion.approach(at30,100,1./30,.055);
        for(int i=0;i<144;i++)at144=MenuMotion.approach(at144,100,1./144,.055);
        require(Math.abs(at30-at144)<1e-9,"Scrolling response is frame-rate independent");
        require(MenuMotion.mix(0xFF000000,0xFFFFFFFF,0)==0xFF000000,"Color start exact");
        require(MenuMotion.mix(0xFF000000,0xFFFFFFFF,1)==0xFFFFFFFF,"Color end exact");
        for (int module = 0; module < 8; module++) {
            double a = MenuMotion.iconAngle(0, module);
            require(Math.abs(MenuMotion.iconAngle(24_000_000_000L, module) - a) < 1e-9, "Rotation loops without drift");
            require(Math.abs(MenuMotion.iconAngle(12_000_000_000L, module) - a - Math.PI) < 1e-9, "Slow half rotation");
            require(Double.isFinite(MenuMotion.iconAngle(Long.MAX_VALUE, module)), "Rotation remains finite");
        }
        System.out.println("PASS: menu fade timing, reduced motion, bounded easing and slow frame-independent icon rotation");
    }
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
}
