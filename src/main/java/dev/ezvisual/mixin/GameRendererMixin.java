package dev.ezvisual.mixin;

import dev.ezvisual.EzVisualClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void ez$noHurtShake(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (EzVisualClient.noHurtShake) ci.cancel();
    }

    // Aspect Ratio
    @Inject(method = "getBasicProjectionMatrix", at = @At("RETURN"))
    private void ez$aspect(float fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
        if (!EzVisualClient.aspectOn) return;
        Window w = MinecraftClient.getInstance().getWindow();
        float cur = (float) w.getFramebufferWidth() / (float) w.getFramebufferHeight();
        Matrix4f m = cir.getReturnValue();
        m.m00(m.m00() * cur / EzVisualClient.aspect);
    }
}
