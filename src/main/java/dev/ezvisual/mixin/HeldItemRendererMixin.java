package dev.ezvisual.mixin;

import dev.ezvisual.EzVisualClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    // Custom Hand: розмір і зсув руки/предмета від першої особи
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void ez$handStart(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                              float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                              VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        matrices.push();
        if (EzVisualClient.customHand) {
            matrices.translate(EzVisualClient.handX, EzVisualClient.handY, 0);
            float s = EzVisualClient.handScale;
            matrices.scale(s, s, s);
        }
    }

    @Inject(method = "renderFirstPersonItem", at = @At("RETURN"))
    private void ez$handEnd(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                            float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                            VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        matrices.pop();
    }
}
