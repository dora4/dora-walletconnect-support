package dora.trade

import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal

class ModalDelegateProxy(val payListener: DoraTrade.PayListener? = null) : Web3Modal.ModalDelegate {

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

    override fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {
        onSessionEventNative(sessionEvent)
    }

    override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {
        onSessionDeleteNative(deletedSession)
    }

    override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {
//        onConnectionStateChangeNative(state)
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

    external fun onSessionApprovedNative(approvedSession: Modal.Model.ApprovedSession)
    external fun onSessionRejectedNative(rejectedSession: Modal.Model.RejectedSession)
    external fun onSessionRequestResponseNative(response: Modal.Model.SessionRequestResponse)
    external fun onSessionUpdateNative(updatedSession: Modal.Model.UpdatedSession)
    external fun onSessionExtendNative(session: Modal.Model.Session)
    external fun onSessionEventNative(sessionEvent: Modal.Model.SessionEvent)
    external fun onSessionDeleteNative(deletedSession: Modal.Model.DeletedSession)
//    external fun onConnectionStateChangeNative(state: Modal.Model.ConnectionState)
    external fun onErrorNative(error: Modal.Model.Error)
    external fun onProposalExpiredNative(proposal: Modal.Model.ExpiredProposal)
    external fun onRequestExpiredNative(request: Modal.Model.ExpiredRequest)
}
