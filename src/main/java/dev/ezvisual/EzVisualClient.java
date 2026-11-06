package dev.ezvisual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

public class EzVisualClient implements ClientModInitializer {
    public static boolean hud = true, armorPanel = true, noFire = true, noHurtShake = true, clearWater = true,
            hitColor = true, crosshair = true, customHand = false, chinaHat = true, strawHat = false,
            jumpCircles = true, aspectOn = false, rainbow = false;
    public static float hue = 0.85f, aspect = 1.78f, handScale = 1.0f, handX = 0f, handY = 0f;

    /** Спільний колір (RGB) для Hit Color, China Hat, Jump Circles, Crosshair, HUD. */
    public static int color() {
        float h = rainbow ? (System.currentTimeMillis() % 4000L) / 4000f : hue;
        return Color.HSBtoRGB(h, 0.75f, 1f) & 0xFFFFFF;
    }

    private KeyBinding kGui;

    @Override
    public void onInitializeClient() {
        kGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.ezvisual.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.ezvisual"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (kGui.wasPressed()) {
                if (mc.currentScreen == null && mc.player != null) mc.setScreen(new EzScreen());
            }
            Effects.tick(mc);
        });

        HudRenderCallback.EVENT.register(HudRenderer::render);
        WorldRenderEvents.LAST.register(Effects::render);
    }
}
