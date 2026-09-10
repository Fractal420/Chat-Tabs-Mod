# Yarn → Mojmap migration notes (1.21.11)

## What was done
- `build.gradle` now uses `mappings loom.officialMojangMappings()`
- `yarn_mappings` removed from `gradle.properties`
- Bulk rename of common symbols performed:
  - MinecraftClient → Minecraft
  - KeyBinding → KeyMapping
  - Identifier → ResourceLocation
  - Text → Component
  - DrawContext → GuiGraphics
  - textRenderer → font
  - currentScreen → screen
  - inGameHud → gui / getChatHud → getChat
  - ChatHud → ChatComponent
  - ButtonWidget → Button, TextFieldWidget → EditBox, SliderWidget → AbstractSliderButton
  - InputUtil → InputConstants
  - RenderTickCounter → DeltaTracker
  - KeyMapping.Category.create → .register
  - ResourceLocation.of → fromNamespaceAndPath
  - drawTextWithShadow / drawCenteredTextWithShadow → drawString / drawCenteredString

## What you still need to do locally
1. Open the project in IntelliJ / run `./gradlew build`
2. Fix remaining compile errors (especially in the Mixin):
   - `Click` and `KeyInput` may have become different event types in 1.21.11 Mojmap
   - `GuiGraphics.getMatrices()` / `pushMatrix()` may now be `pose()` / `pushPose()`
   - Mixin `@Inject` method descriptors and `@Shadow` field types
   - Any Fabric event signatures that changed with Mojmap
3. Useful lookup: https://mappings.dev/ or Linkie (Yarn ↔ Mojang)

Original Yarn sources were kept under `src/main/java.yarn-backup/` for reference.
