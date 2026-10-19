# Fabric通信レビューの再現と修正

2026-10-20 / Architectury API 13.0.8 / Minecraft 1.21.1

## 再現

専用Fabricサーバーでクライアント初期化を行わずに空中歩行状態通知を送信経路へ渡すと、ArchitecturyのS2CコーデックがnullのためNullPointerException。修正前ログはbuild/fabric-network-before.log。

## 修正

KitsunebiFabric.onInitializeで、物理環境がSERVERの場合にregisterS2CPayloadTypeを実行する。CLIENT環境では既存のClientModInitializerが受信登録するため、統合サーバーで二重登録しない。

## 検証結果

- :fabric:runNetworkVerifyServer：専用サーバーだけで開始・解除の状態パケットを生成・復号。Fabric S2CコーデックとC2S三種の登録確認がPASS。
- -PverifyNetworkConnection=trueで専用サーバーを維持し、:fabric:runNetworkVerifyClientから127.0.0.1:25587へ実接続。足袋装備の同期後、バニラしゃがみパケットとC2S開始要求を送信し、S2C承認でクライアントの空中歩行状態が有効になることを確認。再度C2S要求を送りS2C解除で無効になることを確認。両方向の実パケット往復がPASS。
- クライアントはJEIを含む通常のFabric構成。検証用ファイル・ワールドはfabric/build/network-verify-serverおよびnetwork-verify-clientに分離。通常起動では検証フラグがないため実行されない。
- 通信検証結果がFAILまたは欠落した場合はGradleタスクも失敗する。

結果：fabric/build/network-verify-server/network-verification.txt、fabric/build/network-verify-client/network-client-verification.txt。
ログ：build/fabric-connection-server.log、build/fabric-connection-client.log。

実行には専用テストディレクトリのserver.propertiesをlocalhost:25587、offline-mode（online-mode=false）に設定し、EULAを準備する。通常のプレイ用サーバー設定・セーブには変更を加えない。テスト用プレイヤーに足袋を配布・位置を変更する処理は接続検証フラグが有効な場合だけ実行される。
