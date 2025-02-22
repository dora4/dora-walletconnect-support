package dora.trade

import com.walletconnect.android.CoreClient
import com.walletconnect.web3.modal.client.Modal

object ModalParamsHelper {

    @JvmStatic
    fun createInitParams(
        coreClient: CoreClient,
        excludedWalletIds: List<String>,
        recommendedWalletsIds: List<String>,
        coinbaseEnabled: Boolean,
        enableAnalytics: Boolean?
    ): Modal.Params.Init {
        return Modal.Params.Init(
            coreClient,
            excludedWalletIds,
            recommendedWalletsIds,
            coinbaseEnabled,
            enableAnalytics
        )
    }
}
