package com.eternitywall.regtest.util;

import org.bitcoinj.core.Block;
import org.bitcoinj.params.VtknNetParams;
import org.bitcoinj.params.VtknTestNetParams;
import org.junit.Test;

/**
 * Created by Riccardo Casatta @RCasatta on 15/11/17.
 */

public class TestBlockHash {

    @Test
    public void testGenesisBlockHash() {

        System.out.println("VtknNetParams");
        final Block genesisBlock = new VtknNetParams().getGenesisBlock();
        System.out.println("genesis hash: " + genesisBlock.getHashAsString());
        System.out.println("merkle root: " + genesisBlock.getMerkleRoot().toString());
        System.out.println("time: " + genesisBlock.getTime());

        System.out.println();
        System.out.println("VtknTestNetParams");
        final Block genesisBlockTestnet = new VtknTestNetParams().getGenesisBlock();
        System.out.println("genesis hash: " + genesisBlockTestnet.getHashAsString());
        System.out.println("merkle root: " + genesisBlockTestnet.getMerkleRoot().toString());
        System.out.println("time: " + genesisBlockTestnet.getTime());


    }
}
