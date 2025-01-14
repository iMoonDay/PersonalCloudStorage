package com.imoonday.personalcloudstorage.mixin;

import com.imoonday.personalcloudstorage.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Redirect(method = "clientSideCloseContainer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void clientSideCloseContainer(Minecraft instance, Screen guiScreen) {
        if (!ClientUtils.switchingPage) {
            instance.setScreen(guiScreen);
        }
    }
}
