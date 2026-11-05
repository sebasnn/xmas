package dev.xmas.overlay.mixin;

import dev.xmas.overlay.XmasConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes the local player's hand-swing animation faster/slower. Only the animation length of the
 * client-side player object changes; attack cooldown, damage and what the server sees do not.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Inject(method = "getHandSwingDuration", at = @At("RETURN"), cancellable = true)
	private void xmas$visualSwingSpeed(CallbackInfoReturnable<Integer> cir) {
		XmasConfig cfg = XmasConfig.get();
		if (!cfg.swingEnabled) return;
		if ((Object) this != MinecraftClient.getInstance().player) return;
		float speed = cfg.swingSpeed / 100f;
		cir.setReturnValue(Math.max(1, Math.round(cir.getReturnValue() / speed)));
	}
}
