package dev.trevorroll.test;

import java.util.function.DoubleSupplier;
import net.minecraft.client.resources.sounds.SoundInstance;

/** Test-only observation of the real SoundManager, never packaged in the mod. */
public final class CinematicSoundProbe {
    private CinematicSoundProbe() {}
    public static int count;
    public static double progress;
    private static DoubleSupplier clock;
    public static void start(DoubleSupplier supplier){clock=supplier;count=0;progress=-1;}
    public static void stop(){clock=null;}
    public static void played(SoundInstance sound){
        if(clock==null||!sound.getIdentifier().toString().equals("minecraft:entity.ender_dragon.growl"))return;
        double current=clock.getAsDouble();if(current<0||current>=1)return;
        count++;progress=current;
    }
}
