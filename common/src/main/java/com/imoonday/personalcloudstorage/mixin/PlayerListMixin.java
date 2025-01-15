package com.imoonday.personalcloudstorage.mixin;

import com.imoonday.personalcloudstorage.event.EventHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(method = "sendAllPlayerInfo", at = @At("RETURN"))
    private void sendAllPlayerInfo(ServerPlayer player, CallbackInfo ci) {
        EventHandler.syncToClient(player);
    }
}
