# 朱狐 NPC 制作ログ

## 正本・担当範囲

2026-10-21。`仕様書/00_README_作業順序.md`、`01_朱狐_NPC_制作指示書.md` と同梱768×512の個別設定資料PNGを読み、全身正面・両側面・背面、顔・髪・衣装の部品拡大、表情と尾の段階図を確認した。独立NPCの編集原本と専用ゲーム資産を新規制作した。既存の装備モデル、テクスチャ、描画コードは本工程では読み込み・複製・変更していない。

NeoForge 1.21.1側の実際の`build.gradle`でGeckoLib 4.9.3を確認。編集原本はBlockbenchが読めるBedrock / format_version 4.10とし、アニメーション3本と全6表情PNGを埋め込んで持ち運べる形にした。Blockbench UIとMinecraftの検証は親担当であり、この制作ログでは成功とみなさない。

## 制作内容

- 体高33.3px前後、-Z正面、+Y上。顔の箱は8.8×8.0×6.2、厚みのある前髪・横髪・短い後ろ髪を独立させた大頭小柄の体型。頭部ピボットはY=21.7、目線はY≈25。耳先はY≈33.3。
- 狐面は正面画像に合わせて画面右＝キャラ左、+X側。指示文「右側」と食い違うが「画像優先」の指定を採った。朱柄の白面を頭の横に斜めに添え、赤い結び・金端の下がり紐を付けた。
- 白・朱・金の厚い斜め重ね襟、白い大振袖、赤い袖裏・袖縁、花と幾何学の袖裾模様、両肩の金鈴と房、手先独立、太い金縄帯・前結びと金房、背中の赤い蝶結びを制作。
- 中央朱の刺繍パネル、脇白布、金の縁取り、重なった朱の下裾、白赤柄の裾、短い足袋と赤鼻緒・金縁・二枚歯の下駄。
- 九尾は根元→中節→先節が別骨の立体扇型。付け根はY=10.75/Z=3.15付近、各尾は前後にずらした。白毛の段差・側面の毛束・朱先・白の不揃いな境界を立体で分けた。尾の標準表示は9本、独立した尾rootを隠すことで将来1/3/6本にも切替可能。装備数には連動させない。

## テクスチャ解像度の選択

64、128、256の候補を同じ実アトラスから縮小比較した。`authoring-previews/texture-resolution-comparison.png`に等倍サイズへ戻した比較を保存。

| 候補 | 観察 | 判断 |
|---|---|---|
| 64² | 顔に16²しか残らず、瞳の白ハイライト・口・頬線が混ざる。金縄の細い編みと花芯も消える | 不採用 |
| 128² | 顔32²。目の細部と狐面の朱線が部分的に潰れ、袖柄の金芯・輪郭が薄くなる | 不採用 |
| 256² | 顔64²、各材質/文様32²。1pxのハイライト・金線・縄目を維持できる | 採用 |

全6枚とも256×256 RGB、透過なし。差分は左上64²の顔だけで、肌色・目・眉・頬・口を描画した。残りは共有UV配置で、髪の束の陰影、布目、金の反射帯、鈴の切込み、斜め編み縄、桜相当の五弁花、幾何学裾、狐面朱柄を描き込んだ。複数アトラスや追加描画パスは使わない。表情はneutral/smile/happy/serious/surprised/worried。

## 編集原本とエクスポート

`shuko.bbmodel`が正本。Blockbenchで形状、UV、骨のピボット、アニメーションキーを編集して保存後、リポジトリrootから以下を実行する。

```powershell
python docs/npc/shuko/export_assets.py
```

エクスポーターは保存済み原本を読み、geo、埋込みPNG、アニメーションをゲーム資産として書き出す。原本は書き換えない。Blockbench 4系のoutliner内骨定義と5系のgroups分離形式を扱う。別名保存した原本は引数で明示できる。テクスチャの外部編集を使う場合はBlockbenchに再読込・保存し、原本の埋込みsourceを更新すること。

`build_source.py`は初期造形の再現用コード。既存原本がある場合は停止し、明示的な`--rebuild`だけが原本を作り直す。**手編集後にこれを再実行しない。** 制作中の自動形状修正で原本を再構築したが、Blockbenchでの手編集開始前に限定した。

## 骨・アニメーション契約

64 bones / 341 cubes。全ての可動部は剛体のGeckoLib骨で、ウェイトは不要。

```text
root
└ body
  ├ neck → head → hair / hair_fringe / hair_back / hair_left / hair_right
  │                ear_left / ear_right / mask → mask_ribbon
  ├ collar
  ├ sleeve_left → hand_left
  ├ sleeve_right → hand_right
  ├ shoulder_bell_left/right → shoulder_tassel_left/right
  ├ obi → obi_knot / obi_tassel_left/right / back_bow
  ├ skirt → skirt_front / skirt_back / skirt_left / skirt_right
  ├ leg_left/right → foot_left/right
  └ tail_root
     ├ tail_01 → tail_01_mid → tail_01_tip
     ├ tail_02 → tail_02_mid → tail_02_tip
     ├ tail_03 → tail_03_mid → tail_03_tip
     ├ tail_04 → tail_04_mid → tail_04_tip
     ├ tail_05 → tail_05_mid → tail_05_tip
     ├ tail_06 → tail_06_mid → tail_06_tip
     ├ tail_07 → tail_07_mid → tail_07_tip
     ├ tail_08 → tail_08_mid → tail_08_tip
     └ tail_09 → tail_09_mid → tail_09_tip
```

05が中央、01は-X側、09は+X側。1本=05、3本=04/05/06、6本=02/03/04/06/07/08、9本=全てが推奨。rootを隠せば中節・先節も一緒に隠れる。

- `animation.shuko.idle`：4秒ループ。小さい呼吸、首の傾き、袖・房、尾rootとmidの非対称な揺れ。
- `animation.shuko.walk`：1.2秒ループ。短い歩幅、控えめな袖・脇裾の動き、尾の追従。
- `animation.shuko.talk`：2.4秒ループ。頭の頷き、左袖と手の小さい身振り。表情はJava側の6PNG選択で切り替える。

## 自己監査と限界

`export_assets.py`でJSON読込み、骨名重複、尾27骨の存在、6PNG寸法・RGBを検査。`asset-validation.json`へ実測数を保存。最大2046 cube faces、追加透過面0、発光専用面0。通常は不透明1パス、常時パーティクル無しを想定する。複数体のフレーム時間は未測定。

自前ソフトウェアの正投影4方向とテクスチャ比較を`authoring-previews/`へ保存。これは**BlockbenchスクリーンショットでもMinecraft表示結果でもない**。この監査で朱の尾先が白の尾に埋まる厚み不足を発見し、白の終節を短縮し朱の外厚を増して修正した。

元PNGは小さい生成系設定画のため、細かい花形は五弁文様として、帯縄の裏側と狐面の裏は画像と矛盾しない範囲で補完。曲面や毛束はMinecraft向けの段付き直方体で近似している。参照画像の柔らかな筆致、完全な丸い鈴、衣服の物理シミュレーションは再現していない。袖・裾・尾の実ゲーム歩行中干渉、Blockbenchの表示と実際のフレーム時間は別の検証記録を参照する。
