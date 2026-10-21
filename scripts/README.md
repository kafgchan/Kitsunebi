# Shared build and launch commands

Gemini / Antigravity / Codexで共通利用する入口です。作業ディレクトリに依存せず、各スクリプト自身からリポジトリ位置を解決します。個人の絶対パスは含めません。

Java 25をインストールし、`JAVA_HOME`を設定してください。ルートWrapperが必要なJava 17 / 21のツールチェーンを解決します。初回はGradle・依存Mod等のダウンロードが必要です。
PowerShellでリポジトリ直下から実行します。

```powershell
# Forge 1.20.1（引数省略時）
.\Build.ps1
.\RunClient.ps1
.\RunServer.ps1

# 1.21.1
.\Build.ps1 -Target fabric-1.21.1
.\RunClient.ps1 -Target neoforge-1.21.1 -RecipeViewer jei -Curios
.\RunClient.ps1 -Target fabric-1.21.1 -RecipeViewer emi

# 実装済み3構成を順にビルド
.\Build.ps1 -Target all

# 26.xは起動基盤のみ。明示した場合だけビルド
.\Build.ps1 -Target fabric-26.2
.\RunClient.ps1 -Target neoforge-26.3
```

`-Target`は `forge-1.20.1`、`fabric-1.21.1`、`neoforge-1.21.1`、およびFabric/NeoForgeの26.1.2・26.2・26.3です。
`all`はビルド専用で、26.xを含めません。
`-RecipeViewer`は `none`（既定）/ `jei` / `emi` / `rei`。Curiosは `-Curios` 指定時のみ。FabricのCurios連携は対象外、26.xの任意Mod連携は未実装です。
実装本体は [Invoke-Kitsunebi.ps1](Invoke-Kitsunebi.ps1)、JAR配置処理は [stage-release.gradle](../gradle/stage-release.gradle) です。

スクリプト実行がローカルの実行ポリシーで制限される場合、同じ引数で次のように実行できます（恒久的な設定変更は不要）。

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\Build.ps1 -Target forge-1.20.1
```

通常のGradleコマンドも継続利用できます。

```powershell
.\gradlew.bat forge1201Build
.\gradlew.bat :fabric-1.21.1:build
.\gradlew.bat :neoforge-1.21.1:runClient
```

ビルド成功時に `releases/` へ配布JAR・ソースJAR・SHA256SUMS.txtをコピーします。失敗時には更新しません。
ランチャーはMinecraftサーバーのEULAに自動同意しません。通常の開発ワールドは各モジュールの `run/` を使用します。

起動せず、呼び出すGradleコマンドだけを確認する場合：

```powershell
.\RunClient.ps1 -Target forge-1.20.1 -RecipeViewer jei -Curios -DryRun
```

現在のNeoForge 1.21.1ソースはGeckoLib 4.9.3の描画仮実装を含むため、NeoForgeビルドの出力先は `releases/preview/1.21.1/neoforge/` です。`RunClient.ps1 -Target neoforge-1.21.1` はGradleがGeckoLibを解決します。配布JARを通常ランチャーで使う場合はGeckoLib 4.9.3とArchitectury APIを別途導入してください。
