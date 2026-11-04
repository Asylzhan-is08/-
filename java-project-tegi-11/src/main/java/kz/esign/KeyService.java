package kz.esign;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/** RSA-2048 кілт жұбын жасайды. */
public class KeyService {

    public static final String ALGORITHM = "RSA";
    public static final int KEY_SIZE = 2048;

    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
            generator.initialize(KEY_SIZE, new SecureRandom());
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new EsignException("RSA алгоритмі қолжетімсіз", e);
        }
    }
}
