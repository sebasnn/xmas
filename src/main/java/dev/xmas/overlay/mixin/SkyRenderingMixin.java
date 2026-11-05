package dev.xmas.overlay.mixin;

import dev.xmas.overlay.XmasConfig;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.SkyRendering;
import net.minecraft.client.render.state.SkyRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * "Galaxy" night sky: after vanilla fills in the sky render state we blend the night colour towards a
 * very dark, slowly drifting violet / blue / magenta nebula tint and brighten the stars. Daytime is untouched.
 */
@Mixin(SkyRendering.class)
public class SkyRenderingMixin {
	@Inject(method = "updateRenderState", at = @At("TAIL"))
	private void xmas$galaxy(ClientWorld world, float tickProgress, Camera camera, SkyRenderState state, CallbackInfo ci) {
		XmasConfig cfg = XmasConfig.get();
		if (!cfg.galaxySky || world.getRegistryKey() != World.OVERWORLD) return;
		SkyRenderStateAccessor acc = (SkyRenderStateAccessor) state;

		float stars = acc.xmas$getStarBrightness();       // 0 by day, ~0.5 at full night (less in rain)
		float night = Math.max(0f, Math.min(1f, stars * 2f));
		if (night <= 0.001f) return;

		float t = (System.currentTimeMillis() % 120000L) / 120000f * (float) (Math.PI * 2);
		// three nebula colours (r,g,b 0..1) mixed by slow sine waves
		float w1 = 0.5f + 0.5f * (float) Math.sin(t);
		float w2 = 0.5f + 0.5f * (float) Math.sin(t + 2.1f);
		float w3 = 0.5f + 0.5f * (float) Math.sin(t + 4.2f);
		float sum = w1 + w2 + w3;
		float r = (w1 * 0.16f + w2 * 0.04f + w3 * 0.22f) / sum;
		float g = (w1 * 0.04f + w2 * 0.10f + w3 * 0.03f) / sum;
		float b = (w1 * 0.36f + w2 * 0.30f + w3 * 0.30f) / sum;
		float dark = 0.55f;                                   // keep it properly dark
		int nr = (int) (r * dark * 255f * 3f), ng = (int) (g * dark * 255f * 3f), nb = (int) (b * dark * 255f * 3f);

		int vanilla = acc.xmas$getSkyColor();
		int vr = (vanilla >> 16) & 0xFF, vg = (vanilla >> 8) & 0xFF, vb = vanilla & 0xFF;
		int rr = (int) (vr + (Math.min(255, nr) - vr) * night);
		int gg = (int) (vg + (Math.min(255, ng) - vg) * night);
		int bb = (int) (vb + (Math.min(255, nb) - vb) * night);
		acc.xmas$setSkyColor((vanilla & 0xFF000000) | (rr << 16) | (gg << 8) | bb);

		acc.xmas$setStarBrightness(Math.min(1f, stars * (cfg.starBoost / 100f)));
	}
}
