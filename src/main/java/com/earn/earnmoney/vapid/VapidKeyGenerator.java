package com.earn.earnmoney.vapid;

import java.security.*;
import java.util.Base64;

public class VapidKeyGenerator {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(256);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        String publicKey = Base64.getUrlEncoder().withoutPadding().encodeToString(keyPair.getPublic().getEncoded());
        String privateKey = Base64.getUrlEncoder().withoutPadding().encodeToString(keyPair.getPrivate().getEncoded());

        System.out.println("Public Key: " + publicKey);
        System.out.println("Private Key: " + privateKey);
    }
}