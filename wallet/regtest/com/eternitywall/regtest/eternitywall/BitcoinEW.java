package com.eternitywall.regtest.eternitywall;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.VtknTestNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.utils.MonetaryFormat;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.Wallet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BitcoinEW {


    // Constants
    public static String EW_URL = "https://eternitywall-api-test.appspot.com/pn2a/v1";
    public static String EW_API_KEY = "ETxYGDXTV8O63Wv";
    public static String EW_SHARING = "https://vtoken.eternitywall.com/";
    public static long WALLET_MIN_TIMESTAMP = 1495000000 ;
    public static int MAX_TRANSACTION_AMOUNT = 100*10000; // vTKN 100
    public static final MonetaryFormat vTKN = new MonetaryFormat().minDecimals(2).optionalDecimals(2, 1).repeatOptionalDecimals(2,0).code(4,"vTKN").shift(4);

    // Network & DNS peers
    public static final NetworkParameters NETWORK_PARAMETERS = VtknTestNetParams.get();
    public static final String[] DNSPEERS = {
            "test.signer2.eternitywall.com"
    };
}
