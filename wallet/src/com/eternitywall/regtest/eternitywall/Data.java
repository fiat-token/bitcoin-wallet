package com.eternitywall.regtest.eternitywall;

import android.view.View;

import org.bitcoinj.core.Transaction;

import java.io.UnsupportedEncodingException;

/**
 * Created by luca on 22/09/2017.
 */

public class Data {

    private byte type;
    private byte len;
    private byte []payload;

    public static byte TYPE_NOTE=0x28;
    public static byte TYPE_KEY=0x29;

    public static byte MAX_LENGTH=62;

    public byte getType(){
        return type;
    }
    public void setType(byte type){
        this.type = type;
    }

    public byte getLen(){
        return len;
    }
    public void setLen(byte len){
        this.len = len;
    }

    public byte[] getPayload(){
        return payload;
    }
    public void setPayload(byte[] payload){
        this.payload = payload;
    }

    public Data(byte type, byte len, byte [] payload) throws Exception {
        if(type != TYPE_NOTE && type != TYPE_KEY)
            throw new Exception("Invalid type");
        if(len>MAX_LENGTH)
            throw new Exception("Invalid len");
        if(payload.length>MAX_LENGTH)
            throw new Exception("Payload too long");
        this.type = type;
        this.len = len;
        this.payload = payload;
    }
    public Data(byte type, byte [] payload) throws Exception {
        len = (byte) payload.length;
        if(type != TYPE_NOTE && type != TYPE_KEY)
            throw new Exception("Invalid type");
        if(len>MAX_LENGTH)
            throw new Exception("Invalid len");
        if(payload.length>MAX_LENGTH)
            throw new Exception("Payload too long");
        this.type = type;
        this.len = len;
        this.payload = payload;
    }

    public byte[] serialize(){
        byte[] buffer = new byte[2+len];
        buffer[0]=this.type;
        buffer[1]=this.len;
        for (int i = 0 ; i<len; i++){
            buffer[2+i]=payload[i];
        }
        return buffer;
    }

    public static Data deserialize(byte []buffer) throws Exception {
        byte type=buffer[0];
        byte len=buffer[1];
        byte[] payload = new byte[len];
        for (int i = 0 ; i<len; i++){
            payload[i]=buffer[2+i];
        }
        return new Data(type,len,payload);
    }

    public String toString() {
        try {
            return new String(payload, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
