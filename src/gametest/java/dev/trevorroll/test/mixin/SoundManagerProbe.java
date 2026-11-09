package dev.trevorroll.test.mixin;

import dev.trevorroll.test.CinematicSoundProbe;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundManager.class)
abstract class SoundManagerProbe {
    @Inject(method="play",at=@At("HEAD"))
    private void trevorroll$observe(SoundInstance sound,CallbackInfoReturnable<SoundEngine.PlayResult> result){
        CinematicSoundProbe.played(sound);
    }
}
