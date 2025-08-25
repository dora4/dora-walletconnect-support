package dora.pay

import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal

/**
 * 支付生命周期处理。
 */
class ModalDelegateProxy(val payListener: DoraFund.PayListener? = null) : Web3Modal.ModalDelegate {

    override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
        onSessionApprovedNative(approvedSession)
    }

    override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {
        onSessionRejectedNative(rejectedSession)
    }

    override fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse) {
        onSessionRequestResponseNative(response)
    }

    override fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession) {
        onSessionUpdateNative(updatedSession)
    }

    override fun onSessionExtend(session: Modal.Model.Session) {
        onSessionExtendNative(session)
    }

    @Deprecated(
        "Use onSessionEvent(Modal.Model.Event) instead. Using both will result in duplicate events.",
        replaceWith = ReplaceWith("onEvent(event)")
    )
    override fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {
        onSessionEventNative(sessionEvent)
    }

    override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {
        onSessionDeleteNative(deletedSession)
    }

    override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {
        onConnectionStateChangeNative(state)
    }

    override fun onError(error: Modal.Model.Error) {
        onErrorNative(error)
    }

    override fun onProposalExpired(proposal: Modal.Model.ExpiredProposal) {
        onProposalExpiredNative(proposal)
    }

    override fun onRequestExpired(request: Modal.Model.ExpiredRequest) {
        onRequestExpiredNative(request)
    }

    private external fun onSessionApprovedNative(approvedSession: Modal.Model.ApprovedSession)
    private external fun onSessionRejectedNative(rejectedSession: Modal.Model.RejectedSession)
    private external fun onSessionRequestResponseNative(response: Modal.Model.SessionRequestResponse)
    private external fun onSessionUpdateNative(updatedSession: Modal.Model.UpdatedSession)
    private external fun onSessionExtendNative(session: Modal.Model.Session)
    private external fun onSessionEventNative(sessionEvent: Modal.Model.SessionEvent)
    private external fun onSessionDeleteNative(deletedSession: Modal.Model.DeletedSession)
    private external fun onConnectionStateChangeNative(state: Modal.Model.ConnectionState)
    private external fun onErrorNative(error: Modal.Model.Error)
    private external fun onProposalExpiredNative(proposal: Modal.Model.ExpiredProposal)
    private external fun onRequestExpiredNative(request: Modal.Model.ExpiredRequest)
}
