package dev.trevorroll;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
public final class RewardAudio {
    private RewardAudio(){}
    public static void play(RewardSound style,int note,float volume){
        if(style==RewardSound.DRAGON_LEVEL){
            play(RewardSound.DRAGON,0,volume);play(RewardSound.LEVEL_UP,0,volume);return;
        }
        var event=switch(style){
            case DRAGON_LEVEL, DRAGON -> SoundEvents.ENDER_DRAGON_GROWL;
            case WITHER -> SoundEvents.WITHER_SPAWN;
            case LEVEL_UP -> SoundEvents.PLAYER_LEVELUP;
            case CHIME -> SoundEvents.NOTE_BLOCK_CHIME.value();
        };
        float pitch=style==RewardSound.CHIME?new float[]{1,1.26f,1.5f}[Math.clamp(note,0,2)]:1;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event,pitch,volume*.55f));
    }
}
