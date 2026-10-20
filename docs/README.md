# Development documentation

Current specifications, architecture, asset guidelines, provenance, source textures,
and model generation/validation tools remain in this directory.

Historical reviews, handover notes, GUI/model previews, texture backups, and local
verification logs were moved on 2026-10-20 to the sibling directory:

`../../Kitsunebi-Archive/2026-10-20/`

The archive preserves original repository-relative paths under `docs/` and `build/`.
Its `manifest.csv` records each file's original path, size, and verified SHA-256.
Historical preview and review paths mentioned by older documents refer to that archive.
Preview scripts may recreate their output images locally when run.

`prepare-textures.ps1` now writes future backups to the sibling
`Kitsunebi-Archive/texture-backups/` directory rather than this repository.

Build caches, IDE configuration/output, development worlds, current build artifacts,
and regression test sources remain in place for active development. They are excluded
from version control where appropriate by `.gitignore`.
