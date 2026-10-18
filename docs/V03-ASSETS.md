# v0.3 altar asset production

Mode: built-in image generation for the new bitmap atlas; Python native JSON geometry generation for the model. The generated bitmap is preserved without crop, recoloring or resampling. Texture metadata disables blur. All material UVs stay within their own quadrant.

Asset paths are relative to `common/src/main/resources/assets/kitsunebi/`:

- `textures/block/kitsunebi_altar_atlas.png` and `.png.mcmeta`
- `models/block/kitsunebi_altar.json`
- `models/item/kitsunebi_altar.json`

## Final image prompt

Create a production-ready square Minecraft pixel-art material texture ATLAS for an original foxfire crafting altar. This is a texture sheet, NOT a rendered block, no scene, no perspective, no labels, no text, no borders or gutters. Fill the entire square with EXACTLY four equal square quadrants in a 2 by 2 grid aligned at the exact image center. Top-left: seamless dark obsidian stone with subtle angular purple cracks, near-black charcoal/purple, restrained contrast. Top-right: seamless rich crimson woven ceremonial cloth with a few gold stitch speckles, red dominant. Bottom-left: seamless deep violet amethyst crystal material with lilac facets, small pale cyan highlights. Bottom-right: aged ivory parchment with sparse subtle tan fibers, blank, no writing. Crisp nearest-neighbor Minecraft pixel art, each quadrant should visually use a 16 by 16 logical-pixel grid enlarged uniformly; hard rectangular pixels, no antialiasing, no shadows, no lighting gradients, no outlines. Four materials consistent with vanilla Minecraft resolution and muted palette. Completely original artwork, no copied Minecraft textures. Opaque background.

## Model regeneration and visual inspection

Run `python docs/generate-altar-model.py`, then `python docs/preview-altar-model.py`. The main building generator also invokes the altar generator after its generic item transforms. The standalone preview software-renders the resource's actual element rotations and UV coordinates at three angles. It does not certify game lighting or occlusion.

The generated source is 1254×1254 pixels; retain the original source. Mipmap behavior at the selected game graphics setting should be checked during client visual QA. No texture animation or emissive rendering extension is required.
