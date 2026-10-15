# Kitsunebi — アセット制作・受渡規約

本ドキュメントは、アセット制作と実装間の連携規約を定義する。

## 1. アセット配置ディレクトリ規約

アセットはマルチローダー共通として `common/src/main/resources/` に配置する。

```text
common/src/main/resources/assets/kitsunebi/
├── textures/
│   ├── item/           # 狐火アイテム・御幣テクスチャ (.png)
│   └── block/          # 狐火ブロックテクスチャ (.png)
├── models/
│   ├── item/           # アイテムモデル定義 (.json)
│   └── block/          # ブロックモデル定義 (.json)
├── blockstates/        # ブロック状態定義 (.json)
└── lang/               # 言語ファイル (en_us.json, ja_jp.json)
```

## 2. アセット仕様

- **テクスチャ解像度**: 16x16 PNG
- **狐火の表示**: 透過（半透明）炎演出、RenderType.translucent()
- **対応色**: 7色（白、青、水色、緑、紫、橙、赤）
