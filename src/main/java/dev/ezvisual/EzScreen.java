package dev.ezvisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class EzScreen extends Screen {
    private interface FGet { float get(); }
    private interface FSet { void set(float v); }
    private record Toggle(String name, BooleanSupplier get, Consumer<Boolean> set) {}
    private record Slider(String name, float min, float max, FGet get, FSet set, boolean hueBar) {}

    private static final Identifier ANIME = Identifier.of("ezvisual", "textures/gui/anime.png");
    private static final int IMG_W = 192, IMG_H = 288; // розмір png; малюється вдвічі меншим

    private final List<Toggle> toggles = List.of(
            new Toggle("HUD", () -> EzVisualClient.hud, v -> EzVisualClient.hud = v),
            new Toggle("Armor HUD", () -> EzVisualClient.armorPanel, v -> EzVisualClient.armorPanel = v),
            new Toggle("Без вогню на екрані", () -> EzVisualClient.noFire, v -> EzVisualClient.noFire = v),
            new Toggle("Без тряски екрана", () -> EzVisualClient.noHurtShake, v -> EzVisualClient.noHurtShake = v),
            new Toggle("Чиста вода", () -> EzVisualClient.clearWater, v -> EzVisualClient.clearWater = v),
            new Toggle("Hit Color", () -> EzVisualClient.hitColor, v -> EzVisualClient.hitColor = v),
            new Toggle("Crosshair", () -> EzVisualClient.crosshair, v -> EzVisualClient.crosshair = v),
            new Toggle("Custom Hand", () -> EzVisualClient.customHand, v -> EzVisualClient.customHand = v),
            new Toggle("China Hat", () -> EzVisualClient.chinaHat, v -> EzVisualClient.chinaHat = v),
            new Toggle("Hat: солом'яний стиль", () -> EzVisualClient.strawHat, v -> EzVisualClient.strawHat = v),
            new Toggle("Jump Circles", () -> EzVisualClient.jumpCircles, v -> EzVisualClient.jumpCircles = v),
            new Toggle("Aspect Ratio", () -> EzVisualClient.aspectOn, v -> EzVisualClient.aspectOn = v),
            new Toggle("Райдужний колір", () -> EzVisualClient.rainbow, v -> EzVisualClient.rainbow = v)
    );

    private final List<Slider> sliders = List.of(
            new Slider("Колір", 0f, 1f, () -> EzVisualClient.hue, v -> EzVisualClient.hue = v, true),
            new Slider("Aspect", 0.5f, 3f, () -> EzVisualClient.aspect, v -> EzVisualClient.aspect = v, false),
            new Slider("Рука: розмір", 0.3f, 1.5f, () -> EzVisualClient.handScale, v -> EzVisualClient.handScale = v, false),
            new Slider("Рука: X", -0.5f, 0.5f, () -> EzVisualClient.handX, v -> EzVisualClient.handX = v, false),
            new Slider("Рука: Y", -0.5f, 0.5f, () -> EzVisualClient.handY, v -> EzVisualClient.handY = v, false)
    );

    private static final int W = 440, H = 292, TOP = 34, RH = 18;
    private static final int LX = 8, LW = 204, RX = 224, RW = 208;
    private int dragging = -1;

    public EzScreen() { super(Text.literal("EzVisual")); }

    private int px() { return (width - W) / 2; }
    private int py() { return (height - H) / 2; }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        int x = px(), y = py();
        int accent = 0xFF000000 | EzVisualClient.color();

        ctx.fill(x, y, x + W, y + H, 0xE0101018);
        ctx.fill(x, y, x + W, y + 2, accent);
        ctx.drawCenteredTextWithShadow(textRenderer, "EzVisual", x + W / 2, y + 12, accent);

        // перемикачі
        for (int i = 0; i < toggles.size(); i++) {
            Toggle t = toggles.get(i);
            int ry = y + TOP + i * RH, rx = x + LX;
            boolean hover = mx >= rx && mx <= rx + LW && my >= ry && my < ry + RH - 2;
            ctx.fill(rx, ry, rx + LW, ry + RH - 2, hover ? 0x80303048 : 0x60202030);
            ctx.drawText(textRenderer, t.name(), rx + 6, ry + 4, 0xFFFFFFFF, true);
            boolean on = t.get().getAsBoolean();
            int sx = rx + LW - 30, sy = ry + 2;
            ctx.fill(sx, sy, sx + 24, sy + 12, on ? accent : 0xFF444455);
            int kx = on ? sx + 13 : sx + 1;
            ctx.fill(kx, sy + 1, kx + 10, sy + 11, 0xFFFFFFFF);
        }

        // повзунки
        for (int i = 0; i < sliders.size(); i++) {
            Slider s = sliders.get(i);
            int ry = y + TOP + i * RH, rx = x + RX;
            float v = s.get().get();
            float f = (v - s.min()) / (s.max() - s.min());
            if (s.hueBar()) {
                for (int k = 0; k < RW; k++) {
                    ctx.fill(rx + k, ry, rx + k + 1, ry + RH - 2,
                            Color.HSBtoRGB(k / (float) RW, 0.75f, 1f) | 0xFF000000);
                }
                int kx = rx + Math.round(f * (RW - 3));
                ctx.fill(kx - 1, ry - 1, kx + 4, ry + RH - 1, 0xFFFFFFFF);
            } else {
                ctx.fill(rx, ry, rx + RW, ry + RH - 2, 0x60202030);
                ctx.fill(rx, ry, rx + Math.round(f * RW), ry + RH - 2, (accent & 0xFFFFFF) | 0x90000000);
                ctx.drawText(textRenderer, s.name() + ": " + String.format("%.2f", v), rx + 6, ry + 4, 0xFFFFFFFF, true);
            }
            if (s.hueBar()) ctx.drawText(textRenderer, s.name(), rx + 6, ry + 4, 0xFF000000, false);
        }

        // аніме-дівчинка
        int ix = x + RX + (RW - IMG_W / 2) / 2;
        int iy = y + TOP + sliders.size() * RH + 4;
        ctx.getMatrices().push();
        ctx.getMatrices().scale(0.5f, 0.5f, 1f);
        ctx.drawTexture(RenderLayer::getGuiTextured, ANIME, ix * 2, iy * 2, 0f, 0f, IMG_W, IMG_H, IMG_W, IMG_H);
        ctx.getMatrices().pop();

        ctx.drawCenteredTextWithShadow(textRenderer, "Founder: ezbumhikvisual", x + W / 2, y + H - 14, 0xFFAAAAAA);
    }

    private void applySlider(int i, double mx) {
        Slider s = sliders.get(i);
        float f = (float) ((mx - (px() + RX)) / RW);
        f = Math.max(0f, Math.min(1f, f));
        s.set().set(s.min() + f * (s.max() - s.min()));
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int x = px(), y = py();
            for (int i = 0; i < toggles.size(); i++) {
                int ry = y + TOP + i * RH;
                if (mx >= x + LX && mx <= x + LX + LW && my >= ry && my < ry + RH - 2) {
                    Toggle t = toggles.get(i);
                    t.set().accept(!t.get().getAsBoolean());
                    return true;
                }
            }
            for (int i = 0; i < sliders.size(); i++) {
                int ry = y + TOP + i * RH;
                if (mx >= x + RX && mx <= x + RX + RW && my >= ry && my < ry + RH - 2) {
                    dragging = i;
                    applySlider(i, mx);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging >= 0) { applySlider(dragging, mx); return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = -1;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }
}
