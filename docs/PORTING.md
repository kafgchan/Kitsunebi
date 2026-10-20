# Minecraft 26.x template integration

ユーザー提供の `../mdk/` のテンプレートを、リポジトリ内の独立ビルドとして組み込みました。
元のテンプレートは変更していません。

| Minecraft | Fabric | NeoForge | Java |
| --- | --- | --- | --- |
| 26.1.2 | Loader 0.19.5 / API 0.155.3+26.1.2 | 26.1.2.122 | 25 |
| 26.2 | Loader 0.19.5 / API 0.161.0+26.2 | 26.2.0.93 | 25 |
| 26.3 | Loader 0.19.5 / API 0.164.0+26.3 | 26.3.0.94 | 25 |

各ディレクトリは `fabric-26.1.2`、`neoforge-26.1.2` のように命名しています。
テンプレート付属のWrapper（FabricはGradle 9.7.1、NeoForgeは9.2.1）を維持します。

## 実装範囲

Mod ID `kitsunebi`、作者、アイコン、LGPL、ライセンス文書同梱、ローダーの
エントリーポイント、ビルド・クライアント／サーバー起動設定を接続しています。
JAR名とMod説明に `porting preview` を記載しています。

**現段階は移植用の起動基盤です。** 狐火・祭壇・幻影建材・朱狐装備・妖術・槍・
レシピビューア・Curios等は未移植です。Architectury APIはまだ使用・要求しません。
1.21.1用コード・データを無条件に取り込まず、各バージョンでのAPI対応を分離しています。

## 操作

各ディレクトリでJava 25を使い、付属Wrapperから `build` / `runClient` / `runServer`
を実行できます。上位のWrapperからは以下を利用できます。

```powershell
.\gradlew.bat fabric2612Build
.\gradlew.bat neoforge2612Build
.\gradlew.bat fabric262Build
.\gradlew.bat neoforge262Build
.\gradlew.bat fabric263Build
.\gradlew.bat neoforge263Build

# BuildをRunClient / RunServerに置き換えると開発起動
.\gradlew.bat fabric2612RunClient
```

通常のルート `build` は従来の1.21.1向けです。26.xの依存解決失敗により1.21.1の
開発が止まらないよう、独立したWrapperへ委譲します。
依存アーティファクトが取得できない場合は、テンプレートの組み込みとビルド成功を
区別して記録します。ビルド成功だけではゲーム内の起動・機能確認済みにはなりません。

## 検証結果（2026-10-20）

- 上記6構成の個別 `build` がすべて成功。
- ルートから6つの `*Build` タスクを呼び出す統合ビルドも成功。
- バイナリ・ソースJAR計12本でライセンス4文書の同梱を確認。
- バイナリ6本でエントリーポイント、展開済みModメタデータ、アイコンを確認。
- ゲーム内での起動と、ゲームプレイ機能の移植・検証は未実施。
