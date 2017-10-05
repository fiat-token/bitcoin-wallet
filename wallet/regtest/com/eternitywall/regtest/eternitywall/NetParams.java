package com.eternitywall.regtest.eternitywall;

import org.bitcoinj.core.Block;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet2Params;

import java.math.BigInteger;

import static com.eternitywall.regtest.eternitywall.BitcoinEW.BITCOIN_PORT;
import static com.google.common.base.Preconditions.checkState;

/**
 * Network parameters for the regression test mode of bitcoind in which all blocks are trivially solvable.
 */
public class NetParams extends TestNet2Params {
    private static final BigInteger MAX_TARGET = new BigInteger("7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);

    public NetParams() {
        super();
        // Difficulty adjustments are disabled for regtest.
        // By setting the block interval for difficulty adjustments to Integer.MAX_VALUE we make sure difficulty never changes.
        interval = Integer.MAX_VALUE;
        maxTarget = MAX_TARGET;
        subsidyDecreaseBlockCount = 150;
        port = BITCOIN_PORT;
        id = ID_REGTEST;

        packetMagic = 0xbfda034eL;
        addressHeader = 111;
        p2shHeader = 196;
        dumpedPrivateKeyHeader = 239;
        bip32HeaderPub = 0x043587CF;
        bip32HeaderPriv = 0x04358394;

        majorityEnforceBlockUpgrade = MainNetParams.MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = MainNetParams.MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = MainNetParams.MAINNET_MAJORITY_WINDOW;
    }

    @Override
    public boolean allowEmptyPeerChain() {
        return true;
    }

    private static Block genesis;

    @Override
    public Block getGenesisBlock() {
        synchronized (org.bitcoinj.params.RegTestParams.class) {
            if (genesis == null) {
                genesis = super.getGenesisBlock();
                genesis.setNonce(50);
                genesis.setDifficultyTarget(0x207fFFFFL);
                genesis.setTime(1504224000L);
                //checkState(genesis.getHashAsString().toLowerCase().equals("0f9188f13cb7b2c71f2a335e3a4fc328bf5beb436012afca590b1a11466e2206"));
            }
            return genesis;
        }
    }

    private static NetParams instance;

    public static synchronized NetParams get() {
        if (instance == null) {
            instance = new NetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_REGTEST;
    }
}
