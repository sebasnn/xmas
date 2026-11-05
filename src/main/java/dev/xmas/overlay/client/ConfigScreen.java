package dev.xmas.overlay.client;

import dev.xmas.overlay.XmasConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

public class ConfigScreen extends Screen {
	private final Screen parent;

	public ConfigScreen(Screen parent) {
		super(Text.literal("Christmas Overlay"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		XmasConfig c = XmasConfig.get();
		int colW = 150, gap = 10, rowH = 24;
		int left = width / 2 - colW - gap / 2;
		int right = width / 2 + gap / 2;
		int top = 36;

		// left column: on/off switches
		toggle(left, top + rowH * 0, colW, "Mining overlay", () -> c.overlayEnabled, v -> c.overlayEnabled = v);
		toggle(left, top + rowH * 1, colW, "Mining particles", () -> c.miningParticles, v -> c.miningParticles = v);
		toggle(left, top + rowH * 2, colW, "Knife particles", () -> c.knifeParticles, v -> c.knifeParticles = v);
		toggle(left, top + rowH * 3, colW, "Galaxy night sky", () -> c.galaxySky, v -> c.galaxySky = v);
		toggle(left, top + rowH * 4, colW, "Knife inspect", () -> c.inspectEnabled, v -> c.inspectEnabled = v);
		toggle(left, top + rowH * 5, colW, "Inspect any item", () -> c.inspectAnyItem, v -> c.inspectAnyItem = v);
		toggle(left, top + rowH * 6, colW, "Hand position", () -> c.handOffsetEnabled, v -> c.handOffsetEnabled = v);
		toggle(left, top + rowH * 7, colW, "Swing speed (visual)", () -> c.swingEnabled, v -> c.swingEnabled = v);

		// right column: sliders
		slider(right, top + rowH * 0, colW, "Overlay opacity", 5, 100, c.overlayOpacity, v -> c.overlayOpacity = v, v -> v + "%");
		slider(right, top + rowH * 1, colW, "Particle amount", 1, 10, c.particleDensity, v -> c.particleDensity = v, v -> "" + v);
		slider(right, top + rowH * 3, colW, "Star brightness", 100, 300, c.starBoost, v -> c.starBoost = v, v -> v + "%");
		slider(right, top + rowH * 6, colW, "Hand distance", 0, 100, c.handOffset, v -> c.handOffset = v, v -> v + "%");
		slider(right, top + rowH * 7, colW, "Swing speed", 25, 300, c.swingSpeed, v -> c.swingSpeed = v, v -> v + "%");

		addDrawableChild(ButtonWidget.builder(Text.literal("Reset all"), b -> {
			XmasConfig.reset();
			clearAndInit();
		}).dimensions(width / 2 - 154, height - 30, 100, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
				.dimensions(width / 2 + 54, height - 30, 100, 20).build());
	}

	private void toggle(int x, int y, int w, String label, BooleanSupplier get, Consumer<Boolean> set) {
		addDrawableChild(ButtonWidget.builder(text(label, get.getAsBoolean()), b -> {
			set.accept(!get.getAsBoolean());
			b.setMessage(text(label, get.getAsBoolean()));
		}).dimensions(x, y, w, 20).build());
	}

	private static Text text(String label, boolean on) {
		return Text.literal(label + ": " + (on ? "ON" : "OFF"));
	}

	private void slider(int x, int y, int w, String label, int min, int max, int value, IntConsumer set, IntFunction<String> fmt) {
		addDrawableChild(new SliderWidget(x, y, w, 20, Text.literal(label + ": " + fmt.apply(value)), (value - min) / (double) (max - min)) {
			private int current() { return min + (int) Math.round(this.value * (max - min)); }

			@Override protected void updateMessage() { setMessage(Text.literal(label + ": " + fmt.apply(current()))); }

			@Override protected void applyValue() { set.accept(current()); }
		});
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, 0xFFFFFFFF);
		ctx.drawCenteredTextWithShadow(textRenderer,
				Text.literal("Inspect key: set in Controls > Christmas Overlay. Knife colours: Resource Packs."),
				width / 2, height - 46, 0xFFAAAAAA);
	}

	@Override
	public void close() {
		XmasConfig.save();
		MinecraftClient.getInstance().setScreen(parent);
	}
}
