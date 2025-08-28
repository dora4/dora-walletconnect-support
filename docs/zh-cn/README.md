# 朵拉支付 SDK 文档
![Release](https://jitpack.io/v/dora4/dora-walletconnect-support.svg)

--------------------------------

#### Gradle依赖配置

添加以下代码到项目根目录下的settings.gradle.kts
```kotlin
dependencyResolutionManagement {
    repositories {
        maven { setUrl("https://jitpack.io") }
    }
}
```
添加以下代码到app模块的build.gradle.kts
```kotlin
dependencies {
    // 扩展包必须在有主框架dora的情况下使用
    implementation("com.github.dora4:dora:1.3.14")
    implementation("com.github.dora4:dora-walletconnect-support:2.0.6")
}
```

#### SDK要求

```kotlin
minSdk = 23
```

#### 使用方式

在AndroidManifest中加入配置。
```xml
<application>
        <!-- Dora生命周期注入的配置 -->
        <meta-data
            android:name="dora.lifecycle.config.WalletConnectGlobalConfig"
            android:value="GlobalConfig"/>
</application>
```
在Application类的onCreate()中调用。
```kotlin
// 通过chainId指定支持的以太坊兼容链
val chains: Array<Modal.Model.Chain> = arrayOf(
            Web3ModalChainsPresets.ethChains["1"]!!,      // 支持Ethereum
            Web3ModalChainsPresets.ethChains["137"]!!,    // 支持Polygon
            Web3ModalChainsPresets.ethChains["42161"]!!   // 支持Arbitrum
)
DoraFund.init(this, "App Name", "App Description", "https://yourdomain.com", chains)
```
在Activity中与冷钱包建立连接。
```kotlin
DoraFund.connectWallet(this)
```
（非必须）如果只有一个Activity进行支付，可在Activity中设置冷钱包支付的监听器。如果PayListener在Application的
init()中注册，则在回调处发送消息给处理界面。
```kotlin
DoraFund.setPayListener(object : DoraFund.PayListener {
    override fun onSendTransactionToBlockchain(orderId: String, transactionHash: String) {
        // 转账消息上链，确认中
    }

    override fun onPayFailure(orderId: String, msg: String) {
        // 支付失败
    }
})
```
构建订单数据进行支付。
```kotlin
DoraFund.pay(this,
                "填写朵拉支付的accessKey，如AyAD8J9M0R7H",
                "填写朵拉支付的32位secretKey，不要泄露给任何人，包括我们的工作人员",
                "填写订单信息，便于框架给你弹窗，以让用户知晓正在支付",
                "填写商品详细描述，便于框架给你弹窗，以让用户知晓正在支付",
                "填写收款方的钱包地址，如0xcBa852Ef29a43a7542B88F60C999eD9cB66f6000",
                0.01,
                object: DoraFund.OrderListener {
                    override fun onPrintOrder(orderId: String, chain: Modal.Model.Chain, value: Double) {
                        // 在此记录该笔订单的交易订单号，便于以后查询支付状态
                    }
                })
```
查询订单支付情况。

```kotlin
// 查询当前选中链的交易
PayUtils.queryTransaction("填写该笔订单的交易哈希")
// 查询Ethereum主网的交易
PayUtils.queryTransaction("填写该笔订单的交易哈希", PayUtils.DEFAULT_RPC_ETHEREUM)
// 查询Polygon主网的交易
PayUtils.queryTransaction("填写该笔订单的交易哈希", PayUtils.DEFAULT_RPC_POLYGON)
// 查询Arbitrum主网的交易
PayUtils.queryTransaction("填写该笔订单的交易哈希", PayUtils.DEFAULT_RPC_ARBITRUM)
```
添加混淆规则。
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


