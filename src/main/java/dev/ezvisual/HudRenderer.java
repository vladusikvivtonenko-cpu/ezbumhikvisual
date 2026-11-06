package dev.ezvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import java.awt.Color;

public class HudRenderer {
    private static final int BG = 0xA0101018;
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public static void render(DrawContext ctx, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;

        int accent = 0xFF000000 | EzVisualClient.color();

        // ---- Custom Crosshair ----
        if (EzVisualClient.crosshair && mc.currentScreen == null && mc.options.getPerspective().isFirstPerson()) {
            int cx = ctx.getScaledWindowWidth() / 2, cy = ctx.getScaledWindowHeight() / 2;
            int gap = 3, len = 5;
            int[][] bars = {
                    {cx - gap - len, cy, cx - gap, cy + 1}, {cx + gap + 1, cy, cx + gap + 1 + len, cy + 1},
                    {cx, cy - gap - len, cx + 1, cy - gap}, {cx, cy + gap + 1, cx + 1, cy + gap + 1 + len}};
            for (int[] b : bars) {
                ctx.fill(b[0] - 1, b[1] - 1, b[2] + 1, b[3] + 1, 0xA0000000);
            }
            for (int[] b : bars) ctx.fill(b[0], b[1], b[2], b[3], accent);
            ctx.fill(cx, cy, cx + 1, cy + 1, 0xFFFFFFFF);
        }

        if (!EzVisualClient.hud || mc.getDebugHud().shouldShowDebugHud()) return;
        TextRenderer tr = mc.textRenderer;

        // ---- Watermark ----
        int ping = 0;
        if (mc.getNetworkHandler() != null) {
            PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (e != null) ping = e.getLatency();
        }
        String text = "EzVisual  |  Founder: ezbumhikvisual  |  " + mc.getCurrentFps() + " fps  |  " + ping + " ms";
        int w = tr.getWidth(text) + 10;
        int x = 6, y = 6;
        ctx.fill(x, y, x + w, y + 16, BG);
        ctx.fill(x, y, x + w, y + 2, accent);
        ctx.drawText(tr, text, x + 5, y + 5, 0xFFFFFFFF, true);

        // ---- Armor HUD ----
        if (!EzVisualClient.armorPanel) return;
        y = 28;
        int total = Math.round(mc.player.getArmor() / 20f * 100f);
        ctx.fill(x, y, x + 78, y + 14, BG);
        ctx.fill(x, y, x + 2, y + 14, accent);
        ctx.drawText(tr, "Броня: " + total + "%", x + 6, y + 3, 0xFFFFFFFF, true);
        y += 16;

        for (EquipmentSlot slot : ARMOR) {
            ItemStack st = mc.player.getEquippedStack(slot);
            if (st.isEmpty()) continue;
            int pct = 100;
            if (st.isDamageable()) {
                pct = Math.round((st.getMaxDamage() - st.getDamage()) * 100f / st.getMaxDamage());
            }
            int col = Color.HSBtoRGB(Math.max(0f, Math.min(1f, pct / 100f)) * 0.33f, 0.9f, 1f) | 0xFF000000;
            ctx.fill(x, y, x + 78, y + 18, BG);
            ctx.fill(x, y, x + 2, y + 18, accent);
            ctx.drawItem(st, x + 5, y + 1);
            ctx.drawText(tr, pct + "%", x + 26, y + 5, col, true);
            y += 20;
        }
    }
}
