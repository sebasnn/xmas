package dev.xmas.overlay.client;

import dev.xmas.overlay.XmasConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Arm;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

/** Client-only particle effects for mining and for the held knife. */
public final class ChristmasParticles {
	private static final Random RNG = Random.create();
	private static final int[] COLORS = { 0xE8202A, 0x1FA84F, 0xFFD35A, 0xFFFFFF };

	private static boolean wasBreaking = false;
	private static BlockPos lastPos = null;

	private ChristmasParticles() {}

	public static void tick(MinecraftClient mc) {
		ClientWorld world = mc.world;
		ClientPlayerEntity player = mc.player;
		if (world == null || player == null || mc.interactionManager == null) { wasBreaking = false; return; }
		XmasConfig cfg = XmasConfig.get();

		// ---- mining ----
		boolean breaking = mc.interactionManager.isBreakingBlock();
		if (cfg.miningParticles) {
			if (breaking && mc.crosshairTarget instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
				lastPos = hit.getBlockPos();
				Vec3d p = hit.getPos();
				Direction side = hit.getSide();
				int count = Math.max(1, cfg.particleDensity / 2 + 1);
				for (int i = 0; i < count; i++) {
					double x = p.x + side.getOffsetX() * 0.03 + (side.getAxis() == Direction.Axis.X ? 0 : (RNG.nextDouble() - 0.5) * 0.9);
					double y = p.y + side.getOffsetY() * 0.03 + (side.getAxis() == Direction.Axis.Y ? 0 : (RNG.nextDouble() - 0.5) * 0.9);
					double z = p.z + side.getOffsetZ() * 0.03 + (side.getAxis() == Direction.Axis.Z ? 0 : (RNG.nextDouble() - 0.5) * 0.9);
					spawnFestive(mc, x, y, z, side.getOffsetX() * 0.04, 0.02 + side.getOffsetY() * 0.04, side.getOffsetZ() * 0.04);
				}
			}
			if (wasBreaking && !breaking && lastPos != null && world.getBlockState(lastPos).isAir()) {
				Vec3d c = Vec3d.ofCenter(lastPos);
				int burst = 8 + cfg.particleDensity * 4;
				for (int i = 0; i < burst; i++) {
					double vx = (RNG.nextDouble() - 0.5) * 0.5, vy = RNG.nextDouble() * 0.35, vz = (RNG.nextDouble() - 0.5) * 0.5;
					spawnFestive(mc, c.x + vx * 0.8, c.y + vy * 0.5, c.z + vz * 0.8, vx, vy, vz);
				}
			}
		}
		wasBreaking = breaking;

		// ---- knife sparkles ----
		if (cfg.knifeParticles && (player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING) || player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING))) {
			boolean main = player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING);
			Arm arm = main ? player.getMainArm() : player.getMainArm().getOpposite();
			double side = arm == Arm.RIGHT ? 1 : -1;
			Vec3d eye = player.getEyePos();
			Vec3d fwd = player.getRotationVec(1f);
			Vec3d right = fwd.crossProduct(new Vec3d(0, 1, 0));
			if (right.lengthSquared() < 1e-4) right = new Vec3d(1, 0, 0);
			right = right.normalize();
			Vec3d up = right.crossProduct(fwd).normalize();
			Vec3d hand = eye.add(fwd.multiply(0.7)).add(right.multiply(0.32 * side)).subtract(up.multiply(0.24));
			if (RNG.nextFloat() < 0.35f + 0.05f * cfg.particleDensity) {
				double ox = (RNG.nextDouble() - 0.5) * 0.18, oy = (RNG.nextDouble() - 0.5) * 0.22, oz = (RNG.nextDouble() - 0.5) * 0.18;
				spawnFestive(mc, hand.x + ox, hand.y + oy, hand.z + oz, 0, 0.012, 0);
			}
		}
	}

	private static void spawnFestive(MinecraftClient mc, double x, double y, double z, double vx, double vy, double vz) {
		int roll = RNG.nextInt(5);
		ParticleEffect effect;
		switch (roll) {
			case 0 -> effect = ParticleTypes.SNOWFLAKE;
			case 1 -> effect = ParticleTypes.END_ROD;
			case 2 -> effect = ParticleTypes.ITEM_SNOWBALL;
			default -> effect = new DustParticleEffect(COLORS[RNG.nextInt(COLORS.length)], 0.8f);
		}
		mc.particleManager.addParticle(effect, x, y, z, vx, vy, vz);
	}
}
