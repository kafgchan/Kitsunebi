# Credits and Attribution

## Project

**Kitsunebi (狐火)** — A Minecraft mod providing transparent illumination, phantom construction blocks, altar crafting/repair, and Shuko (朱狐) equipment and tools.

Project repository: [kafgchan/Kitsunebi](https://github.com/kafgchan/Kitsunebi).

## Development Team & Contributors

- **Project Lead & Author**: kafgchan (楓 / Kaede) — project direction, requirements, design decisions, and gameplay testing.

## AI-assisted development tools

The following tools assisted the project's development; these entries describe their use rather than naming additional human contributors.

- **Antigravity / Gemini (Google)**: Implementation, multi-loader integration, crafting logic, collision handling, fixes, and visual concept references.
- **GPT (OpenAI)**: Specification drafting, architecture reviews, and logic reviews.
- **Codex (OpenAI)**: Textures, 3D models, UV mapping, GUI work, code reviews, fixes, and verification.
- **OpenAI image generation**: Foxfire texture sources and selected cloth/fur texture studies.

## Inspiration & Acknowledgments

- Inspired by **Bamboo Mod (竹Mod)** by **Ruby (ruby / rubnsn)**, especially its _Kitsunebi_ invisible floating illumination. See the [upstream project](https://github.com/rubnsn/bambooo). This acknowledgment does not relicense Bamboo Mod material.
- Minecraft is developed by Mojang Studios. This project is not an official Mojang or Microsoft product.

## Licenses

- **Source Code**: [GNU Lesser General Public License v3.0 or later](LICENSE) (`LGPL-3.0-or-later`).
- **Original asset data (models, textures, and other visuals)**: [Creative Commons Attribution 4.0 International](LICENSE-ASSETS.md) (`CC BY 4.0`), within the scope stated there. Java rendering/model code and generation scripts use the source-code license.
- **GPL terms incorporated by LGPL v3**: [GPL v3](LICENSE-GPL-3.0).

## Asset provenance and dependencies

- Foxfire textures and selected cloth/fur texture studies were generated with OpenAI image generation. Project model geometry, texture palettes, and model-generation scripts were developed with Codex assistance.
- The shrine outfit, fox mask, and gohei were modeled from a user-supplied Gemini concept reference. Their geometry and palettes were generated in code rather than copied as pixels from the reference. Reference images and rejected studies are documented in `docs/v04-codex/sources/`; historical previews and reviews are in the sibling development archive. Their use as references does not grant rights to third-party material.
- Construction models use Minecraft model parents and adapted vanilla blockstate layouts. Minecraft material remains subject to Mojang/Microsoft terms and is excluded from the project's original-asset license grant.
- Architectury API, Fabric API, loaders, JEI, EMI, REI, Curios, and GeckoLib are external dependencies, not relicensed as Kitsunebi assets. The current build does not shade or nest their JARs into the mod.
- Gradle Wrapper is third-party build tooling; its upstream license is unchanged.

- The NeoForge 1.21.1 Shuko armor prototype uses [GeckoLib](https://github.com/bernie-g/geckolib), fixed at 4.9.3. The Shuko model is derived from existing Kitsunebi geometry with user-requested reference-based detail adjustments; the original shared texture and Java models are retained. GeckoLib remains a separately installed dependency.


- Shuko / Byakko / Kokko cosmetic armor shares repository-authored geometry; colors follow the user-supplied design references. Generated UV palettes and native item models are maintained by the scripts under `docs/v04-codex/`.

- Shinko armor: separately authored reference-based geometry, generated 256px UV palette and emissive mask. The user-supplied divine fox design sheets guided the halo, jewelry, layered garments, footwear and tails. Source scripts are in `docs/v04-codex/`; original armor assets remain retained.
