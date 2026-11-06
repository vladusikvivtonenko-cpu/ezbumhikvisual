package dev.ezvisual.mixin;

import dev.ezvisual.EzVisualClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {
    // прибирає вогонь на екрані
    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void ez$noFire(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (EzVisualClient.noFire) ci.cancel();
    }

    // прибирає синю плівку під водою
    @Inject(method = "renderUnderwaterOverlay", at = @At("HEAD"), cancellable = true)
    private static void ez$clearWater(MinecraftClient client, MatrixStack matrices,
                                      VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (EzVisualClient.clearWater) ci.cancel();
    }
}
