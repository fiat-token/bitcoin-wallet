package com.eternitywall.regtest.eternitywall;

import android.view.View;


import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bitcoinj.script.ScriptBuilder.*;

public class BitcoinEW {

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
