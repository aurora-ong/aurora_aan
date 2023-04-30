package ong.aurora.aan.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

public class AANConfig {

    private static final Logger log = LoggerFactory.getLogger(AANConfig.class);

    public String nodeId;

    public PrivateKey privateKey;

    public PublicKey publicKey;

    public String blockchainFilePath;

    public Integer commandPort;

    public Integer networkPort;

    public Integer projectorPort;


    @Override
    public String toString() {
        return "AANConfig{" +
                "nodeId='" + nodeId + '\'' +
                ", privateKey=" + privateKey +
                ", publicKey=" + publicKey +
                ", blockchainFilePath='" + blockchainFilePath + '\'' +
                ", commandPort=" + commandPort +
                ", networkNodePort=" + networkPort +
                ", projectorPort=" + projectorPort +
                '}';
    }

    private AANConfig(String nodeId, PublicKey nodeKeyPublic, PrivateKey nodeKeyPrivate, String blockchainFilePath, Integer commandPort, Integer networkPort, Integer projectorPort) {
        this.nodeId = nodeId;
        this.publicKey = nodeKeyPublic;
        this.privateKey = nodeKeyPrivate;
        this.blockchainFilePath = blockchainFilePath;
        this.commandPort = commandPort;
        this.networkPort = networkPort;
        this.projectorPort = projectorPort;
        log.info("Configuración cargada {}", this);
    }

    public static AANConfig fromEnviroment() {

        log.info("Cargando configuración desde env");

        Map<String, String> env = System.getenv();

        String nodeId = env.get("AAN_NODE_ID");

        String nodeKeyPublicString = env.get("AAN_NODE_KEY_PUBLIC");

        String nodeKeyPrivateString = env.get("AAN_NODE_KEY_PRIVATE");

        String blockchainFilePath = env.get("AAN_BLOCKCHAIN_FILE_PATH");

        String commandPortString = env.get("AAN_COMMAND_PORT");
        int commandPort = 6000;

        String networkPortString = env.get("AAN_NETWORK_PORT");
        int networkPort = 4000;

        String projectorPortString = env.get("AAN_PROJECTOR_PORT");
        int projectorPort = 8000;


        if (nodeId == null || nodeId.isEmpty()) {
            throw new RuntimeException("Debe proporcionarse un identificador de nodo");
        }

        if (nodeKeyPublicString == null || nodeKeyPublicString.isEmpty()) {
            throw new RuntimeException("Debe proporcionarse una llave pública para este nodo");
        }

        if (nodeKeyPrivateString == null || nodeKeyPrivateString.isEmpty()) {
            throw new RuntimeException("Debe proporcionarse una llave privada para este nodo");
        }

        PublicKey nodeKeyPublic = publicKeyFromString(nodeKeyPublicString);

        PrivateKey nodeKeyPrivate = privateKeyFromString(nodeKeyPrivateString);

        if (commandPortString != null && !commandPortString.isEmpty()) {
            commandPort = Integer.parseInt(commandPortString);

        }

        if (networkPortString != null && !networkPortString.isEmpty()) {
            networkPort = Integer.parseInt(networkPortString);
        }

        if (projectorPortString != null && !projectorPortString.isEmpty()) {
            projectorPort = Integer.parseInt(projectorPortString);
        }

        return new AANConfig(nodeId, nodeKeyPublic, nodeKeyPrivate, blockchainFilePath, commandPort, networkPort, projectorPort);
    }

    private static PublicKey publicKeyFromString(String publicKey) {
        try {
            byte[] byteKey = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(X509publicKey);

        } catch (Exception e) {
            throw new RuntimeException("Clave pública inválida {}", e);
        }
    }

    private static PrivateKey privateKeyFromString(String privateKey) {
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
