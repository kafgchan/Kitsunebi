# 朱狐装備・アクセサリー・祭壇レシピ連携

2026-10-19 / Minecraft 1.21.1 NeoForge・Fabric

- 装備・ツール・仮面・御幣の表示名を朱狐（Shuko / 슈코）に統一。内部のkitsunebi IDはセーブ互換のため維持。狐火素材・狐火ブロック・祭壇の名称は維持。
- 空中歩行は水・泡に触れても解除しない。溶岩・実地形への着地など従来の解除条件は維持。
- CuriosはNeoForge 9.5.1+1.21.1に対応。仮面をheadタグへ追加し、プレイヤーへhead 1枠を付与。装備可否・可視化機能・頭追従描画に対応。FabricにCuriosは読み込まない（既存のAccessories/Trinkets判定は維持）。
- JEI 19.21.0.247 / REI 16.0.799 / EMI 1.1.24+1.21.1の任意連携。素材代替・必要数・出力数・祭壇触媒を登録。祭壇製作34件と修理例18件を専用カテゴリへ表示。
- 祭壇製作は既存サーバーエンジンから素材数を導出。半ブロック等は元のバッチ比率で表示。修理例は耐久4,096/32,768を消費した入力で示す。実際の修理は従来エンジンが制御。
- 色変更など通常クラフトJSONは各ビューアの標準クラフトカテゴリに自動登録。祭壇専用レシピを作業台へ追加しない。レシピ自動投入ボタンは未実装。
- 開発環境はJEI標準、NeoForgeにはCuriosも追加。`-PrecipeViewer=rei` / `emi` / `none`で切替。ビューアとCuriosは配布JARへ内包しないため、利用者は対応Modを別途導入する。

## API確認元

- [Curios公式ソース](https://github.com/TheIllusiveC4/Curios/tree/1.21.x)
- [JEI公式ソース](https://github.com/mezz/JustEnoughItems/tree/1.21.x)
- [REI公式ソース](https://github.com/shedaniel/RoughlyEnoughItems)
- [EMI公式ソース](https://github.com/emilyploszaj/emi)

## 検証

隔離NeoForgeサーバーで52件を祭壇エンジンの実出力と照合。Curios実インベントリでheadへの仮面装着と可視化、水中での空中歩行維持、既存の移動・採掘・農業・色変更・GUIメニューの回帰検証を行う。

クライアント検証は `:neoforge:runCompatVerifyClient`。`neoforge/build/compat-verify-client`内の専用ワールドを使用し、ビューア別に52件登録とレシピ画面表示を確認する。結果は`compat-<viewer>.txt`、描画は`screenshots/altar-<viewer>.png`へ保存。

### 実行結果

- NeoForge隔離サーバー：52件のエンジン照合、Curios head装着・可視化、水中の空中歩行、既存回帰検証がPASS。
- NeoForge実クライアント：JEI / REI / EMIをそれぞれ単独導入し、祭壇カテゴリの登録・描画がPASS。JEIはCuriosなしでも起動・表示PASS。
- Fabric：3種のアダプターを含むコンパイル・remapJar成功。2026-10-20にFabric実クライアントでもJEIとEMIをそれぞれ単独導入し、52件登録・祭壇カテゴリ描画を確認。CuriosはFabricに導入しない。
- NeoForge/Fabric最終JAR生成成功。Curios非導入の確認用には`-PcuriosEnabled=false`を指定可能。

### 2026-10-20 起動・IDE修正

Fabric開発起動時の防具モデル不足を修正。配布JARだけに共通クライアントclassをコピーする方式をやめ、Fabric client source setで共通モデル・画面・スロープをコンパイルする。JEI/EMIでワールドへ入り祭壇52件を閲覧する実起動検証がPASS。

共有アダプターのcompatをGradleサブプロジェクトとして登録し、IDE用Eclipse/JDTクラスパスを生成。CuriosはNeoForgeのみ。NeoForgeが非推奨にしたFabric互換ブリッジは、共有APIで置換できない箇所に限定して警告抑止を明記。EMI用狐火素材タグの6言語名を追加。

祭壇は不定形モデルに合わせnoOcclusionを設定。隣接する石の六方向すべての面が描画対象になる回帰テストを追加。これにより壁や地面の面が消えて地形内部が見える問題を修正。

### 2026-10-20 IDE出力の競合修正

添付診断108件の起点はcommon/bin/mainのリソース競合。JDT専用出力を各プロジェクトのbuild/jdt/classesへ分離し、compatからcommonへの明示的なEclipseプロジェクト参照・ビルド順を追加。Eclipse/JDT設定を再生成し、common/compat/NeoForge/FabricのGradleコンパイル成功を確認。

共有コードのAPI非推奨差分に必要なSuppressWarningsに対し、common側だけで不要扱いになる警告トークンの設定を整備。1.20.1 MDKサンプルの未使用importとResourceLocationコンストラクターも互換APIへ修正。

IDEのjdt_wsログに複数の.indexファイル欠落（Failed to save JDT index / Failed to update qualified index）が残っている。実行中のIDEキャッシュは削除していない。残存診断の更新には、Antigravity IDEでJava: Clean Java Language Server Workspaceを実行し再起動する必要がある。IDE画面上の診断0件までは未確認。
