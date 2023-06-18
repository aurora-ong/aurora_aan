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

    public String nodeName;

    public String nodeHostname;

    public PrivateKey privateKey;

    public PublicKey publicKey;

    public String blockchainFilePath;

    public Integer commandPort;

    public Integer nodePort;

    public Integer projectorPort;


    @Override
    public String toString() {
        return  "nodeId=" + nodeId + "\n" +
                "nodeName=" + nodeName + "\n" +
                "nodeHostname=" + nodeHostname + "\n" +
                "privateKey=" + privateKey.getEncoded() + "\n" +
                "publicKey=" + publicKey.getEncoded() + "\n" +
                "blockchainFilePath='" + blockchainFilePath + "\n" +
                "commandPort=" + commandPort + "\n" +
                "networkNodePort=" + nodePort + "\n" +
                "projectorPort=" + projectorPort;
    }

    private AANConfig(String nodeId, String nodeName, String nodeHostname, PublicKey nodeKeyPublic, PrivateKey nodeKeyPrivate, String blockchainFilePath, Integer commandPort, Integer nodePort, Integer projectorPort) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.nodeHostname = nodeHostname;
        this.publicKey = nodeKeyPublic;
        this.privateKey = nodeKeyPrivate;
        this.blockchainFilePath = blockchainFilePath;
        this.commandPort = commandPort;
        this.nodePort = nodePort;
        this.projectorPort = projectorPort;
        log.info("Configuración cargada:\n\n{}\n", this);
    }

    public static AANConfig fromEnviroment() {

        log.info("Cargando configuración desde env");

        Map<String, String> env = System.getenv();

        log.debug("ENV CONFIG \n{}\n", env);

        String nodeId = env.get("AAN_NODE_ID");

        String nodeName = env.get("AAN_NODE_NAME");

        String nodeHostname = env.get("AAN_NODE_HOSTNAME");

        String nodeKeyPublicString = env.get("AAN_NODE_KEY_PUBLIC");

        String nodeKeyPrivateString = env.get("AAN_NODE_KEY_PRIVATE");

        String blockchainFilePath = env.get("AAN_BLOCKCHAIN_FILE_PATH");

        String commandPortString = env.get("AAN_COMMAND_PORT");
        int commandPort = 6000;

        String networkPortString = env.get("AAN_NODE_PORT");
        int nodePort = 4000;

        String projectorPortString = env.get("AAN_PROJECTOR_PORT");
        int projectorPort = 8000;


        if (nodeId == null || nodeId.isEmpty()) {
            throw new RuntimeException("Debe proporcionarse un identificador para este nodo");
        }

        if (nodeName == null || nodeName.isEmpty()) {
            throw new RuntimeException("Debe proporcionarse un nombre para este nodo");
        }

        if (nodeHostname == null || nodeHostname.isEmpty()) {
            throw new RuntimeException("Debe proporcionarse un hostname para este nodo");
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
            nodePort = Integer.parseInt(networkPortString);
        }

        if (projectorPortString != null && !projectorPortString.isEmpty()) {
            projectorPort = Integer.parseInt(projectorPortString);
        }

        return new AANConfig(nodeId, nodeName, nodeHostname, nodeKeyPublic, nodeKeyPrivate, blockchainFilePath, commandPort, nodePort, projectorPort);
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
