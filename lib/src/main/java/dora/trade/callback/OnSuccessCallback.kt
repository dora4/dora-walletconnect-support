package dora.trade.callback

import com.walletconnect.web3.modal.client.models.request.SentRequestResult

interface OnSuccessCallback {
    fun onSuccess(result: SentRequestResult)
}