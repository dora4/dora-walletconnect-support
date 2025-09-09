# Documentación del SDK Dora Fund
![Release](https://jitpack.io/v/dora4/dora-walletconnect-support.svg)

--------------------------------  

#### Configuración de dependencia en Gradle

Añade el siguiente código en el archivo `settings.gradle.kts` en la raíz del proyecto:
```kotlin
dependencyResolutionManagement {
    repositories {
        maven { setUrl("https://jitpack.io") }
    }
}
```
Añade el siguiente código en el archivo `build.gradle.kts` del módulo `app`:
```kotlin
dependencies {
    // El paquete de extensión debe usarse con el framework principal "dora"
    implementation("com.github.dora4:dora:1.3.14")
    implementation("com.github.dora4:dora-walletconnect-support:2.0.10")
}
```

#### Requisitos del SDK
```kotlin
minSdk = 23
```

#### Uso

Añade la siguiente configuración en `AndroidManifest.xml`:
```xml
<application>
        <!-- Configuración para la inyección del ciclo de vida de Dora -->
        <meta-data
            android:name="dora.lifecycle.config.WalletConnectGlobalConfig"
            android:value="GlobalConfig"/>
</application>
```
En el método `onCreate()` de la clase `Application`, llama al siguiente método:
```kotlin
// Especificar las cadenas compatibles con Ethereum usando chainId
val chains: Array<Modal.Model.Chain> = arrayOf(
            Web3ModalChainsPresets.ethChains["1"]!!,      // Soporta Ethereum
            Web3ModalChainsPresets.ethChains["137"]!!,    // Soporta Polygon
            Web3ModalChainsPresets.ethChains["42161"]!!   // Soporta Arbitrum
)
DoraFund.init(this, "Nombre de la aplicación", "Descripción de la aplicación", "https://yourdomain.com", chains)
```
Establece la conexión con la billetera fría en una `Activity`:
```kotlin
DoraFund.connectWallet(this)
```
(Opcional) Si solo una `Activity` maneja los pagos, puedes configurar un oyente de pago de billetera fría en esa `Activity`. Si `PayListener` está registrado en el método `init()` de `Application`, puedes enviar mensajes dentro del callback para notificar a la interfaz de procesamiento.
```kotlin
DoraFund.setPayListener(object : DoraFund.PayListener {
    override fun onSendTransactionToBlockchain(orderId: String, transactionHash: String) {
        // La transacción ha sido enviada a la blockchain y está pendiente de confirmación
    }

    override fun onPayFailure(orderId: String, msg: String) {
        // Pago fallido
    }
})
```
Construye los datos de la orden y procede con el pago:
```kotlin
DoraFund.pay(this,
                "Introduce la clave de acceso de DoraPay, por ejemplo, AyAD8J9M0R7H",
                "Introduce la clave secreta de 32 caracteres de DoraPay. No la compartas con nadie, incluido nuestro personal.",
                "Introduce la información del pedido para que el framework muestre una ventana emergente informando al usuario sobre el pago.",
                "Introduce los detalles del producto para que el framework muestre una ventana emergente informando al usuario sobre el pago.",
                "Introduce la dirección de la billetera del destinatario, por ejemplo, 0xcBa852Ef29a43a7542B88F60C999eD9cB66f6000",
                0.01,
                object: DoraFund.OrderListener {
                    override fun onPrintOrder(orderId: String, chain: Modal.Model.Chain, value: Double) {
                        // Registra este ID de transacción para futuras consultas sobre el estado del pago
                    }
                })
```
Consulta el estado del pago de la orden:
```kotlin
// Consultar la transacción de la cadena seleccionada actualmente
PayUtils.queryTransaction("Rellena el hash de la transacción de este pedido")
// Consultar la transacción en la red principal de Ethereum
PayUtils.queryTransaction("Rellena el hash de la transacción de este pedido", PayUtils.DEFAULT_RPC_ETHEREUM)
// Consultar la transacción en la red principal de Polygon
PayUtils.queryTransaction("Rellena el hash de la transacción de este pedido", PayUtils.DEFAULT_RPC_POLYGON)
// Consultar la transacción en la red principal de Arbitrum
PayUtils.queryTransaction("Rellena el hash de la transacción de este pedido", PayUtils.DEFAULT_RPC_ARBITRUM)
```
Agregar reglas de ofuscación:
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