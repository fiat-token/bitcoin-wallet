package com.eternitywall.regtest.eternitywall;

import android.view.View;


import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.utils.MonetaryFormat;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.Protos;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletTransaction;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BitcoinEW {


    // Constants
    public static String EW_URL = "https://eternitywall-api.appspot.com/pn2a/v1";
    public static String EW_API_KEY = "ETxYGDXTV8O63Wv";
    public static String EW_SHARING = "https://vtoken.eternitywall.com/";
    public static long WALLET_MIN_TIMESTAMP = 1495000000 ;
    public static int MAX_TRANSACTION_AMOUNT = 100*10000; // vTKN 100
    public static final MonetaryFormat vTKN = new MonetaryFormat().minDecimals(2).optionalDecimals(2, 1).repeatOptionalDecimals(2,0).code(4,"vTKN").shift(4);

    // Network & DNS peers
    public static final NetworkParameters NETWORK_PARAMETERS = RegTestParams.get();
    public static final String[] DNSPEERS = {
            "relay1.eternitywall.com",
            "relay2.eternitywall.com",
            "relay3.eternitywall.com",
            "relay4.eternitywall.com",
            "relay5.eternitywall.com",
    };

    // Script OP_RETURN manipulation
    public static Script createOpReturnScript(List<Data> list) throws Exception {
        byte[] buffer = {};
        for (Data data : list){
            byte[] serialized = data.serialize();
            buffer = merge(buffer,serialized);
        }
        return ScriptBuilder.createOpReturnScript(buffer);
    }

    public static List<Data> parseOpReturnScript(byte[] outputSerialized) throws Exception {
        List<Data> list = new ArrayList<>();
        byte []opreturn = outputSerialized;
        while (opreturn!= null && opreturn.length>0){
            int len = opreturn[1];
            byte[] serialized = Arrays.copyOf(opreturn,len+2);
            list.add(Data.deserialize(serialized));
            opreturn = Arrays.copyOfRange(opreturn,len+2,opreturn.length);
        }
        return list;
    }

    // Utils
    final static
    public byte[] merge(final byte[] ...arrays ) {
        int size = 0;
        for ( byte[] a: arrays )
            size += a.length;

        byte[] res = new byte[size];

        int destPos = 0;
        for ( int i = 0; i < arrays.length; i++ ) {
            if ( i > 0 ) destPos += arrays[i-1].length;
            int length = arrays[i].length;
            System.arraycopy(arrays[i], 0, res, destPos, length);
        }

        return res;
    }


    public static DeterministicKey getAuthenticationDeterministicKey (Wallet wallet){
        return wallet.getActiveKeyChain().getKey(KeyChain.KeyPurpose.AUTHENTICATION);
    }

/*
    public static Transaction buildTransaction(Wallet wallet, NetworkParameters params, Coin amount, Address address, List<Data> datas){
        Transaction tx = new Transaction(params);

        // Add outputs
        try {
            Script script = createOpReturnScript(datas);
            if(address == null) {
                tx.addOutput(amount, script);
            } else {
                tx.addOutput(Coin.ZERO,script);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //utxos is an array of inputs from my wallet
        ECKey key = wallet.currentReceiveKey();
        for(UTXO utxo :    )
        {
            TransactionOutPoint outPoint = new TransactionOutPoint(params, utxo.getIndex(), utxo.getHash());
            //YOU HAVE TO CHANGE THIS
            tx.addSignedInput(outPoint, utxo.getScript(), key, Transaction.SigHash.ALL, true);
        }

        tx.getConfidence().setSource(TransactionConfidence.Source.SELF);
        tx.setPurpose(Transaction.Purpose.USER_PAYMENT);

        //System.out.println(tx.getHashAsString());
        //b_peerGroup.GetPeerGroup().broadcastTransaction(tx);
        return tx;
    }*/


    public static DeterministicKey getDeterministicKey (Wallet wallet){
        return getDeterministicKey(wallet.getKeyChainSeed().getSeedBytes());
    }
    public static DeterministicKey getDeterministicKey (byte[] seed){
        DeterministicKey deterministicKey = HDKeyDerivation.createMasterPrivateKey(seed);
        DeterministicKey ewMaster = HDKeyDerivation.deriveChildKey(deterministicKey, new ChildNumber(0, true));   // /m/0'/
        DeterministicKey bitcoinMaster = HDKeyDerivation.deriveChildKey(ewMaster, new ChildNumber(0, true));  // /m/4544288'/0'
        return bitcoinMaster;
    }

    public static byte[] getEntropyFromPassphrase(String mnemonic) {
        try {
            final MnemonicCode mnemonicCode = new MnemonicCode();
            if(mnemonicCode!=null)
                return mnemonicCode.toEntropy(Arrays.asList(mnemonic.split("\\s+")));
        } catch (MnemonicException.MnemonicLengthException e) {
            e.printStackTrace();
        } catch (MnemonicException.MnemonicWordException e) {
            e.printStackTrace();
        } catch (MnemonicException.MnemonicChecksumException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
