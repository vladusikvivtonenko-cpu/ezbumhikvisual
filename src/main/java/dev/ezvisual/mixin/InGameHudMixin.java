package dev.ezvisual.mixin;

import dev.ezvisual.EzVisualClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    // ховає ванільний приціл, коли ввімкнено кастомний
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void ez$crosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (EzVisualClient.crosshair) ci.cancel();
    }
}
