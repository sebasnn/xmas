package dev.xmas.overlay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** All user settings. Saved to config/xmasoverlay.json. */
public final class XmasConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("xmasoverlay.json");
	private static XmasConfig instance = new XmasConfig();

	// Mining overlay
	public boolean overlayEnabled = true;
	public int overlayOpacity = 40;            // 5..100 (%)
	// Particles
	public boolean miningParticles = true;
	public int particleDensity = 5;            // 1..10
	public boolean knifeParticles = true;
	// Sky
	public boolean galaxySky = true;
	public int starBoost = 200;                // 100..300 (%) extra star brightness at night
	// Inspect
	public boolean inspectEnabled = true;
	public boolean inspectAnyItem = false;     // false = only the totem knife
	// Hand position
	public boolean handOffsetEnabled = true;
	public int handOffset = 40;                // 0..100 (%) how far items sit from the screen corner
	// Visual-only swing speed
	public boolean swingEnabled = false;
	public int swingSpeed = 100;               // 25..300 (%), client side visual only

	public static XmasConfig get() { return instance; }

	public static void load() {
		try {
			if (Files.exists(FILE)) {
				XmasConfig c = GSON.fromJson(Files.readString(FILE), XmasConfig.class);
				if (c != null) instance = c;
			}
		} catch (Exception e) {
			System.err.println("[xmasoverlay] could not read config, using defaults: " + e);
		}
		instance.clamp();
		save();
	}

	public static void save() {
		try {
			instance.clamp();
			Files.writeString(FILE, GSON.toJson(instance));
		} catch (IOException e) {
			System.err.println("[xmasoverlay] could not save config: " + e);
		}
	}

	public static void reset() { instance = new XmasConfig(); save(); }

	private void clamp() {
		overlayOpacity = Math.max(5, Math.min(100, overlayOpacity));
		particleDensity = Math.max(1, Math.min(10, particleDensity));
		starBoost = Math.max(100, Math.min(300, starBoost));
		handOffset = Math.max(0, Math.min(100, handOffset));
		swingSpeed = Math.max(25, Math.min(300, swingSpeed));
	}
}
