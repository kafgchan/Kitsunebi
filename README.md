# Kitsunebi (狐火)

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.259-orange)
![Fabric](https://img.shields.io/badge/Fabric-0.16.14-blue)
![Java](https://img.shields.io/badge/Java-21-red)
![License: LGPL v3](https://img.shields.io/badge/License-LGPL_v3-blue.svg)
![License: CC BY 4.0](https://img.shields.io/badge/License-CC_BY_4.0-lightgrey.svg)

旧竹Mod（Bamboo Mod）に登場した「狐火」に着想を得た、Minecraft 1.21.1 向けの独立した高品質・軽量な光源Modです。
NeoForge および Fabric の両ローダーに対応しています。

---

## 🌟 主な特徴

### 1. 透明照明・完全すり抜け
- **当たり判定なし**: プレイヤー、Mob、矢などの飛翔体は完全にブロックを通り抜けます。
- **水中設置（Waterlogged）対応**: 水中にも自然に設置でき、水流をせき止めません。
- **手持ち時の回収**: 手に「狐火」または「狐火の御幣」を持っている時のみ選択枠が現れ、左クリック一撃で回収できます。

### 2. 手持ち時の狐火可視化（パーティクル演出）
- 手に「狐火」または「狐火の御幣」を持っている間、周囲10m以内の透明な狐火の位置から各色の淡い光パーティクルがふわふわと立ち上り、どこに設置されているかが一目で分かります。
  - **白**: エンドロッドの白いきらめき
  - **青**: ソウル炎の蒼い火の粉
  - **水色**: グロウの淡い発光粒子
  - **緑**: 村人のハッピーきらめき粒子
  - **紫**: 魔女の魔法粒子
  - **橙・赤**: 揺らめく炎の粒子
- 手から離すとパーティクルは即座に消失し、完全な透明照明に戻ります。

### 3. 空中設置機能
- 空中で右クリックすることで、視線先の空間（3.5ブロック先）に直接狐火を配置できます。足場のない吹き抜けや天井の照明配置に最適です。

### 4. 狐火の御幣（専用道具）
- 狐火に対して右クリック（またはShift右クリック）することで、以下の状態を切り替えられます：
  - **表示モード**: 透明（非表示） ⇔ 常時表示（炎モデル）
  - **光源強度**: 発光（Level 15） ⇔ 消灯（Level 0）

### 5. 7色のカラーバリエーション
- 白 (White)
- 青 (Blue)
- 水色 (Cyan)
- 緑 (Green)
- 紫 (Purple)
- 橙 (Orange)
- 赤 (Red)

それぞれ独立したブロックおよびアイテムとして提供され、クリエイティブタブ「Kitsunebi」から利用可能です。

---

## 🛠️ 開発・ビルド環境

- **Minecraft**: 1.21.1
- **Gradle**: 9.7.1
- **Java**: 21+
- **NeoForge**: 21.1.259
- **Fabric Loader**: 0.16.14+
- **Fabric API**: 0.107.0+1.21.1

### ビルドコマンド
```powershell
# 全モジュールのコンパイル
.\gradlew.bat compileJava

# NeoForge / Fabric のビルド
.\gradlew.bat build

# NeoForge クライアントの起動
.\gradlew.bat :neoforge:runClient

# Fabric クライアントの起動
.\gradlew.bat :fabric:runClient
```

---

## 📄 ライセンス
- **ソースコード**: [GNU Lesser General Public License v3.0 or later](LICENSE) (`LGPL-3.0-or-later`)
- **アセット（テクスチャ・モデル等）**: [Creative Commons Attribution 4.0 International](LICENSE-ASSETS.md) (`CC BY 4.0`)
- **詳細・クレジット**: [CREDITS.md](CREDITS.md) をご覧ください。
