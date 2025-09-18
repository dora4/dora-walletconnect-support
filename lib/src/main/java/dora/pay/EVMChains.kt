package dora.pay

import com.walletconnect.web3.modal.presets.Web3ModalChainsPresets

/**
 * Provide an enumeration of commonly used EVM-compatible chains.
 * @since 2.1
 */
interface EVMChains {

    companion object {

        /**
         * Ethereum mainnet.
         * @since 2.1
         */
        val ETHEREUM = Web3ModalChainsPresets.ethChains["1"]!!

        /**
         * Optimism.
         * @since 2.1
         */
        val OPTIMISM = Web3ModalChainsPresets.ethChains["10"]!!

        /**
         * BNB Smart Chain.
         * @since 2.1
         */
        val BSC = Web3ModalChainsPresets.ethChains["56"]!!

        /**
         * Polygon.
         * @since 2.1
         */
        val POLYGON = Web3ModalChainsPresets.ethChains["137"]!!

        /**
         * Ethereum Proof-of-Work.
         * @since 2.1
         */
        val ETHEREUM_POW = Web3ModalChainsPresets.ethChains["10001"]!!

        /**
         * Arbitrum.
         * @since 2.1
         */
        val ARBITRUM = Web3ModalChainsPresets.ethChains["42161"]!!

        /**
         * Avalanche.
         * @since 2.1
         */
        val AVALANCHE = Web3ModalChainsPresets.ethChains["43114"]!!
    }
}