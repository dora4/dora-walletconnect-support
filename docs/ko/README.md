# Dora Fund SDK 문서
![Release](https://jitpack.io/v/dora4/dora-walletconnect-support.svg)

--------------------------------

#### Gradle 종속성 구성

프로젝트 루트 디렉터리의 `settings.gradle.kts` 파일에 다음 코드를 추가하세요:
```kotlin
dependencyResolutionManagement {
    repositories {
        maven { setUrl("https://jitpack.io") }
    }
}
```
`app` 모듈의 `build.gradle.kts` 파일에 다음 코드를 추가하세요:
```kotlin
dependencies {
    // 확장 패키지는 메인 프레임워크 "dora"와 함께 사용해야 합니다.
    implementation("com.github.dora4:dora:1.3.37")
    implementation("com.github.dora4:dora-walletconnect-support:2.1.15")
}
```

#### SDK 요구 사항
```kotlin
minSdk = 23
```

#### 사용법

`AndroidManifest.xml`에 다음 설정을 추가하세요:
```xml
<application>
        <!-- Dora 라이프사이클 주입을 위한 설정 -->
        <meta-data
            android:name="dora.lifecycle.config.WalletConnectGlobalConfig"
            android:value="GlobalConfig"/>
</application>
```
`Application` 클래스의 `onCreate()` 메서드에서 다음 메서드를 호출하세요:
```kotlin
DoraFund.init(this, "App Name", "App Description", "https://yourdomain.com", arrayOf(
    EVMChains.ETHEREUM,   // 이더리움 지원
    EVMChains.POLYGON,    // 폴리곤 지원
    EVMChains.ARBITRUM,   // 아비트럼 지원
    EVMChains.AVALANCHE   // 아발란체 C-체인 지원
))
```
`Activity`에서 콜드월렛과 연결을 설정하세요:
```kotlin
DoraFund.connectWallet(this)
```
(선택 사항) 단일 `Activity`에서만 결제를 처리하는 경우, 해당 `Activity`에서 콜드월렛 결제 리스너를 설정할 수 있습니다. 만약 `Application`의 `init()` 메서드에서 `PayListener`를 등록하면, 콜백에서 메시지를 전송하여 처리 인터페이스에 알릴 수 있습니다.
```kotlin
DoraFund.setPayListener(object : DoraFund.PayListener {
    override fun onSendTransactionToBlockchain(orderId: String, transactionHash: String) {
        // 트랜잭션이 블록체인에 전송되었으며 확인 대기 중
    }

    override fun onPayFailure(orderId: String, msg: String) {
        // 결제 실패
    }
})
```
주문 데이터를 생성하고 결제를 진행하세요:
```kotlin
DoraFund.pay(this,
                "DoraPay accessKey를 입력하세요. 예: AyAD8J9M0R7H",
                "32자리 DoraPay secretKey를 입력하세요. 이는 우리 직원 포함 누구와도 공유하지 마세요.",
                "주문 정보를 입력하세요. 프레임워크가 팝업을 띄워 사용자에게 결제를 안내합니다.",
                "상품 정보를 입력하세요. 프레임워크가 팝업을 띄워 사용자에게 결제를 안내합니다.",
                "수취인의 지갑 주소를 입력하세요. 예: 0xcBa852Ef29a43a7542B88F60C999eD9cB66f6000",
                0.01,
                null,
                object: DoraFund.OrderListener {
                    override fun onPrintOrder(orderId: String, chain: Modal.Model.Chain, value: Double) {
                        // 이 주문의 거래 ID를 기록하여 이후 결제 상태 조회에 사용
                    }
                })
```
주문 결제 상태를 조회하세요:
```kotlin
// 현재 선택된 체인의 거래 조회
PayUtils.queryTransaction("이 주문의 거래 해시를 입력하세요")
// Ethereum 메인넷의 거래 조회
PayUtils.queryTransaction("이 주문의 거래 해시를 입력하세요", PayUtils.DEFAULT_RPC_ETHEREUM)
// Polygon 메인넷의 거래 조회
PayUtils.queryTransaction("이 주문의 거래 해시를 입력하세요", PayUtils.DEFAULT_RPC_POLYGON)
// Arbitrum 메인넷의 거래 조회
PayUtils.queryTransaction("이 주문의 거래 해시를 입력하세요", PayUtils.DEFAULT_RPC_ARBITRUM)
```
프로가드 규칙 추가:
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