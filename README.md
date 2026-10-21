# Kitsunebi（狐火）

透明な光源「狐火」、幻影の建材、和装の「朱狐」装備と妖術ツールを追加するMinecraft Modです。竹Modの狐火に着想を得た独立プロジェクトです。

NeoForge 1.21.1の現在の開発版には、GeckoLib 4.9.3が必須となる[朱狐防具の描画仮実装](docs/v04-codex/FOX-ARMOR-VARIANTS.md)があります。仮実装JARはreleases/preview/に分離し、通常版JARと依存条件を区別します。

## 共同開発の入口

ビルドは `.\Build.ps1`、クライアント起動は `.\RunClient.ps1`、サーバー起動は `.\RunServer.ps1`（既定：Forge 1.20.1）。対象・任意Modの選択は [共通コマンド一覧](scripts/README.md) を参照してください。ビルド成功時のJARは [releases/](releases/README.md) に集約されます。

## 対応環境

現在の実装は **Minecraft 1.20.1 / Forge** と **1.21.1 / NeoForge・Fabric** 向けです。設計仕様のv0.4と、配布ファイルのバージョン（現在1.0.0）は別の番号です。

Forge 1.20.1はJava 17・Forge 47.4.10以降で動作します。詳細は[Forge版README](forge-1.20.1/README.md)を参照してください。次の表は1.21.1版の設定です。

| 項目 | 現在の設定 |
| --- | --- |
| Minecraft | 1.21.1 |
| Java（実行） | 21以上 |
| NeoForge | 21.1.259以上 |
| Fabric Loader | 0.19.5以上 |
| Fabric API | Fabric版で必須（開発時0.116.17+1.21.1） |
| Architectury API | 両ローダーで13.0.8以上が必須 |

26.1.2・26.2・26.3のFabric／NeoForgeは[移植用テンプレート](docs/PORTING.md)を組み込み済みですが、ゲームプレイ機能は未移植です。槍は1.20.1・1.21.1版にはありません。

## 導入

利用するMinecraft・ローダー用のMod JARを`mods`へ配置します。1.21.1版ではArchitectury API、Fabric版ではさらにFabric APIが必須です。**Forge 1.20.1版の基本動作にはArchitectury APIは不要**です（REIを追加する場合はREI側の依存として必要）。複数のローダー用JARを同時に入れないでください。サーバーで使用する場合は、サーバーと接続クライアントに対応するModと必須依存を導入します。

任意の連携Modは以下のとおりです。依存Mod本体はKitsunebiのJARには同梱しません。

| 連携 | NeoForge | Fabric |
| --- | --- | --- |
| JEI / EMI / REI | 祭壇レシピの表示 | 祭壇レシピの表示 |
| Curios API | 朱狐の仮面を頭アクセサリとして装備 | 対象外 |

レシピビューアは必要なものを1種類選んでください。上表のNeoForge・Fabricは1.21.1版です。Forge 1.20.1版もJEI・EMI・REIとCuriosに対応し、使用バージョンは[Forge版README](forge-1.20.1/README.md)に記載しています。1.21.1の開発設定はJEI 19.21.0.247、EMI 1.1.24、REI 16.0.799、Curios 9.5.1です。

## 主な機能

- **狐火**：白・青・水色・緑・紫・橙・赤の7色。透明照明、水中設置、空中設置、御幣による表示・発光の切り替えに対応。
- **幻影ブロック**：ブロック、ハーフ、階段、スロープ、柵、壁、ドアなどの建材。朱狐の仮面・頭飾りによる可視化を支援。
- **狐火の祭壇**：「狐火」「ブロック」「装備・ツール」の3カテゴリ。装備・ツールには製作と修理のサブタブがあり、必要素材・所持数・不足を表示します。
- **朱狐の装備**：狐耳の頭飾り、羽織、袴、足袋。装備効果と、装着数に応じた狐尻尾の描画に対応。
- **朱狐のツール**：剣、ツルハシ、斧、シャベル、クワ。遠距離・範囲攻撃、面採掘、鉱脈採掘、トンネル掘削、単木伐採、農業用妖術を備えます。
- **修理と色変更**：祭壇修理は7色いずれの狐火でも利用可能。狐火と変更先の染料を通常クラフトして色を変更できます。
- **6言語**：日本語、英語、韓国語、簡体字、繁体字（台湾・香港）。

妖術の範囲・処理数・耐久消費などはConfigで調整できます。具体的な素材と製作数は祭壇GUIまたはレシピビューアで確認してください。

## 操作（初期設定）

| キー | 操作 |
| --- | --- |
| V | 選択した妖術を発動 |
| B | ツールの妖術ON/OFF |
| N / M | 次 / 前の妖術モード |
| G | 足袋の妖術ON/OFF |
| 左クリック | 通常攻撃・採掘。対応ツールでは選択モードの範囲処理 |
| 右クリック | 道具本来の使用動作・クワの選択モードの操作 |

キーはMinecraftの操作設定から変更できます。チャット・GUIを開いている間は妖術キーを処理しません。

足袋の妖術が有効なとき、**スニーク＋ジャンプ**で空中歩行を切り替えます。空中歩行中はジャンプで上昇、スニークで降下し、ダッシュで移動速度が変化します。再切り替えには両方のキーを一度離してください。

## 開発・ビルド

Gradle Wrapperは9.7.1、コンパイルのJavaツールチェーンは21です。`gradle.properties`には開発PC固有の`org.gradle.java.home`があるため、別環境では利用するJDKへ変更するか、項目を削除して環境側で指定してください。Gradle自体を動かすJDKはWrapperの対応バージョンを使用します。

```powershell
# 1.21.1の両ローダーの配布JAR
.\gradlew.bat build

# Forge 1.20.1のビルド・起動（Java 17へ委譲）
.\gradlew.bat forge1201Build
.\gradlew.bat forge1201RunClient

# 開発クライアント
.\gradlew.bat :neoforge-1.21.1:runClient
.\gradlew.bat :fabric-1.21.1:runClient

# 開発用レシピビューアを選択（jei / emi / rei / none）
.\gradlew.bat :fabric-1.21.1:runClient -PrecipeViewer=emi

# NeoForgeでCuriosなしの起動
.\gradlew.bat :neoforge-1.21.1:runClient -PcuriosEnabled=false
```

配布用JARは`neoforge-1.21.1/build/libs/`と`fabric-1.21.1/build/libs/`に生成されます。`sources`・`dev`等の開発用JARは通常の導入には使用しません。ソース構成は`common-1.21.1`（共通処理・アセット）、`compat-1.21.1`（連携）、`neoforge-1.21.1`・`fabric-1.21.1`（ローダー固有処理）です。

制作・検証用の資料は[docs](docs/README.md)を参照してください。

## ライセンス・クレジット

- ソースコード・ビルドスクリプト：[LGPL-3.0-or-later](LICENSE)。LGPLが参照する[GPL v3本文](LICENSE-GPL-3.0)も同梱します。
- プロジェクト独自のテクスチャ・モデル等：[CC BY 4.0](LICENSE-ASSETS.md)。再利用時はクレジット、ライセンスへのリンク、変更の明示が必要です。
- Minecraftの素材・名称、外部ライブラリ、第三者提供物にはそれぞれの権利・条件が適用されます。
- 作者・制作経緯：[CREDITS.md](CREDITS.md)。本プロジェクトはMojang・Microsoftの公式製品ではありません。

Modのバイナリを公開する場合は、その版に対応するソースとビルド設定もライセンス条件に従って取得できるようにしてください。



1.21.1開発版には、詳細案を元にした色違い防具 **Shuko（朱狐）・Byakko（白狐）・Kokko（黒狐）** を実装しています。同能力で、白狐・黒狐の製作レシピは未追加です。新規装備はクリエイティブ一覧とコマンドから利用できます。

神狐（Shinko）は独立した詳細モデルを追加。[実装内容と確認画像](docs/v04-codex/SHINKO-DETAILED-MODEL.md)。クリエイティブ一覧／コマンドから入手でき、専用製作レシピは未追加です。

4種の袖・袴・組紐・足元・尻尾を詳細案に基づき再モデリング。[最新の確認画像と生成手順](docs/v04-codex/FOUR-FOX-REFERENCE-REMODEL.md)。対象は1.21.1 NeoForge / Fabricです。
