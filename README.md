# Strata

Upcoming Hypixel SkyBlock modification.

A modular client-side utility mod for Minecraft **26.1.2** (Fabric).

Modules declare their settings; the interface builds itself from them. Adding a module
requires no UI code.

## Requirements

- **JDK 25** (Minecraft 26.1 will not build on anything older)
- Gradle wrapper is included, so no separate Gradle install is needed

## Building

```bash
./gradlew build
```

The jar lands in `build/libs/`.

## Running a dev client

```bash
./gradlew runClient
```

## Notes for contributors

Minecraft 26.1 broke most existing Fabric knowledge. Things that will surprise you:

- **26.1 is unobfuscated.** There are no mappings at all: no Yarn, and
  `loom.officialMojangMappings()` fails because there is nothing to download. The
  `mappings` line is simply absent.
- The Loom plugin is `net.fabricmc.fabric-loom`, not `fabric-loom`, and it does not
  remap. Use `implementation`, not `modImplementation`; the task is `jar`, not
  `remapJar`.
- The GUI is retained-mode: `GuiGraphics` is now `GuiGraphicsExtractor`, and
  `Screen#render` is `Screen#extractRenderState`.
- Input arrives as objects (`MouseButtonEvent`, `KeyEvent`) rather than loose ints.

### Rendering

The interface is drawn with **NanoVG**, which needs care under Blaze3D. Drawing happens
at the tail of `RenderTarget#blitToScreen`, after Minecraft composites to the default
framebuffer, because 26.1 exposes no framebuffer id to target directly.

Blaze3D also leaves GL state NanoVG cannot cope with. The reset in
`NanoVgRenderer#beginFrame` is mandatory, not defensive. In particular
`glBindSampler(0, 0)`: a bound sampler overrides a texture's own parameters, and
NanoVG's glyph atlas has no mipmaps, so a leftover sampler makes it an incomplete
texture. The symptom is invisible text while shapes render fine, with no GL error.

Widgets draw through the `UiRenderer` interface and never touch NanoVG types directly.

## License

MIT License.

The bundled **Manrope** typeface is licensed under the SIL Open Font License. Its
licence ships alongside it at `src/client/resources/manrope-OFL.txt` and must remain
in any redistribution.
