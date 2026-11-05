package dev.xmas.overlay.mixin;

import dev.xmas.overlay.XmasConfig;
import dev.xmas.overlay.client.InspectController;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
	// vanilla resting position of the item in view space (see HeldItemRenderer.applyEquipOffset)
	private static final float REST_X = 0.56f, REST_Y = -0.52f, REST_Z = -0.72f;

	@Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
	private void xmas$moveHand(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand,
			float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
			OrderedRenderCommandQueue queue, int light, CallbackInfo ci) {
		XmasConfig cfg = XmasConfig.get();
		Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
		float side = arm == Arm.RIGHT ? 1f : -1f;

		// 1) permanent offset: move the item away from the screen corner, towards the centre
		if (cfg.handOffsetEnabled && cfg.handOffset > 0) {
			float d = cfg.handOffset / 100f;
			matrices.translate(-side * 0.45f * d, 0.30f * d, -0.25f * d);
		}

		// 2) "examine the knife" animation, pivoting around the item so it turns in place
		float u = InspectController.progress();
		if (u >= 0f && InspectController.appliesTo(item, hand)) {
			float[] p = InspectController.pose(u);
			matrices.translate(side * p[0], p[1], p[2]);
			matrices.translate(side * REST_X, REST_Y, REST_Z);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(p[3]));
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(p[4]));
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * p[5]));
			matrices.translate(-side * REST_X, -REST_Y, -REST_Z);
		}
	}
}
