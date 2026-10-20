# v0.4 Codex 制作物・Antigravity向け接続手順

最新の防具・狐面・御幣は、ユーザー提供のGemini図を基準に再制作済み。詳細は `SHRINE-REFERENCE.md`。再生成は `generate-shrine-outfit.py` を使用し、旧生成器を単独実行しない。

2026-10-19。既存の登録・能力・祭壇メニュー・ネットワーク・レシピ・Config・共有lang・build.gradleは変更していません。

## 実機表示不一致の修正（同日、追加依頼）

以下の追補が、以降に記載した初回納品時の「接続未完了」等の状態に優先します。

- 実機ログで9モデルの `kitsunebi:entity/kitsunebi_equipment` がmissing spriteになることを確認。`assets/minecraft/atlases/blocks.json` にsingle sourceを追加し、ゲームのアトラスへ登録。
- 装着モデルをcommonのclient/modelへ収録。NeoForgeのRegisterClientExtensionsEventとFabricのArmorRendererへ登録。NeoForgeの防具レイヤー画像も同じ素材アトラスへ変更。頭・胴・腕・脚のポーズを親モデルから引き継ぎ、尾は袴の描画時だけ表示。
- Fabricビルドにはclient/modelのクライアント専用収録設定を追加。防具能力、祭壇メニュー、レシピ、通信の実処理は変更なし。
- ツール5点の一人称スケール0.9→1.2、三人称0.85→1.1。GUI・額縁・地面スケールは維持。
- 不足素材／準備完了の不足翻訳キーを6言語へ追加。修理説明の長い連続文を素材アイコンと回復量2行に置換。中央・回復量欄の狐火／狐火ブロックにアイテムツールチップを追加。
- 検証用NeoForgeクライアントで3カテゴリ・修理画面・9モデル・装着形状を描画。実アトラスのsprite存在チェック成功。初回修正後のコンパイルは両ローダーで成功。
- 最終検証では4部位のNeoForge描画拡張が実際にカスタム装着モデルを返すことも確認。修理画面の狐火ブロック名を実描画し、Fabric remapJarも成功。配布JAR内の装着モデル2クラスとアトラス登録・画像の収録を検査済み。

`neoforge-1.21.1/build/altar-verify-client/altar-gui-2.png` は製作、`altar-gui-3.png` は修理素材ツールチップ、`altar-gui-4.png` はゲームレンダラーによる装備確認画像です。

## 適用した内容

- 装備4点、ツール5点のアイテムモデルを立体モデルへ変更。白、朱赤、金、黒で統一。GUI、両手、額縁、地面の表示変換を定義。
- AI生成の布・毛素材を `textures/entity/kitsunebi_equipment.png` に追加。金属等の色見本はコード生成。
- 祭壇画面に「狐火」「ブロック」「装備・ツール」、装備内の「製作」「修理」を表示。装備の数量ボタン／スライダーは非表示。素材投入欄の3列×5行、既存のスロット座標・番号は維持。
- 修理対象枠、修理後プレビュー、必要素材、状態表示の受け口を追加。追加表示は `ui/v04_labels.json` の6言語で供給。
- 装備メニューが下記APIを実装するまでは、製作品の見本だけ表示し、実行は無効。未対応サーバーへ新しい操作IDを送信しない。

## 祭壇メニュー側：Antigravityの担当

`KitsunebiAltarMenu implements net.kafgchan.kitsunebi.api.AltarEquipmentView` とし、`kitsunebiEquipmentView()` で同期済み状態を返してください。

State の意味：

- `tab`: 0=製作、1=修理。`selection` は choices 内の選択位置。
- `choices`: 製作の選択肢。現在の見本順は頭飾り、羽織、袴、足袋、剣、ツルハシ、斧、シャベル、クワ。槍は1.21.1へ追加しない。
- `preview`: `RecipeResult`。完成品／修理後のコピー、材料名、必要数、所持数。クライアント表示のために実アイテムを変更しない。
- `ready`: バックエンド対応済み。`canExecute`: 素材、対象、損傷、出力先容量等を含む実行可否。
- `status`: プレイヤー向けのローカライズ済み状態文。
- `repairTargetSlot`: 実際のメニュースロット番号。対象の素材投入枠を金枠で示す。非修理時は-1。

操作IDの提案（サーバー側の実装は追加していません）：

| ID | 意味 |
|---|---|
| 0/1/2 | 既存のカテゴリ変更 |
| 100 + 選択位置 | 既存形式の製作対象選択 |
| 2000 | 既存の製作実行 |
| 3000 / 3001 | 製作／修理サブタブ |
| 2001 | 修理実行 |

新IDの衝突、選択範囲、装備カテゴリ以外からの要求、数量=1、対象と素材の除外、成功時だけ消費、修理済み装備の出力先はサーバー側で検証してください。全状態はメニューのDataSlot等で同期し、画面を閉じて再接続しても整合させてください。

## 装着モデル側：Antigravityの担当

`integration/java/.../KitsunebiArmorVisualModel.java` は装着描画用の独立した納品ソースです。まだ本番ソースセットへ登録していません。`codex/v04/armor_geometry.json` と同じ元データから生成しました。

1. クライアント専用ソースセットへ配置し、各ローダーのモデルレイヤー登録で `createLayer()` を登録する。
2. 装備描画フックでベイク済みインスタンスを取得。プレイヤー親モデルから各部位の変換／ポーズをコピーしてから `configure(slot, kitsunebiArmorCount, ageInTicks)` を呼ぶ。
3. `kitsunebi:textures/entity/kitsunebi_equipment.png` を使用。HEAD=耳、CHEST=着物・羽織・袖、LEGS=袴・帯・尾、FEET=足袋。通常の防具描画と重複させない。
4. 尾は袴の描画時だけ表示。装備数1/2/3/4で1/3/6/9本。根元の左右・上下揺れを設定済み。実体・当たり判定は追加しない。
5. Fabricでは既存commonのクライアント専用クラスと同様にmainから除外し、clientへの収録を設定する。1.20.1／26.xは対応APIで別途検証する。

既存のバニラ防具用 `textures/models/armor/kitsunebi_layer_*.png` は変更していません。AI生成の展開図2枚は厳密なバニラUV位置に合わず、`sources/` に参考画像として保存しただけです。装着モデルが上記素材アトラスを使うことでUVを確定します。

## 確認と未確認

- common、Fabric client、NeoForge mainのJavaコンパイルは初回成功。
- 独立した装着モデルも次の追加タスクでコンパイル成功。既存buildファイルの変更は不要。

- 最終再確認では、並行作業中の `common-1.21.1/src/main/java/net/kafgchan/kitsunebi/item/armor/ModArmorMaterials.java:38` の構文エラー（`)` または `,` が必要）でコンパイルが停止。このファイルはCodex側で変更していません。9モデルの範囲・UV参照、6言語のキー、ボーン親子関係、尾9本の整合性チェックは成功。

```powershell
java -jar gradle/wrapper/gradle-wrapper.jar -I docs/v04-codex/verify-visuals.gradle :common-1.21.1:compileJava :fabric-1.21.1:compileClientJava :neoforge-1.21.1:compileCodexVisualModel --offline --console=plain
```

- `equipment-preview.png` は本番アイテムJSONとUVをソフトウェアで描画した確認画像。
- `armor-preview.png` は装着用の階層・尾本数を描画した確認画像。ゲーム内スクリーンショットではなく、UVは素材象限の簡易投影。
- 実機での装着・アニメーション・製作／修理通信は上記登録・メニュー接続後の確認が必要。前回レビューで発見した起動時の防具レジストリ問題もAntigravity側の修正対象です。
- 生成し直す場合はこのフォルダの `generate-equipment.py` を使用。既存の全体生成器は使わない。

モデル・ソースの詳細と素材の出典は `CREDITS-ADDENDUM.md` を参照。
