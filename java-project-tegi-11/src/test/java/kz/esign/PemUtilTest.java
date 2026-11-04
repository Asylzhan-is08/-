package kz.esign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PemUtilTest {

    private static KeyPair pair;
    private final PemUtil pem = new PemUtil();

    @TempDir
    Path dir;

    @BeforeAll
    static void generate() {
        pair = new KeyService().generateKeyPair();
    }

    @Test
    void privatePemHasCorrectHeader() throws IOException {
        Path file = dir.resolve("private.pem");
        pem.writePrivateKey(pair.getPrivate(), file);
        assertTrue(Files.readString(file).startsWith("-----BEGIN PRIVATE KEY-----"));
    }

    @Test
    void publicPemHasCorrectHeader() throws IOException {
        Path file = dir.resolve("public.pem");
        pem.writePublicKey(pair.getPublic(), file);
        assertTrue(Files.readString(file).startsWith("-----BEGIN PUBLIC KEY-----"));
    }

    @Test
    void privateKeyRoundTrip() {
        Path file = dir.resolve("private.pem");
        pem.writePrivateKey(pair.getPrivate(), file);
        assertEquals(pair.getPrivate(), pem.readPrivateKey(file));
    }

    @Test
    void publicKeyRoundTrip() {
        Path file = dir.resolve("public.pem");
        pem.writePublicKey(pair.getPublic(), file);
        assertEquals(pair.getPublic(), pem.readPublicKey(file));
    }

    @Test
    void invalidPemThrowsEsignException() throws IOException {
        Path file = dir.resolve("broken.pem");
        Files.writeString(file, "бұл PEM емес");
        assertThrows(EsignException.class, () -> pem.readPublicKey(file));
    }
}
