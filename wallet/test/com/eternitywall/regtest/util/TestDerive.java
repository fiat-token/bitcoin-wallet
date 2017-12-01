package com.eternitywall.regtest.util;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.VtknNetParams;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by Riccardo Casatta @RCasatta on 15/11/17.
 */

public class TestDerive {

    @Test
    public void testSer() {

        final String s = "000102030405060708090a0b0c0d0e0f";
        byte[] mySeed= Hex.decode(s);

        final DeterministicKey deterministicKey = HDKeyDerivation.createMasterPrivateKey(mySeed);  // /m/
        System.out.println("deterministicKey=" + deterministicKey);

        final String pub58 = deterministicKey.serializePubB58(VtknNetParams.get());
        System.out.println("pub58=" + pub58);


    }

    @Test
    public void testDeser() {

        //final String s = "xpub6A7TcUpDS8XucyVXXdsyrzv54TJqYNorSm8bGrxS1bcjXM1j3gNz35rTzArPHJKxwwz1YiYPDBekyJb2Euot7d2jq3TCL2QrTsyJCBpeSQa";
        final String s = "xpub69RfAxZAZEK1xrs41S5qPEHgqnBs6ron38xVne9x6Xmkm9nG5arRiiA8eapRnV33VEBuKujvXqHaHToA9Xbt4o6cFkFJsRNozXRJ8mT7GbX";
        final DeterministicKey deterministicKey = DeterministicKey.deserializeB58(s, VtknNetParams.get());
        System.out.println("address0=" + deterministicKey.toAddress(VtknNetParams.get()).toBase58());

        //VTyAq1bHNnawWF58E7qWJBP2vzvMYBPFbo

        final DeterministicKey deterministicKey1 = HDKeyDerivation.deriveChildKey(deterministicKey, new ChildNumber(0, false));
        System.out.println("address1=" + deterministicKey1.toAddress(VtknNetParams.get()).toBase58());


        final String s2 = "xpub69RfAxZAZEK1xrs41S5qPEHgqnBs6ron38xVne9x6Xmkm9nG5arRiiA8eapRnV33VEBuKujvXqHaHToA9Xbt4o6cFkFJsRNozXRJ8mT7GbX";
        final DeterministicKey deterministicKey2 = DeterministicKey.deserializeB58(s2, VtknNetParams.get());
        System.out.println("address2=" + deterministicKey2.toAddress(VtknNetParams.get()).toBase58());
        final DeterministicKey deterministicKey20 = HDKeyDerivation.deriveChildKey(deterministicKey2, new ChildNumber(0, false));
        System.out.println("address2/0=" + deterministicKey20.toAddress(VtknNetParams.get()).toBase58());
        final DeterministicKey deterministicKey200 = HDKeyDerivation.deriveChildKey(deterministicKey20, new ChildNumber(0, false));
        System.out.println("address2/0/0=" + deterministicKey200.toAddress(VtknNetParams.get()).toBase58());

        final DeterministicKey deterministicKey21 = HDKeyDerivation.deriveChildKey(deterministicKey2, new ChildNumber(1, false));
        System.out.println("address2/1=" + deterministicKey21.toAddress(VtknNetParams.get()).toBase58());
        final DeterministicKey deterministicKey210 = HDKeyDerivation.deriveChildKey(deterministicKey21, new ChildNumber(0, false));
        System.out.println("address2/1/0=" + deterministicKey210.toAddress(VtknNetParams.get()).toBase58());


    }
}
