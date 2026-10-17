# Texture assets

Generated with the built-in ImageGen tool on 2026-10-18 (JST).

The original pixel-art assets were generated separately for seven foxfire colors,
a gohei wand, goggles, and a shared phantom construction surface. Prompts and
source paths are recorded in `texture-generation.json`.

Runtime PNGs use 32 x 32 RGBA pixels. Pixel-center nearest-neighbor sampling
preserves the generated alpha channel without adding filtering. Foxfire block
and inventory textures use the same image for each color.

- `texture-sources/`: full-resolution generated source images.
- `texture-backup-2026-10-18/`: textures saved before replacement.
- `texture-preview.png`: enlarged runtime texture contact sheet.
- `prepare-textures.ps1`: repeatable conversion and replacement script.

Building item models now use dedicated shapes for stairs, slopes, fences, walls,
doors, buttons, levers, and pressure plates. Stair/slope resources cover all four
directions, both halves, and five corner states. Slope JSONs provide texture and
transform data plus fallback geometry. Both loaders replace the baked slope models
with `SmoothSlopeModel`, using continuous wedge faces from `SlopeMesh` for world
and inventory rendering. Corner planes are clipped at their exact intersections,
and UV coordinates span the whole surface without repeating per slice. Collision
uses ordinary vanilla stepping against four collision steps for high/mid sizes and
three for low size. The failed custom movement Mixin and dynamic support-plane
collision are removed. No position correction or speed multiplier is applied.
The selection outline remains sixteen slices and the rendered surface is continuous.
Regenerate with `generate-building-models.py`, validate
with `validate-building-models.py`, and inspect `building-model-preview.png`.
The preview is a flat-color geometry check, not an in-game screenshot.

The former architectural foxfire is now named Kitsunebi Block (狐火ブロック).
It uses a full cube model and full cube selection shape, retaining no collision.
Its opaque 32 x 32 block-face texture prompt is in
`kitsunebi-block-texture-generation.json` (built-in ImageGen).

Client vision now shows actual terrain textures rather than locator particles.
The client tracks vision equipment and requests a terrain rebuild only when vision
changes. Only loaded sections whose palettes contain Kitsunebi/phantom blocks are
marked dirty. Existing GPU meshes remain visible while those sections rebuild;
the client no longer clears the world renderer with `allChanged()` on equipment
changes. No chunks are loaded for this refresh. The volatile client visibility
flag is read by terrain worker threads.
Normal hidden blocks stay invisible; permanently visible colored flames remain
visible independently of vision. No server block state or collision is changed
when toggling vision. Both loaders use the same visibility helper.

The connected wall/fence and interactive door/button/plate/lever resource states
are generated from the installed Minecraft 1.21.1 resources, with texture overrides.
Inventory slab/stair/slope GUI rotations expose their thickness and ascending face.
Worn goggles geometry is outside this change.

GUI transforms use vanilla's 0.625 block scale and 1.0 sprite scale, with dedicated
views for volumetric flames, the tall framed door, and the fox mask.
Minecraft fence/wooden_fence/wall tags
include their phantom equivalents so vanilla connection rules recognize them.
Engine shape queries without an observing player retain their real outline shape;
only player selection queries are hidden without vision equipment. This avoids
`ParticleEngine.crack` requesting bounds on an empty shape. Kitsunebi Block has
solid attachment support and all six support faces while retaining empty collision.

`gradlew :common:shapeRegression` tests engine shape/bounds calls, fence connections,
and attachment support. It also verifies all 120 smooth slope mesh combinations
for signed volume, winding, bounds, rotation and upside-down orientation.
It is also part of the common `check` task.

Regression tests sample 21,660 surface points across all sizes/directions/corners
and verify the continuous visual surface. The isolated
NeoForge `runSlopeVerifyServer` task opts into a runtime fixture that performs 72
real `Player.travel` flat-ground entry and complete ascent runs, comparing slopes
against same-size stairs across all sizes/directions and three input speed settings.
High/mid traverse in the same tick count; low differs by at most one tick. It records
`neoforge/build/slope-verify-server/slope-verification.txt`, and stops the server.
Its working directory is separate from normal runs and it should bind localhost.
`SmoothSlopeModel` lives in the standard common main source root. Fabric excludes
that client-only source from main compilation, references the common compiled class
from its client classpath, and embeds it once in its jar; NeoForge compiles the
shared source directly. Refresh the IDE's Gradle project after source set changes.

`generate-cosmetic-models.py` builds code-native cuboid geometry and a flat material
palette for the seven volumetric flames, framed/windowed door and white/red fox mask.
The mask uses the legacy `kitsunebi_goggles` ID for save compatibility, but all six
locales name it Kitsunebi Mask. The item's `head` transform is used by vanilla's
CustomHeadLayer in the normal head slot; ears, muzzle, eye markings and straps are
actual geometry. Right-click swaps it into the head slot. Optional accessory slot
renderers are not implemented by this change.

`cosmetic-model-preview.png` shows front/side/top geometry projections. It does not
replace in-game verification of equipment alignment or third-party renderers.

Validation: inspect the contact sheet, verify PNG dimensions/transparency, and
resolve local texture references. In-game appearance still needs verification.

Low stair corners now apply union/intersection to both tiers. Low slope inner
corners retain the complete perpendicular arm in the mesh, collision and fallback
models. Connections require matching size; high/mid/low no longer distort each other.

Same-type horizontal/vertical halves combine into a cube with two pieces; quarters
combine up to four occupied slices. Click a vacant slice or the exposed inner face
to add a piece. Legacy `layer`/`facing` states are retained, with `extra=0` and
`double=false` defaults. Full stacks clear waterlogging. Mixed block types are not
combined. The opt-in NeoForge verification server exercises BlockItem.useOn in all
four directions, including collision volume and survival item consumption.
