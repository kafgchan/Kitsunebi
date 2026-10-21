# Kitsunebi — Forge 1.20.1

Minecraft 1.20.1 / Forge 47.4.10以降 / Java 17向けの実装です。ビルドには付属のGradle 8.8 Wrapperを使用します。

## 移植した機能

- 7色の狐火、御幣、仮面、狐火ブロック、幻影建材、滑らかなスロープと重ね置き。
- 祭壇の3カテゴリ、15素材スロット、数量選択、装備の製作・修理。
- 朱狐の頭飾り・羽織・袴・足袋、狐耳・尻尾・和装モデル。
- 剣・ツルハシ・斧・シャベル・クワの妖術、採掘・伐採・農業。
- 空中歩行、上昇・降下、ダッシュ、スニーク＋ジャンプ切り替え、Gによる足袋の妖術ON/OFF。
- 7色いずれの狐火でも祭壇修理、染料による通常クラフトの色変更。
- 日本語・英語・韓国語・簡体字・繁体字（台湾・香港）。
- 任意連携：JEI / EMI / REIで祭壇レシピ表示、Curiosで頭アクセサリに仮面を装着。

槍は26.x限定の仕様のため追加しません。通常操作はルートREADMEと同じです。

## 必須・任意依存

基本動作はForgeのみで利用でき、**Architectury APIは必須ではありません**。
REIを追加する場合は、REI自身が必要とするArchitectury APIとCloth Configも必要です。
外部Modは配布JARに同梱しません。

開発時の連携バージョンはCurios 5.14.1+1.20.1、JEI 15.20.0.135、
EMI 1.1.24+1.20.1、REI 12.1.785です。

## ビルド・起動

Java 17の `JAVA_HOME` を指定し、このディレクトリで実行します。

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer

# 任意連携Modを開発起動に追加
.\gradlew.bat runClient -PrecipeViewer=jei -PcuriosEnabled=true
# recipeViewer は none（既定）/ jei / emi / rei
```

ルートのWrapperからはJava 17への委譲が可能です。

```powershell
.\gradlew.bat forge1201Build
.\gradlew.bat forge1201RunClient
.\gradlew.bat forge1201RunServer
```

出力は `build/libs/kitsunebi-forge-1.20.1-1.0.0.jar`。
通常のルート `build` は1.21.1向けです。

## バージョン差の扱い

1.20.1固有のコードはこのディレクトリに分離し、NBT保存・Forge SimpleChannel・
防具マテリアル・属性修飾・Forge描画拡張に対応しています。
モデル・テクスチャ・翻訳は `../common-1.21.1` の制作素材を共用します。
レシピ・タグ等は `generateLegacyData` が1.20.1の複数形ディレクトリと
`result.item`形式へ変換します。1.21.1のランタイムJavaコードには依存しません。

開発起動では任意ModのSRG参照マップをMojang名へ変換する設定を使用します。

## 回帰検証

```powershell
.\gradlew.bat runVerificationServer
.\gradlew.bat runVerificationServer -PcuriosEnabled=true
.\gradlew.bat runVisualVerifyClient
```

検証用の保存先は `build/` 配下に分離しています。
サーバー検証では専用フォルダーにMinecraft EULAの同意とサーバー設定が必要です。
通常の `run/` のワールドを検証に利用しません。

コードはLGPL-3.0-or-later、独自アセットはルートの `LICENSE-ASSETS.md` に従います。

## 検証結果（2026-10-20）

- 任意Modなしの専用サーバーで、祭壇・修理・7色の色変更・採掘・伐採・農業・重ね置き・空中歩行を回帰検証。
- スロープは3サイズ・4方向・3速度の72走行ケースを実際の `Player.travel` で確認。
- Curiosありの専用サーバーで仮面の頭スロットと可視化支援を確認。
- GUIの3カテゴリ・修理素材名・個数スライダー・防具モデルを実クライアントで描画確認。
- JEI / EMI / REIをそれぞれ単独で導入し、専用サーバーへの接続、空中歩行の開始・解除、祭壇レシピ52件の登録と画面表示を確認。
- `allow-flight=false` で空中歩行を80tick超継続し、解除後の落下中も切断されないことを確認。

Forge 1.20.1の浮遊判定には、サーバーが認めた空中歩行中と、解除後120tick以内の落下中に限って猶予を適用します。飛行権限そのものは変更しません。
検証は開発環境での回帰検証です。他Mod全般との互換性や長時間運用を保証するものではありません。

### Forge限定の追加確認（2026-10-20）

- スロープのUV単位差を修正。1.20.1の `TextureAtlasSprite.getU/getV` は0～16で指定するため、1.21.1の0～1座標を変換して適用。
- 実クライアントでスロープ3サイズのアイテムと全BlockStateのUV範囲を検証し、通常ブロックと並べた描画を確認。専用画面は `build/visual-verify-client/altar-gui-5.png`。
- 防具の実Forgeダメージ・反撃・ノックバックイベント、耐久・エンチャント可否、頭飾りの暗視／有害効果解除、袴＋足袋の速度2.5倍を追加検証。
- 祭壇・採掘・農業・重ね置き・空中歩行・72スロープ走行の回帰検証はPASS。今回確認した範囲で、追加のForge移植漏れは検出しなかった。
- 修正はForge 1.20.1に限定し、1.21.1のJava・共有アセットは変更しない。
