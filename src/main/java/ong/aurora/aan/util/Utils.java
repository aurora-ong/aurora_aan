package ong.aurora.aan.util;

import rx.Subscription;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;

public class Utils {

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

    public static boolean requestConfirmation(String message) {
        do {
            Scanner s = new Scanner(System.in);
            System.out.printf("%s Y/n%n", message);

            String r = s.nextLine();
            if (r.equals("Y")) {
                return true;
            } else if (r.equals("n")) {
                return false;
            } else {
                System.out.println("Opción inválida. Intente nuevamente");

            }
        } while (true);

    }
}
