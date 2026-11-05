# Christmas Overlay (Fabric, Minecraft 1.21.11, client-side)

Client-only mod: nothing needs installing on your server.

## Build
Needs Java 21 and Gradle 9.x (or copy `src/` into a fresh project from https://fabricmc.net/develop,
picking 1.21.11 + Yarn).
1. Open `gradle.properties` and check the versions against https://fabricmc.net/develop
   (especially `yarn_mappings` and `loom_version`).
2. `gradle build`
3. Put `build/libs/xmas-overlay-1.0.0.jar` and Fabric API in `.minecraft/mods`.

This source was written against the 1.21.11 Yarn mappings and Fabric API 0.141.6 but could not be
compiled where it was written. If Gradle reports a name error, it is a one-line fix in the file it names.

## In game
* **J** opens the settings screen (all toggles and sliders). **G** = inspect knife. Rebind both in
  Options > Controls > Key Binds > "Christmas Overlay".
* Knife colours: Options > Resource Packs, enable "Totem Knife: ..." (6 built in). Put the one you want
  above your Vanilla Christmas pack.

| Feature | Setting |
|---|---|
| Transparent mining overlay (edge glow, twinkling lights, snow, holly) | on/off + opacity |
| Mining + block-break particles | on/off + amount |
| Particles around the held knife (totem) | on/off |
| Galaxy night sky (dark violet/blue/magenta tint, brighter stars) | on/off + star brightness |
| Inspect animation (your key) | on/off, knife only or any item |
| Hand position (push held items away from the corner) | on/off + distance 0-100% |
| Swing speed 25-300% (visual only, local player only) | on/off + speed |

Settings are saved to `config/xmasoverlay.json`.

## Easiest way to get the .jar (no installs)
1. Create a free GitHub account and a new empty repository.
2. Upload everything from this folder (including the hidden `.github` folder) to it.
3. Open the repository's **Actions** tab, wait for "build" to finish, open the run and download
   the **xmas-overlay-jar** artifact. Unzip it: the `.jar` inside goes in your `mods` folder.

## Mod Menu
The mod appears in Mod Menu with an icon and a **Configure** button that opens the settings screen.
Mod Menu is optional; without it, press J.
