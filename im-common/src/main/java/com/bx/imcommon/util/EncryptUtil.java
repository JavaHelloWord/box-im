package com.bx.imcommon.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class EncryptUtil {

    /***
     * key和iv值可以随机生成
     */
    private static String KEY = "WEN99FIVNGF23445Fjksdnoi";

    private static String IV = "AMLF2NInfh234mpw";

    public static void main(String[] args) {
        String aaa = encrypt("加密信息是这个三农ifas豆角那个人");
        System.out.println(aaa);
        System.out.println(desEncrypt("m9VwH8OgVQocyEvhB6IWBv/XxkYFzrtxgpOe411ozBA="));
    }
    /***
     * 加密
     * @param  data 要加密的数据
     * @return encrypt
     */
    public static String encrypt(String data){
        return encrypt(data, KEY, IV);
    }

    /***
     * param data 需要解密的数据
     * 调用desEncrypt（）方法
     */
    public static String desEncrypt(String data){
        return desEncrypt(data, KEY, IV);
    }

    /**
     * 加密方法
     * @param data  要加密的数据
     * @param key 加密key
     * @param iv 加密iv
     * @return 加密的结果
     */
    private static String encrypt(String data, String key, String iv){
        try {
            //"算法/模式/补码方式"NoPadding PkcsPadding
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();

            byte[] dataBytes = data.getBytes();
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }

            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);
            return Base64.encode(encrypted);


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解密方法
     * @param data 要解密的数据
     * @param key  解密key
     * @param iv 解密iv
     * @return 解密的结果
     */
    private static String desEncrypt(String data, String key, String iv){
        try {
            byte[] encrypted1 = Base64.decode(data);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] original = cipher.doFinal(encrypted1);
            String s = new String(original);
            return s.trim();
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }
    public static Object encrObj(Object data){
        if(data instanceof JSONObject){
            JSONObject obj = (JSONObject) data;
            String string = obj.getString("content");
            if(StrUtil.isNotBlank(string) && (!"true".equals(string)) && (!"false".equals(string))){
                obj.put("content", encrypt(string));
                return obj;
            }
        }
        return data;

    }
}
