dora-walletconnect-support
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
    implementation("com.github.dora4:dora:1.2.51")
    implementation("com.github.dora4:dora-walletconnect-support:1.22")
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
DoraTrade.init(this, "App Name", "App Description", "https://yourdomain.com", chains)
```
在Activity中与冷钱包建立连接。
```kotlin
DoraTrade.connectWallet(this)
```
在Activity中设置支付结果监听器，请提示用户不要关闭界面，等待支付完成，否则无法发货。如果PayListener在Application的
init()中注册，则在回调处发送消息给处理界面。
```kotlin
DoraTrade.setPayListener(object : DoraTrade.PayListener {
    override fun onPaySuccess() {
        // 支付成功，在此发货商品
    }

    override fun onPayFailure() {
        // 支付失败，一般为点了冷钱包的取消发送
    }
})
```
构建订单数据进行支付。
```kotlin
DoraTrade.pay(this,
                "填写朵拉支付的accessKey，如AyAD8J9M0R7H",
                "填写朵拉支付的32位secretKey，不要泄露给任何人，包括我们的工作人员",
                "填写订单信息，便于框架给你弹窗，以让用户知晓正在支付",
                "填写商品详细描述，便于框架给你弹窗，以让用户知晓正在支付",
                "填写收款方的钱包地址，如0xfF6FC0F28835F2C1FE23B15fb4488d976B06Dcd9",
                0.01)
```
另外，请录制支付教程给用户看确实能发货。被用户举报诈骗，一经核实，则永久封禁accessKey。



