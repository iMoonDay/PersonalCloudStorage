package com.imoonday.personalcloudstorage.mixin;

import com.imoonday.personalcloudstorage.event.EventHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    public void tick(CallbackInfo ci) {
        EventHandler.onPlayerTick((Player) (Object) this);
    }
}
