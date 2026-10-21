# 朱狐NPC 作業チェックポイント（2026-10-21）

## 保存済み

- `shuko.bbmodel` 編集原本、`export_assets.py`、`MODELING.md`、`asset-validation.json`。
- 独立NPC用geo、256×256の6表情PNG、idle/walk/talkアニメーション。
- 64ボーン／341キューブ。既存装備の形状は変更していない。
- NeoForge専用 `npc/` のEntity/Registry/Model/Renderer/登録/検証fixture。
- `/summon kitsunebi:shuko_npc`。尾数と表情はNPC独立データ。

## 実施結果

- NeoForge 1.21.1 `compileJava`: BUILD SUCCESSFUL。
- `runShukoNpcVerifyClient`: 起動し、専用FlatワールドにNPC表示を確認。
- `neoforge-1.21.1/build/shuko-npc-client/shuko-npc-00.png`以降に実ゲーム画像。
- Blockbench Web 5.2.2起動確認。ファイル選択APIがdetached inputエラーになり原本読込は未完了。JSON検査をエディター検証の代用とはしない。
- `authoring-previews`は補助ソフトウェア描画でありBlockbench画像ではない。

## 再開時の作業

1. `build/shuko-npc-client/shuko-npc-verification.txt`と`build/shuko-npc-run.log`で22段階fixtureの完了を確認する。中断時点では最後まで確認していない。
2. fixtureの非アクティブ時pauseを無効化し、通常全身画像のカメラ距離を縮める。実移動と会話animationの表示を画像で検証する。
3. Blockbenchで原本を実際に開き、4方向・近接画像を保存する。Web版の一時file inputが読込APIから消える問題を解決するかデスクトップ版を使用する。
4. Astra Highの独立完成レビュー（読み取り専任）を実施して造形を修正する。現状は初期モデルであり正本と同品質に達したとは判定していない。
5. entity.kitsunebi.shuko_npcの名前を既存commonの6ロケールJSONに追加する。Neo側に部分langファイルを作ると既存翻訳が隠れるため避ける。
6. 既存装備の回帰runGeckoVerifyClientを実施する。開始時SHA256はbuild/shuko-npc-baseline.json。
7. buildとstage-releaseを実行し、新しいJARの場所と検証範囲を報告する。今回のNPC入りJARはまだ作成していない。

使用量確認時: 5時間枠残り2%、週間枠残り52%。ユーザーの中断目安10%を下回った時点で記録。
