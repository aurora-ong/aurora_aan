package ong.aurora.ann.identity;

import ong.aurora.ann.PemFile;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ANNNodeIdentity {

    private static final Logger log = LoggerFactory.getLogger(ANNNodeIdentity.class);

    private ANNNodeIdentity(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public PrivateKey privateKey;
    public PublicKey publicKey;

    public static ANNNodeIdentity fromFile(String privateKeyPath, String publicKeyPath) throws Exception {
        log.info("Obteniendo identidad");

        Security.addProvider(new BouncyCastleProvider());
        log.info("keyPath: \n publicKeyPath: {}\n privateKeyPath: {}", publicKeyPath, privateKeyPath);
        KeyFactory factory = KeyFactory.getInstance("RSA", "BC");

        return new ANNNodeIdentity(generatePrivateKey(factory, privateKeyPath), generatePublicKey(factory, publicKeyPath));
    }

    private static PrivateKey generatePrivateKey(KeyFactory factory, String filename) throws InvalidKeySpecException, FileNotFoundException, IOException {
        PemFile pemFile = new PemFile(filename);
        byte[] content = pemFile.getPemObject().getContent();
        PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
        return factory.generatePrivate(privKeySpec);
    }

    private static PublicKey generatePublicKey(KeyFactory factory, String filename) throws InvalidKeySpecException, FileNotFoundException, IOException {
        PemFile pemFile = new PemFile(filename);
        byte[] content = pemFile.getPemObject().getContent();
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
        return factory.generatePublic(pubKeySpec);
    }

    public boolean compareWith(String base64) {
        String encodedPK = Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
        log.info("Comparando \n{} \n{}", base64, encodedPK);

        return encodedPK.equals(base64);
    }

    public static PublicKey publicKeyFromString(String publicKey) {
        try {
            byte[] byteKey = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(X509publicKey);

        } catch (Exception e) {
            throw new RuntimeException("Clave pública inválida {}", e);
        }
    }

    public static PrivateKey privateKeyFromString(String privateKey) {
        try {
            byte[] byteKey = Base64.getDecoder().decode(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpec);

        } catch (Exception e) {
            throw new RuntimeException("Clave privada inválida {}", e);
        }

    }


}
