package kz.esign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import org.junit.jupiter.api.Test;

class KeyServiceTest {

    private final KeyService service = new KeyService();

    @Test
    void keyLengthIs2048Bits() {
        KeyPair pair = service.generateKeyPair();
        assertEquals(2048, ((RSAPublicKey) pair.getPublic()).getModulus().bitLength());
    }

    @Test
    void twoGenerationsGiveDifferentKeys() {
        KeyPair first = service.generateKeyPair();
        KeyPair second = service.generateKeyPair();
        assertNotEquals(first.getPublic(), second.getPublic());
    }
}
