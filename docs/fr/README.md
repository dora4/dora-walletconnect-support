# Documentation du SDK Dora Fund
![Release](https://jitpack.io/v/dora4/dora-walletconnect-support.svg)

--------------------------------  

#### Configuration de la dépendance Gradle

Ajoutez le code suivant dans le fichier `settings.gradle.kts` à la racine du projet :
```kotlin
dependencyResolutionManagement {
    repositories {
        maven { setUrl("https://jitpack.io") }
    }
}
```
Ajoutez le code suivant dans le fichier `build.gradle.kts` du module `app` :
```kotlin
dependencies {
    // Le package d'extension doit être utilisé avec le framework principal "dora"
    implementation("com.github.dora4:dora:1.3.14")
    implementation("com.github.dora4:dora-walletconnect-support:1.160")
}
```

#### Exigences du SDK
```kotlin
minSdk = 23
```

#### Utilisation

Ajoutez la configuration suivante dans `AndroidManifest.xml` :
```xml
<application>
        <!-- Configuration pour l'injection du cycle de vie Dora -->
        <meta-data
            android:name="dora.lifecycle.config.WalletConnectGlobalConfig"
            android:value="GlobalConfig"/>
</application>
```
Dans la méthode `onCreate()` de la classe `Application`, appelez la méthode suivante :
```kotlin
// Spécifiez les chaînes compatibles avec Ethereum à l'aide de chainId
val chains: Array<Modal.Model.Chain> = arrayOf(
            Web3ModalChainsPresets.ethChains["1"]!!,      // Supporte Ethereum
            Web3ModalChainsPresets.ethChains["137"]!!,    // Supporte Polygon
            Web3ModalChainsPresets.ethChains["42161"]!!   // Supporte Arbitrum
)
DoraTrade.init(this, "Nom de l'application", "Description de l'application", "https://yourdomain.com", chains)
```
Établissez une connexion avec le portefeuille froid dans une `Activity` :
```kotlin
DoraTrade.connectWallet(this)
```
(Facultatif) Si une seule `Activity` gère les paiements, vous pouvez configurer un écouteur de paiement pour le portefeuille froid dans cette `Activity`. Si `PayListener` est enregistré dans la méthode `init()` de `Application`, vous pouvez envoyer des messages via le rappel pour notifier l'interface de traitement.
```kotlin
DoraTrade.setPayListener(object : DoraTrade.PayListener {
    override fun onSendTransactionToBlockchain(orderId: String, transactionHash: String) {
        // La transaction a été envoyée sur la blockchain et est en attente de confirmation
    }

    override fun onPayFailure(orderId: String, msg: String) {
        // Échec du paiement
    }
})
```
Créez les données de commande et procédez au paiement :
```kotlin
DoraTrade.pay(this,
                "Entrez la clé d'accès DoraPay, par exemple AyAD8J9M0R7H",
                "Entrez la clé secrète DoraPay à 32 caractères. Ne la partagez avec personne, y compris notre personnel.",
                "Entrez les informations de commande, afin que le framework affiche une fenêtre contextuelle informant l'utilisateur du paiement.",
                "Entrez les détails du produit, afin que le framework affiche une fenêtre contextuelle informant l'utilisateur du paiement.",
                "Entrez l'adresse du portefeuille du destinataire, par exemple 0xcBa852Ef29a43a7542B88F60C999eD9cB66f6000",
                0.01,
                object: DoraTrade.OrderListener {
                    override fun onPrintOrder(orderId: String, chain: Modal.Model.Chain, value: Double) {
                        // Enregistrez cet ID de transaction pour interroger ultérieurement l'état du paiement
                    }
                })
```
Interrogez l'état du paiement de la commande :
```kotlin
// Rechercher la transaction de la chaîne actuellement sélectionnée
PayUtils.queryTransactionByHash("Remplissez le hachage de la transaction de cette commande")
// Rechercher la transaction sur le réseau principal Ethereum
PayUtils.queryTransactionByHash("Remplissez le hachage de la transaction de cette commande", PayUtils.DEFAULT_RPC_ETHEREUM)
// Rechercher la transaction sur le réseau principal Polygon
PayUtils.queryTransactionByHash("Remplissez le hachage de la transaction de cette commande", PayUtils.DEFAULT_RPC_POLYGON)
// Rechercher la transaction sur le réseau principal Arbitrum
PayUtils.queryTransactionByHash("Remplissez le hachage de la transaction de cette commande", PayUtils.DEFAULT_RPC_ARBITRUM)
```
Ajouter des règles Proguard :
```pro
-keep class org.json.JSONObject { *; }
-keep class dora.trade.DoraTrade { *; }
-keep class dora.trade.DoraTrade$PayListener { *; }
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