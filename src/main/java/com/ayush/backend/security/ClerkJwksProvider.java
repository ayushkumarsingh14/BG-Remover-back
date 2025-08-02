package com.ayush.backend.security;

import java.math.BigInteger;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ClerkJwksProvider {

    @Value("${clerk.jwks-url}")
    private String jwksUrl;

    private final Map<String, PublicKey> keyCache = new HashMap<>();
    private long lastFetchTime = 0;
    private static final long CACHE_TTL = 3600000;

    public PublicKey getPublicKey(String kId) throws Exception{
        if (keyCache.containsKey(kId) && System.currentTimeMillis() - lastFetchTime < CACHE_TTL) {
            return keyCache.get(kId);     
        }
        refreshKey();
        return keyCache.get(kId);
    }

    private void refreshKey() throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jwks = mapper.readTree(new URL(jwksUrl));

        JsonNode keys = jwks.get("keys");

        for (JsonNode keyNode : keys) {
            String kId = keyNode.get("kid").asText();
            String kTy = keyNode.get("kty").asText();
            String alg = keyNode.get("alg").asText();

            if ("RSA".equals(kTy) && "RS256".equals(alg)) {
                String n = keyNode.get("n").asText();
                String e = keyNode.get("e").asText();

                PublicKey publicKey = creatPublicKey(n, e);
                keyCache.put(kId, publicKey);
            }
        }
        lastFetchTime = System.currentTimeMillis();
    }


    private PublicKey creatPublicKey(String modulus, String exponent) throws Exception {

        byte [] modulusBytes = Base64.getUrlDecoder().decode(modulus);
        byte [] exponentBytes = Base64.getUrlDecoder().decode(exponent);
        
        BigInteger modulusBigInt = new BigInteger(1, modulusBytes);
        BigInteger exponenBigInt = new BigInteger(1, exponentBytes);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulusBigInt, exponenBigInt);
        KeyFactory factory = KeyFactory.getInstance("RSA");

        return factory.generatePublic(spec);

    }



}
