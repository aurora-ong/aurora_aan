package ong.aurora.ann;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class ANNNodeIdentity {

    private static final Logger log = LoggerFactory.getLogger(ANNNodeIdentity.class);

    PrivateKey privateKey;
    PublicKey publicKey;

    ANNNodeIdentity(String nodeId) throws Exception {
        log.info("Obteniendo identidad para {}", nodeId);

        Security.addProvider(new BouncyCastleProvider());

        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        String keyPath = s.concat("/ann-node-identity/");

        String publicKeyPath = keyPath.concat(nodeId).concat("_public.pem");
        String privateKeyPath = keyPath.concat(nodeId).concat("_private.pem");

        log.info("keyPath: \n publicKeyPath: {}\n privateKeyPath: {}", publicKeyPath, privateKeyPath);
        KeyFactory factory = KeyFactory.getInstance("RSA", "BC");

        PrivateKey priv = generatePrivateKey(factory, privateKeyPath);
        this.privateKey = priv;

        PublicKey pub = generatePublicKey(factory, publicKeyPath);
        this.publicKey = pub;

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


}
