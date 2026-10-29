package dev.trevorroll;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
public final class RewardAudio {
    private RewardAudio(){}
    public static void pair(RewardSound first,RewardSound second,int note,float firstVolume,float secondVolume){
        if(note<first.notes())play(first,note,firstVolume);
        if(note<second.notes())play(second,note,secondVolume);
    }
    public static void play(RewardSound style,int note,float volume){
        if(style==null||style==RewardSound.NONE||volume<=0)return;
        if(style==RewardSound.DRAGON_LEVEL){play(RewardSound.DRAGON,0,volume);play(RewardSound.LEVEL_UP,0,volume);return;}
        float pitch=style==RewardSound.CHIME?new float[]{1,1.26f,1.5f}[Math.clamp(note,0,2)]:1;
        var event=SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace(style.id));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event,pitch,volume*.55f));
    }
}
