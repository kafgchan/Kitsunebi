# Kitsunebi — マルチローダー基盤設計書

## 1. モジュール構成

```text
Kitsunebi-mod/
├── common/             # Minecraft バニラコード・共通ロジック・共通リソース (assets)
│   ├── src/main/java/net/kafgchan/kitsunebi/
│   │   ├── block/      # KitsunebiBlock（透明・当たり判定なし・水中設置）
│   │   ├── item/       # KitsunebiItem（空中設置）、KitsunebiToolItem（御幣）
│   │   ├── platform/   # Services, IPlatformHelper（ServiceLoaderパターン）
│   │   └── registry/   # ModBlocks, ModItems, ModCreativeTabs
│   └── src/main/resources/assets/kitsunebi/
├── neoforge/           # NeoForge 固有コード（@Mod、イベント、レジストリ）
│   ├── src/main/java/net/kafgchan/kitsunebi/neoforge/
│   │   ├── KitsunebiNeoForge.java
│   │   ├── client/KitsunebiNeoForgeClient.java（半透明描画設定）
│   │   └── platform/NeoForgePlatformHelper.java（DeferredRegister実装）
│   └── src/main/resources/META-INF/services/
├── fabric/             # Fabric 固有コード（ModInitializer、クライアント設定）
│   ├── src/main/java/net/kafgchan/kitsunebi/fabric/
│   │   ├── KitsunebiFabric.java
│   │   └── platform/FabricPlatformHelper.java（Registry.register実装）
│   ├── src/client/java/net/kafgchan/kitsunebi/fabric/client/
│   │   └── KitsunebiFabricClient.java（半透明描画設定）
│   └── src/main/resources/META-INF/services/
├── docs/               # プロジェクト仕様書
├── gradle/             # Gradle 9.7.1 ラッパー
├── settings.gradle     # プロジェクト構成定義
├── build.gradle        # ルートビルド定義
└── gradle.properties   # バージョン定義・共通プロパティ
```

## 2. プラットフォーム抽象化レイヤー (PlatformHelper)

ServiceLoader を利用して、ローダー依存の登録・処理を共通コードから透過的に呼び出す。

- インターフェース: `net.kafgchan.kitsunebi.platform.services.IPlatformHelper`
- 共通アクセサ: `net.kafgchan.kitsunebi.platform.Services`
- NeoForge 実装: `net.kafgchan.kitsunebi.neoforge.platform.NeoForgePlatformHelper`
- Fabric 実装: `net.kafgchan.kitsunebi.fabric.platform.FabricPlatformHelper`
