package com.eternitywall.regtest.eternitywall;

import android.view.View;


import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.utils.MonetaryFormat;

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


}
