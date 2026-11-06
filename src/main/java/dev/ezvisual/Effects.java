package dev.ezvisual;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ezvisual.mixin.OverlayTextureAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Effects {
    private record Circle(double x, double y, double z, long t) {}

    private static final List<Circle> circles = new ArrayList<>();
    private static boolean wasOnGround = true;
    private static int lastOverlay = 0;
    private static final int VANILLA_HURT = 0xB2FF0000;
    private static final int SEG = 48;

    // ---------------- tick ----------------
    public static void tick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null) { circles.clear(); return; }

        if (EzVisualClient.jumpCircles && wasOnGround && !p.isOnGround() && p.getVelocity().y > 0.1) {
            circles.add(new Circle(p.getX(), p.getY(), p.getZ(), System.currentTimeMillis()));
        }
        wasOnGround = p.isOnGround();

        applyHitColor(mc);
    }

    /** Hit Color: міняє колір червоної плівки при ударі (візуально, тільки у тебе на клієнті). */
    private static void applyHitColor(MinecraftClient mc) {
        int argb = EzVisualClient.hitColor ? (0xB2000000 | EzVisualClient.color()) : VANILLA_HURT;
        if (argb == lastOverlay) return;
        try {
            NativeImageBackedTexture tex = ((OverlayTextureAccessor) mc.gameRenderer.getOverlayTexture()).ezGetTexture();
            NativeImage img = tex.getImage();
            if (img == null) return;
            for (int y = 0; y < 8; y++) for (int x = 0; x < 16; x++) img.setColorArgb(x, y, argb);
            tex.upload();
            lastOverlay = argb;
        } catch (Throwable ignored) { }
    }

    // ---------------- render ----------------
    public static void render(WorldRenderContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity p = mc.player;
        MatrixStack ms = ctx.matrixStack();
        if (p == null || ms == null) return;

        Vec3d cam = ctx.camera().getPos();
        float td = ctx.tickCounter().getTickDelta(true);
        int rgb = EzVisualClient.color();

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        // Jump Circles
        long now = System.currentTimeMillis();
        Iterator<Circle> it = circles.iterator();
        while (it.hasNext()) {
            Circle c = it.next();
            float age = (now - c.t()) / 900f;
            if (age >= 1f) { it.remove(); continue; }
            ms.push();
            ms.translate(c.x() - cam.x, c.y() - cam.y + 0.02, c.z() - cam.z);
            float r = 0.3f + 1.3f * age;
            int a = (int) ((1f - age) * 220);
            ring(ms.peek().getPositionMatrix(), 0, r, r + 0.14f, (a << 24) | rgb);
            ms.pop();
        }

        // China Hat (тільки у 3-й особі)
        if ((EzVisualClient.chinaHat) && !mc.options.getPerspective().isFirstPerson()) {
            double x = MathHelper.lerp(td, p.prevX, p.getX()) - cam.x;
            double y = MathHelper.lerp(td, p.prevY, p.getY()) + p.getHeight() - 0.02 - cam.y;
            double z = MathHelper.lerp(td, p.prevZ, p.getZ()) - cam.z;
            ms.push();
            ms.translate(x, y, z);
            Matrix4f m = ms.peek().getPositionMatrix();
            if (EzVisualClient.strawHat) strawHat(m); else cone(m, rgb);
            ms.pop();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    // ---------------- геометрія ----------------
    private static void ring(Matrix4f m, float y, float r0, float r1, int argb) {
        BufferBuilder b = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= SEG; i++) {
            double a = Math.PI * 2 * i / SEG;
            float cs = (float) Math.cos(a), sn = (float) Math.sin(a);
            b.vertex(m, cs * r0, y, sn * r0).color(argb);
            b.vertex(m, cs * r1, y, sn * r1).color(argb);
        }
        BufferRenderer.drawWithGlobalProgram(b.end());
    }

    private static void fan(Matrix4f m, float cy, float r, float apexY, int centerArgb, int edgeArgb) {
        BufferBuilder b = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        b.vertex(m, 0, apexY, 0).color(centerArgb);
        for (int i = 0; i <= SEG; i++) {
            double a = Math.PI * 2 * i / SEG;
            b.vertex(m, (float) Math.cos(a) * r, cy, (float) Math.sin(a) * r).color(edgeArgb);
        }
        BufferRenderer.drawWithGlobalProgram(b.end());
    }

    private static void cone(Matrix4f m, int rgb) {
        fan(m, 0f, 0.5f, 0.3f, 0xD0000000 | rgb, 0x70000000 | rgb);
        ring(m, 0f, 0.49f, 0.53f, 0xFF000000 | rgb);
    }

    /** Солом'яний капелюх (стиль "пірата"): поля + купол + червона стрічка. */
    private static void strawHat(Matrix4f m) {
        int straw = 0xFFE8C468, dark = 0xFFC9A04A, red = 0xFFC0392B;
        fan(m, 0f, 0.7f, 0f, straw, dark);               // поля
        // стінки купола
        BufferBuilder b = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= SEG; i++) {
            double a = Math.PI * 2 * i / SEG;
            float cs = (float) Math.cos(a) * 0.3f, sn = (float) Math.sin(a) * 0.3f;
            b.vertex(m, cs, 0f, sn).color(dark);
            b.vertex(m, cs, 0.24f, sn).color(straw);
        }
        BufferRenderer.drawWithGlobalProgram(b.end());
        fan(m, 0.24f, 0.3f, 0.24f, straw, straw);        // верх
        // стрічка
        BufferBuilder band = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= SEG; i++) {
            double a = Math.PI * 2 * i / SEG;
            float cs = (float) Math.cos(a) * 0.305f, sn = (float) Math.sin(a) * 0.305f;
            band.vertex(m, cs, 0.03f, sn).color(red);
            band.vertex(m, cs, 0.10f, sn).color(red);
        }
        BufferRenderer.drawWithGlobalProgram(band.end());
    }
}
