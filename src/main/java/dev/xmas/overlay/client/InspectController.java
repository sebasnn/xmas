package dev.xmas.overlay.client;

import dev.xmas.overlay.XmasConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** Holds the state of the "examine the knife" animation. */
public final class InspectController {
	public static final long DURATION_MS = 3600;
	private static long startMs = -1;

	private InspectController() {}

	public static void trigger() { startMs = System.currentTimeMillis(); }
	public static void cancel() { startMs = -1; }

	/** 0..1 while the animation is running, otherwise -1. */
	public static float progress() {
		if (startMs < 0) return -1f;
		float u = (System.currentTimeMillis() - startMs) / (float) DURATION_MS;
		if (u >= 1f) { startMs = -1; return -1f; }
		return u;
	}

	public static boolean appliesTo(ItemStack stack, Hand hand) {
		if (hand != Hand.MAIN_HAND) return false;
		XmasConfig cfg = XmasConfig.get();
		if (!cfg.inspectEnabled || stack.isEmpty()) return false;
		return cfg.inspectAnyItem || stack.isOf(Items.TOTEM_OF_UNDYING);
	}

	private static float smooth(float t) {
		t = Math.max(0f, Math.min(1f, t));
		return t * t * (3f - 2f * t);
	}

	/**
	 * Pose of the item for progress u: {dx, dy, dz, rotY, rotX, rotZ} (view space, degrees).
	 * The knife is brought up in front of the face, turned all the way round, tipped to show the
	 * blade edge and the crook, then lowered again.
	 */
	public static float[] pose(float u) {
		float in = smooth(u / 0.14f);
		float out = 1f - smooth((u - 0.88f) / 0.12f);
		float k = Math.min(in, out);                 // how "raised" the knife is
		float turn = smooth((u - 0.14f) / 0.62f);    // main full rotation
		float rotY = 360f * turn + 18f * k;
		float rotX = k * (-28f + 34f * (float) Math.sin(u * Math.PI * 2.0));
		float rotZ = k * (-12f + 22f * (float) Math.sin(u * Math.PI * 3.0 + 0.6));
		float wobble = (float) Math.sin(u * Math.PI * 6.0) * 0.012f * k;
		return new float[] { -0.50f * k, 0.26f * k + wobble, 0.40f * k, rotY, rotX, rotZ };
	}

	public static void tick(MinecraftClient mc) {
		if (startMs < 0 || mc.player == null) return;
		if (mc.options.attackKey.isPressed() || mc.options.useKey.isPressed() || mc.currentScreen != null) cancel();
	}
}
