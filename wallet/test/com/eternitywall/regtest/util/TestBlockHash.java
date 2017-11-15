package com.eternitywall.regtest.util;

import org.bitcoinj.core.Block;
import org.bitcoinj.params.VtknNetParams;
import org.junit.Test;

/**
 * Created by Riccardo Casatta @RCasatta on 15/11/17.
 */

public class TestBlockHash {

    @Test
    public void testGenesisBlockHash() {
        final Block genesisBlock = new VtknNetParams().getGenesisBlock();
        System.out.println("genesis hash: " + genesisBlock.getHashAsString());
        System.out.println("merkle root: " + genesisBlock.getMerkleRoot().toString());

    }
}
