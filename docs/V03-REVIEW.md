# v0.3 review and asset delivery — 2026-10-18

Source: [Kitsunebi Notion plan, v0.3 and all addenda](https://app.notion.com/p/3f87df49e838819ba664ce79e2f748f9), read at page edit timestamp 2026-10-18T02:17:24.986Z. Later explicit corrections supersede earlier recipes. Scope of this delivery: code review and original assets; crafting, networking, drops and licensing implementation are reviewed without changes.

## Findings requiring implementation fixes

Update after the user's subsequent explicit GUI implementation request: findings 1, 2, 3 and 5 are addressed by the new altar menu/screen. See [GUI changes and verification](V03-ALTAR-GUI.md). The numbered findings below preserve the originally reviewed state and old line references; they are not claims about the updated GUI. Door recovery, recipe-policy and license findings remain separate work.

1. **[P1] Synchronized values never reach the fields used by the client.** `KitsunebiAltarMenu.java:44` creates independent `SimpleContainerData`; packet updates change that array, but getters/calculation read `categoryIndex`, `selectionIndex`, and `quantity` at lines 93–105. No `setData` bridge updates those fields. `KitsunebiAltarScreen.java:98–99` immediately rebuilds from the unchanged client menu after sending a category packet. Selecting Phantom therefore leaves the fire controls and client-side material preview on the default selection, even while the server has selected a different output. Use one authoritative data backing for both getters and packets; rebuild controls after synchronized category changes. Validate category, selection and quantity on the server; button 1000 currently stores quantity zero despite calculate clamping it to one.

2. **[P1] Partial result extraction consumes a full requested batch.** `KitsunebiAltarMenu.java:62–70` consumes `quantity` irrespective of how many items were removed. Right-clicking a result stack can take half; shift-click into an inventory with only one available item of capacity can move only that item (`:174`) and still execute the full-cost `onTake` (`:200`). `setupResultSlot` replaces the remaining result after consumption. Reproduce: select 8, supply one batch, leave capacity for one output, shift-click. This violates the spec's requirement to reject insufficient output capacity without consuming ingredients. Gate extraction on complete capacity and commit a single server-side craft transaction; test normal, right-click, shift-click and repeated packets.

3. **[P1] Six input slots cannot hold materials for advertised 64-item batches.** `KitsunebiAltarMenu.java:17` allows six ordinary stacks. Fire ×64 needs 4 stacks bamboo + 2 lapis + 1 light material + 1 amethyst + 1 dye = at least nine slots. Foxfire block ×64 needs eight stacks of fire and one amethyst-block stack. Phantom full block ×64 similarly needs nine stacks. Their 64 presets cannot become craftable with the current inventory. Expand storage or implement a clearly specified additional material source; test actual simultaneous capacity for every recipe and 1–64 outputs.

4. **[P2] Breaking the upper door half cannot recover its item.** `PhantomDoorBlock.java:32` invokes the recovery handler only for LOWER. Breaking UPPER with the gohei removes the door through neighbor updates, without invoking playerDestroy on LOWER; both halves have no loot table. Route either half through exactly one recovery path, verifying gohei, mask+silk, creative and support destruction to avoid duplicates.

5. **[P2] Required quantity controls and material preview are incomplete.** `KitsunebiAltarScreen.java:59–70` has preset buttons and ±10 only, with no 1–64 step-one slider or ±1 alternative. `:158` deliberately shows only two ingredients although fire needs five groups. The +10 button extends beyond the 200-pixel panel and Phantom selection buttons occupy the result-slot area. Complete the slider and all ingredient displays, use item/name tooltips instead of bare numbered buttons, and validate layout at minimum GUI resolution.

6. **[P2] Batch rounding is implemented before the spec decision.** `AltarCraftingEngine.java:95–116` uses ceil proportional costs for arbitrary output counts, although the spec still marks remainder handling unresolved. For example, crafting one slab six times costs six full blocks, versus three for a batch of six slabs. This is an unresolved design decision rather than a claimed arithmetic bug. Select and document a remainder policy before release; do not silently promise exact vanilla ratios for every output quantity. Fence conversion is also a provisional 1:1 and lever still consumes a stick while the specification asks for further conversion design.

7. **[P2] Published license declarations disagree.** Root LICENSE contains LGPL v3, while `fabric.mod.json:11` and README still declare MIT. `CREDITS.md` was absent before this asset delivery. Finish the separately assigned license migration, including the configured NeoForge property, GPL companion text, source headers, and precise asset/model scope. This finding records repository/spec mismatch, not legal advice.

## Confirmed coverage

- Three shaped recipes match the current Notion patterns and ingredients, retaining the legacy goggles registry ID for mask compatibility.
- Seven dye mappings match the final spec, including cyan → light_blue_dye.
- Consumption scans ascending slots and accepts mixed alternative materials, mixed fire colors, and mixed horizontal/vertical half or quarter inputs.
- Current final stair/slope recipes use six full/half/quarter ingredients for four high/mid/low outputs. Earlier contradictory addenda must not be used.
- Conditional recovery is present on seven fires and all phantom classes, including stacked-piece counts. Door upper-half handling remains the exception above.
- Recipes are hard-coded Java; the spec's data-driven recipe/config direction and optional accessory renderers still need follow-up.

## Original assets delivered

- `textures/block/kitsunebi_altar_atlas.png`: new opaque four-material atlas, built-in image generation. Obsidian, ceremonial crimson cloth, amethyst and parchment; no vanilla texture copies.
- `models/block/kitsunebi_altar.json`: 35 cuboid elements; stone base, two fox emblems, hanging cloth, four crystals and a two-sided open book. No billboard surfaces; coordinates remain within one block.
- `models/item/kitsunebi_altar.json`: vanilla-size GUI transform. Existing blockstate and registry ID are reused.
- `docs/generate-altar-model.py`: deterministic native model generator, invoked by the existing building generator.
- `docs/preview-altar-model.py` / `altar-model-preview.png`: textured three-angle render from actual JSON, including element rotations and UVs. This is offline asset validation, not a game screenshot.

The block keeps its existing full-cube collision/occlusion. The decorative upper outline is recessed; assess whether a dedicated collision/occlusion shape is desirable in the implementation pass. No new facing property is required. No crafting/menu/drop behavior is changed by this asset delivery.

## Validation

Resource-reference/bounds validator and both-loader build results are reported in the accompanying chat. In-game GUI synchronization, crafting transactions and upper-door recovery remain review findings, not repaired or runtime-certified behavior.
