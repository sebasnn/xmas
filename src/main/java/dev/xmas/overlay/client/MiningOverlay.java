package dev.xmas.overlay.client;

import dev.xmas.overlay.XmasConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * Transparent, animated Christmas frame drawn on the HUD while a block is being mined:
 * red/green edge glow, a swaying string of twinkling lights, falling snow and holly corners.
 * Drawn with plain rectangles so it needs no extra textures.
 */
public final class MiningOverlay implements HudElement {
	private float level = 0f;       // fade 0..1
	private long lastMs = System.currentTimeMillis();

	private static final int[] BULBS = { 0xFFFF3B47, 0xFF3DDC6B, 0xFFFFE066, 0xFF5AB8FF, 0xFFFFFFFF };

	@Override
	public void render(DrawContext ctx, RenderTickCounter tick) {
		MinecraftClient mc = MinecraftClient.getInstance();
		XmasConfig cfg = XmasConfig.get();
		long now = System.currentTimeMillis();
		float dt = Math.min(0.1f, (now - lastMs) / 1000f);
		lastMs = now;

		boolean mining = cfg.overlayEnabled && mc.player != null && mc.interactionManager != null
				&& mc.interactionManager.isBreakingBlock() && mc.options.hudHidden == false;
		float target = mining ? 1f : 0f;
		level += (target - level) * Math.min(1f, dt * (mining ? 7f : 4f));
		if (level < 0.01f) return;

		float a = level * cfg.overlayOpacity / 100f;
		int w = ctx.getScaledWindowWidth();
		int h = ctx.getScaledWindowHeight();
		float t = now / 1000f;

		// --- soft coloured edge glow (red on top/left, green on bottom/right) ---
		int edge = Math.max(18, h / 7);
		ctx.fillGradient(0, 0, w, edge, argb(a * 0.55f, 0xE8202A), argb(0f, 0xE8202A));
		ctx.fillGradient(0, h - edge, w, h, argb(0f, 0x1FA84F), argb(a * 0.55f, 0x1FA84F));
		int side = Math.max(10, w / 16);
		for (int i = 0; i < side; i++) {
			float f = 1f - i / (float) side;
			int al = (int) (a * 0.22f * f * f * 255);
			if (al <= 0) continue;
			ctx.fill(i, edge, i + 1, h - edge, (al << 24) | 0xE8202A);
			ctx.fill(w - 1 - i, edge, w - i, h - edge, (al << 24) | 0x1FA84F);
		}

		// --- string of twinkling lights hanging along the top ---
		int n = Math.max(10, w / 14);
		float step = w / (float) n;
		int prevX = 0, prevY = 4;
		for (int i = 0; i <= n; i++) {
			float x = i * step;
			float sag = (float) Math.sin((x / w) * Math.PI * 4.0) * 0.5f + 0.5f;
			int y = 3 + (int) (sag * 9 + Math.sin(t * 1.3 + i * 0.7) * 1.2);
			// wire
			ctx.fill(prevX, Math.min(prevY, y), (int) x + 1, Math.max(prevY, y) + 1, argb(a * 0.8f, 0x16361F));
			prevX = (int) x; prevY = y;
			if (i % 2 == 0 || i == n) continue;
			float tw = 0.55f + 0.45f * (float) Math.sin(t * 4.5 + i * 1.9);
			int c = BULBS[(i + (int) (t * 1.5)) % BULBS.length];
			int rgb = c & 0xFFFFFF;
			ctx.fill((int) x - 1, y + 1, (int) x + 2, y + 5, argb(a * tw, rgb));
			ctx.fill((int) x - 2, y + 2, (int) x + 3, y + 4, argb(a * tw * 0.35f, rgb));   // glow
			ctx.fill((int) x, y + 1, (int) x + 1, y + 2, argb(a * tw, 0xFFFFFF));
		}

		// --- holly in the lower corners ---
		holly(ctx, 6, h - 6, 1, a, t);
		holly(ctx, w - 6, h - 6, -1, a, t);

		// --- falling snow ---
		int flakes = 70;
		for (int i = 0; i < flakes; i++) {
			float r1 = hash(i * 12.9898f), r2 = hash(i * 78.233f), r3 = hash(i * 37.719f);
			float speed = 14f + r3 * 26f;
			float y = (t * speed + r2 * h) % (h + 6) - 3;
			float x = r1 * w + (float) Math.sin(t * (0.7f + r3) + i) * 9f;
			int size = r3 > 0.78f ? 2 : 1;
			float al = a * (0.45f + 0.55f * r2);
			int xi = (int) x, yi = (int) y;
			ctx.fill(xi, yi, xi + size, yi + size, argb(al, 0xFFFFFF));
			if (size == 2) {   // tiny cross sparkle for the big flakes
				ctx.fill(xi - 1, yi, xi, yi + 1, argb(al * 0.5f, 0xDDEEFF));
				ctx.fill(xi + 2, yi, xi + 3, yi + 1, argb(al * 0.5f, 0xDDEEFF));
				ctx.fill(xi, yi - 1, xi + 1, yi, argb(al * 0.5f, 0xDDEEFF));
				ctx.fill(xi, yi + 2, xi + 1, yi + 3, argb(al * 0.5f, 0xDDEEFF));
			}
		}
	}

	private static void holly(DrawContext ctx, int ox, int oy, int dir, float a, float t) {
		int green = 0x1FA84F, dark = 0x0F6B30;
		for (int i = 0; i < 5; i++) {
			int len = 18 - i * 2;
			for (int k = 0; k < len; k++) {
				int x = ox + dir * (k + i * 2);
				int y = oy - (int) (k * (0.25f + i * 0.22f)) - i;
				ctx.fill(x, y, x + 2, y + 2, argb(a * 0.85f, (k % 3 == 0) ? dark : green));
			}
		}
		float pulse = 0.8f + 0.2f * (float) Math.sin(t * 3f);
		for (int b = 0; b < 3; b++) {
			int bx = ox + dir * (4 + b * 3), by = oy - 6 - b * 2;
			ctx.fill(bx, by, bx + 3, by + 3, argb(a * pulse, 0xE8202A));
			ctx.fill(bx, by, bx + 1, by + 1, argb(a, 0xFFB3B8));
		}
	}

	private static float hash(float v) {
		float s = (float) Math.sin(v) * 43758.5453f;
		return s - (float) Math.floor(s);
	}

	private static int argb(float alpha, int rgb) {
		int al = Math.max(0, Math.min(255, (int) (alpha * 255f)));
		return (al << 24) | (rgb & 0xFFFFFF);
	}
}
