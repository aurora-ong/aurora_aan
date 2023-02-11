package ong.aurora.ann;

import ong.aurora.ann.identity.ANNNodeIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

public class AANConfig {

    private static final Logger log = LoggerFactory.getLogger(AANConfig.class);

    public String nodeId;

    public PrivateKey privateKey;

    public PublicKey publicKey;


    @Override
    public String toString() {
        return "AANConfig{" +
                "nodeId='" + nodeId + '\'' +
                ", privateKey=" + privateKey.toString() +
                ", publicKey=" + publicKey.toString() +
                '}';
    }

    private AANConfig(String nodeId, PublicKey nodeKeyPublic, PrivateKey nodeKeyPrivate) {

        this.nodeId = nodeId;
        this.publicKey = nodeKeyPublic;
        this.privateKey = nodeKeyPrivate;
        log.info("Configuración cargada {}", this);
    }

    public static AANConfig fromEnviroment() throws Exception {

        log.info("Cargando configuración desde env");

        Map<String, String> env = System.getenv();

        String nodeId = env.get("ANN_NODE_ID");

        String nodeKeyPublicString = env.get("ANN_NODE_KEY_PUBLIC");

        String nodeKeyPrivateString = env.get("ANN_NODE_KEY_PRIVATE");

        if (nodeId == null || nodeId.isEmpty()) {
            throw new Exception("Debe proporcionarse un identificador de nodo");
        }

        if (nodeKeyPublicString == null || nodeKeyPublicString.isEmpty()) {
            throw new Exception("Debe proporcionarse una llave pública para este nodo");
        }

        if (nodeKeyPrivateString == null || nodeKeyPrivateString.isEmpty()) {
            throw new Exception("Debe proporcionarse una llave privada para este nodo");
        }

        PublicKey nodeKeyPublic = ANNNodeIdentity.publicKeyFromString(nodeKeyPublicString);

        PrivateKey nodeKeyPrivate = ANNNodeIdentity.privateKeyFromString(nodeKeyPrivateString);

        return new AANConfig(nodeId, nodeKeyPublic, nodeKeyPrivate);
    }
}
