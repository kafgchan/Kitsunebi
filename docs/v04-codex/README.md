# v0.4 Codex 制作物・Antigravity向け接続手順

2026-10-19。既存の登録・能力・祭壇メニュー・ネットワーク・レシピ・Config・共有lang・build.gradleは変更していません。

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

- 最終再確認では、並行作業中の `common/src/main/java/net/kafgchan/kitsunebi/item/armor/ModArmorMaterials.java:38` の構文エラー（`)` または `,` が必要）でコンパイルが停止。このファイルはCodex側で変更していません。9モデルの範囲・UV参照、6言語のキー、ボーン親子関係、尾9本の整合性チェックは成功。

```powershell
java -jar gradle/wrapper/gradle-wrapper.jar -I docs/v04-codex/verify-visuals.gradle :common:compileJava :fabric:compileClientJava :neoforge:compileCodexVisualModel --offline --console=plain
```

- `equipment-preview.png` は本番アイテムJSONとUVをソフトウェアで描画した確認画像。
- `armor-preview.png` は装着用の階層・尾本数を描画した確認画像。ゲーム内スクリーンショットではなく、UVは素材象限の簡易投影。
- 実機での装着・アニメーション・製作／修理通信は上記登録・メニュー接続後の確認が必要。前回レビューで発見した起動時の防具レジストリ問題もAntigravity側の修正対象です。
- 生成し直す場合はこのフォルダの `generate-equipment.py` を使用。既存の全体生成器は使わない。

モデル・ソースの詳細と素材の出典は `CREDITS-ADDENDUM.md` を参照。
