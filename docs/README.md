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
    implementation("com.github.dora4:dora:1.2.51")
    implementation("com.github.dora4:dora-walletconnect-support:1.36")
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
// Specify supported Ethereum-compatible chains using chainId
val chains: Array<Modal.Model.Chain> = arrayOf(
            Web3ModalChainsPresets.ethChains["1"]!!,      // Supports Ethereum
            Web3ModalChainsPresets.ethChains["137"]!!,    // Supports Polygon
            Web3ModalChainsPresets.ethChains["42161"]!!   // Supports Arbitrum
)
DoraTrade.init(this, "App Name", "App Description", "https://yourdomain.com", chains)
```
Establish a connection with the cold wallet in an `Activity`:
```kotlin
DoraTrade.connectWallet(this)
```
(Optional) If only a single `Activity` handles payments, you can set a cold wallet payment listener in that `Activity`. If `PayListener` is registered in the `init()` method of the `Application`, you can send messages in the callback to notify the processing interface.
```kotlin
DoraTrade.setPayListener(object : DoraTrade.PayListener {
    override fun onSendTransactionToBlockchain(orderId: String, transactionHash: String) {
        // Transaction sent to the blockchain, pending confirmation
    }

    override fun onPayFailure(orderId: String, transactionHash: String) {
        // Payment failed
    }
})
```
Construct order data to proceed with the payment:
```kotlin
DoraTrade.pay(this,
                "Enter the DoraPay accessKey, e.g., AyAD8J9M0R7H",
                "Enter the 32-character DoraPay secretKey. Do not share it with anyone, including our staff",
                "Enter the order information, so the framework can display a popup informing the user about the payment",
                "Enter the product details, so the framework can display a popup informing the user about the payment",
                "Enter the recipient's wallet address, e.g., 0xcBa852Ef29a43a7542B88F60C999eD9cB66f6000",
                0.01,
                object: DoraTrade.OrderListener {
                    override fun onPrintOrder(orderId: String) {
                        // Record this order transaction ID for future payment status queries
                    }
                })
```
Query the order payment status:
```kotlin
// Query a transaction on the Ethereum mainnet
PayUtils.queryTransaction("Enter the transaction order ID")
// Query a transaction on the Polygon mainnet
PayUtils.queryTransaction("Enter the transaction order ID", PayUtils.DEFAULT_RPC_POLYGON)
// Query a transaction on the Arbitrum mainnet
PayUtils.queryTransaction("Enter the transaction order ID", PayUtils.DEFAULT_RPC_ARBITRUM)
```
Add proguard rules:
# Keep the Web3Modal class
-keep class com.walletconnect.web3.modal.client.Web3Modal { *; }
# Keep the Request class
-keep class com.walletconnect.web3.modal.client.models.request.Request { *; }
# Keep the Modal$Params$Init class
-keep class com.walletconnect.web3.modal.client.Modal$Params$Init { *; }
# Keep the AppMetaData class
-keep class com.walletconnect.android.Core$Model$AppMetaData { *; }
# Keep the CoreClient class
-keep class com.walletconnect.android.CoreClient { *; }
# Keep the ConnectionType class
-keep class com.walletconnect.android.relay.ConnectionType { *; }
