# Dora Fund SDK Doc
![Release](https://jitpack.io/v/dora4/dora-walletconnect-support.svg)

--------------------------------

#### Gradle Dependency Configuration

Add the following code to the `settings.gradle.kts` file in the project root directory:
```kotlin
dependencyResolutionManagement {
    repositories {
        maven { setUrl("https://jitpack.io") }
    }
}
```
Add the following code to the `build.gradle.kts` file in the `app` module:
```kotlin
dependencies {
    // The extension package must be used with the main framework "dora"
    implementation("com.github.dora4:dora:1.3.43")
    implementation("com.github.dora4:dora-walletconnect-support:2.1.29")
}
```

#### SDK Requirements

```kotlin
minSdk = 23
```

#### Usage

Add the following configuration to `AndroidManifest.xml`:
```xml
<application>
        <!-- Configuration for Dora lifecycle injection -->
        <meta-data
            android:name="dora.lifecycle.config.WalletConnectGlobalConfig"
            android:value="GlobalConfig"/>
</application>
```
Call the following method in the `onCreate()` method of the `Application` class:
```kotlin
DoraFund.init(this, "App Name", "App Description", "https://yourdomain.com", arrayOf(
    EVMChains.ETHEREUM,   // Support Ethereum
    EVMChains.POLYGON,    // Support Polygon
    EVMChains.ARBITRUM,   // Support Arbitrum
    EVMChains.AVALANCHE   // Support Avalanche C-Chain
))
```
Establish a connection with the cold wallet in an `Activity`:
```kotlin
DoraFund.connectWallet(this)
```
(Optional) If only a single `Activity` handles payments, you can set a cold wallet payment listener in that `Activity`. If `PayListener` is registered in the `init()` method of the `Application`, you can send messages in the callback to notify the processing interface.
```kotlin
DoraFund.setPayListener(object : DoraFund.PayListener {
    override fun onSendTransactionToBlockchain(orderId: String, transactionHash: String) {
        // Transaction sent to the blockchain, pending confirmation
    }

    override fun onPayFailure(orderId: String, msg: String) {
        // Payment failed
    }
})
```
Construct order data to proceed with the payment:
```kotlin
DoraFund.pay(this,
                "Enter the DoraPay accessKey, e.g., AyAD8J9M0R7H",
                "Enter the 32-character DoraPay secretKey. Do not share it with anyone, including our staff",
                "Enter the order information, so the framework can display a popup informing the user about the payment",
                "Enter the product details, so the framework can display a popup informing the user about the payment",
                "Enter the recipient's wallet address, e.g., 0xcBa852Ef29a43a7542B88F60C999eD9cB66f6000",
                0.01,
                null,
                object: DoraFund.OrderListener {
                    override fun onPrintOrder(orderId: String, chain: Modal.Model.Chain, value: Double) {
                        // Record this order transaction ID for future payment status queries
                    }
                })
```
Query the order payment status:
```kotlin
// Query the transaction of the currently selected chain  
PayUtils.queryTransaction("Fill in the transaction hash of this order")
// Query the transaction on the Ethereum mainnet  
PayUtils.queryTransaction("Fill in the transaction hash of this order", PayUtils.DEFAULT_RPC_ETHEREUM)
// Query the transaction on the Polygon mainnet  
PayUtils.queryTransaction("Fill in the transaction hash of this order", PayUtils.DEFAULT_RPC_POLYGON)
// Query the transaction on the Arbitrum mainnet  
PayUtils.queryTransaction("Fill in the transaction hash of this order", PayUtils.DEFAULT_RPC_ARBITRUM)
```
Add proguard rules:
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