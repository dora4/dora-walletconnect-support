# 朵拉支付 SDK 文檔
![Release](https://jitpack.io/v/dora4/dora-walletconnect-support.svg)

--------------------------------

#### Gradle 依賴配置

在專案根目錄的 `settings.gradle.kts` 檔案中添加以下代碼：
```kotlin
dependencyResolutionManagement {
    repositories {
        maven { setUrl("https://jitpack.io") }
    }
}
```
在 `app` 模組的 `build.gradle.kts` 檔案中添加以下代碼：
```kotlin
dependencies {
    // 擴展包必須與主框架 "dora" 一起使用
    implementation("com.github.dora4:dora:1.3.14")
    implementation("com.github.dora4:dora-walletconnect-support:2.0.4")
}
```

#### SDK 要求
```kotlin
minSdk = 23
```

#### 使用方式

在 `AndroidManifest.xml` 中添加以下配置：
```xml
<application>
        <!-- Dora 生命周期注入的配置 -->
        <meta-data
            android:name="dora.lifecycle.config.WalletConnectGlobalConfig"
            android:value="GlobalConfig"/>
</application>
```
在 `Application` 類的 `onCreate()` 方法中調用以下方法：
```kotlin
// 透過 chainId 指定支援的 Ethereum 相容鏈
val chains: Array<Modal.Model.Chain> = arrayOf(
            Web3ModalChainsPresets.ethChains["1"]!!,      // 支援 Ethereum
            Web3ModalChainsPresets.ethChains["137"]!!,    // 支援 Polygon
            Web3ModalChainsPresets.ethChains["42161"]!!   // 支援 Arbitrum
)
DoraFund.init(this, "應用名稱", "應用描述", "https://yourdomain.com", chains)
```
在 `Activity` 中與冷錢包建立連接：
```kotlin
DoraFund.connectWallet(this)
```
(選填) 若僅有單個 `Activity` 負責處理支付，可以在該 `Activity` 設置冷錢包支付監聽器。若在 `Application` 的 `init()` 方法中註冊了 `PayListener`，則可在回調中發送訊息通知處理介面。
```kotlin
DoraFund.setPayListener(object : DoraFund.PayListener {
    override fun onSendTransactionToBlockchain(orderId: String, transactionHash: String) {
        // 交易已發送至區塊鏈，等待確認
    }

    override fun onPayFailure(orderId: String, msg: String) {
        // 支付失敗
    }
})
```
構造訂單數據並執行支付：
```kotlin
DoraFund.pay(this,
                "輸入 DoraPay accessKey，例如 AyAD8J9M0R7H",
                "輸入 32 位 DoraPay secretKey，請勿與任何人分享，包括我們的員工",
                "輸入訂單信息，以便框架彈出窗口通知用戶支付",
                "輸入商品詳情，以便框架彈出窗口通知用戶支付",
                "輸入收款方錢包地址，例如 0xcBa852Ef29a43a7542B88F60C999eD9cB66f6000",
                0.01,
                object: DoraFund.OrderListener {
                    override fun onPrintOrder(orderId: String, chain: Modal.Model.Chain, value: Double) {
                        // 記錄此訂單交易 ID，以供後續查詢支付狀態
                    }
                })
```
查詢訂單支付狀態：
```kotlin
// 查詢當前選中鏈的交易
PayUtils.queryTransaction("填寫該筆訂單的交易哈希")
// 查詢Ethereum主網的交易
PayUtils.queryTransaction("填寫該筆訂單的交易哈希", PayUtils.DEFAULT_RPC_ETHEREUM)
// 查詢Polygon主網的交易
PayUtils.queryTransaction("填寫該筆訂單的交易哈希", PayUtils.DEFAULT_RPC_POLYGON)
// 查詢Arbitrum主網的交易
PayUtils.queryTransaction("填寫該筆訂單的交易哈希", PayUtils.DEFAULT_RPC_ARBITRUM)
```
添加混淆規則：
```pro
-keep class org.json.JSONObject { *; }
-keep class dora.pay.DoraFund { *; }
-keep class dora.pay.DoraFund$PayListener { *; }
-keep class org.web3j.** { *; }
-keep class com.walletconnect.web3.modal.client.Web3Modal { *; }
-keep class com.walletconnect.web3.modal.client.models.request.Request { *; }
-keep class com.walletconnect.web3.modal.client.Modal$Params$Init { *; }
-keep class com.walletconnect.web3.modal.client.Modal$Model$SessionRequestResponse { *; }
-keep class com.walletconnect.web3.modal.client.Modal$Model$JsonRpcResponse$JsonRpcResult { *; }
-keep class com.walletconnect.web3.modal.client.Modal$Model$JsonRpcResponse$JsonRpcError { *; }
-keep class com.walletconnect.android.Core$Model$AppMetaData { *; }
-keep class com.walletconnect.android.CoreClient { *; }
-keep class com.walletconnect.android.relay.ConnectionType { *; }
```