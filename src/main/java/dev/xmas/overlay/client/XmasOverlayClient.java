package dev.xmas.overlay.client;

import dev.xmas.overlay.XmasConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class XmasOverlayClient implements ClientModInitializer {
	public static final String MOD_ID = "xmasoverlay";

	public static KeyBinding inspectKey;
	public static KeyBinding configKey;

	/** id (folder under resourcepacks/) -> translation key of its display name */
	private static final String[][] PACKS = {
			{ "totem_red", "xmasoverlay.pack.red" },
			{ "totem_frost", "xmasoverlay.pack.frost" },
			{ "totem_evergreen", "xmasoverlay.pack.evergreen" },
			{ "totem_golden", "xmasoverlay.pack.golden" },
			{ "totem_galaxy", "xmasoverlay.pack.galaxy" },
			{ "totem_ginger", "xmasoverlay.pack.ginger" },
	};

	@Override
	public void onInitializeClient() {
		XmasConfig.load();

		KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
		// Unbound-by-default is not possible with this constructor, so pick keys vanilla does not use.
		// Change them any time in Options > Controls > Key Binds > "Christmas Overlay".
		inspectKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.xmasoverlay.inspect", GLFW.GLFW_KEY_G, category));
		configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.xmasoverlay.config", GLFW.GLFW_KEY_J, category));

		HudElementRegistry.attachElementBefore(VanillaHudElements.MISC_OVERLAYS,
				Identifier.of(MOD_ID, "mining_overlay"), new MiningOverlay());

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (inspectKey.wasPressed()) {
				if (mc.player != null && mc.currentScreen == null && XmasConfig.get().inspectEnabled) InspectController.trigger();
			}
			while (configKey.wasPressed()) {
				if (mc.currentScreen == null) mc.setScreen(new ConfigScreen(null));
			}
			InspectController.tick(mc);
			ChristmasParticles.tick(mc);
		});

		// Built-in knife colour packs: switch them on/off in Options > Resource Packs.
		FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(container -> {
			for (String[] p : PACKS) {
				ResourceLoader.registerBuiltinPack(Identifier.of(MOD_ID, p[0]), container,
						Text.translatable(p[1]), PackActivationType.NORMAL);
			}
		});
	}
}
