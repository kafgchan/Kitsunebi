# Shared release output

ビルドが成功すると、対応する配布JAR・ソースJAR・SHA256SUMS.txtが自動配置されます。

```text
releases/
  1.20.1/forge/1.0.0/
  1.21.1/fabric/1.0.0/
  1.21.1/neoforge/1.0.0/
  preview/26.1.2/fabric/1.0.0/   # 移植途中。実際のModバージョンは各設定に従う
```

同じMinecraft・ローダー・Modバージョンのファイルは次回ビルドで更新されます。他バージョンは保持します。
26.xはゲームプレイ機能未移植のため `preview/` 配下です。配布先を選ぶ際は取り違えないでください。
ファイル名に `-sources` が付くものは開発用ソースです。通常の導入には付かない方を使用します。

生成JARとチェックサムはGit管理から除外し、フォルダーの説明・ビルド手順・スクリプトを共有します。
共同作業中の実ファイル受け渡しにはこのフォルダーを利用し、公開配布時には検証済みの対象だけを選んでください。

ビルド・起動の共通手順は [scripts/README.md](../scripts/README.md) を参照してください。

NeoForge 1.21.1のGeckoLib描画仮実装も `preview/1.21.1/neoforge/` へ配置します。`gecko-preview` JARはGeckoLib 4.9.3が別途必須です。通常版と同時に導入しないでください。
