package kz.esign;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SignServiceTest {

    private static KeyPair pair;
    private static KeyPair otherPair;
    private final SignService service = new SignService();

    @TempDir
    Path dir;

    @BeforeAll
    static void generate() {
        KeyService keyService = new KeyService();
        pair = keyService.generateKeyPair();
        otherPair = keyService.generateKeyPair();
    }

    private Path document(String text) throws IOException {
        Path file = dir.resolve("doc.txt");
        Files.writeString(file, text);
        return file;
    }

    @Test
    void validSignatureIsAccepted() throws IOException {
        Path file = document("Құпия құжат");
        byte[] sig = service.sign(file, pair.getPrivate());
        assertTrue(service.verify(file, sig, pair.getPublic()));
    }

    @Test
    void modifiedFileIsRejected() throws IOException {
        Path file = document("Құпия құжат");
        byte[] sig = service.sign(file, pair.getPrivate());
        Files.writeString(file, "Құпия құжат!");
        assertFalse(service.verify(file, sig, pair.getPublic()));
    }

    @Test
    void otherPublicKeyIsRejected() throws IOException {
        Path file = document("Құпия құжат");
        byte[] sig = service.sign(file, pair.getPrivate());
        assertFalse(service.verify(file, sig, otherPair.getPublic()));
    }

    @Test
    void truncatedSignatureIsRejected() throws IOException {
        Path file = document("Құпия құжат");
        byte[] sig = service.sign(file, pair.getPrivate());
        assertFalse(service.verify(file, Arrays.copyOf(sig, 10), pair.getPublic()));
    }

    @Test
    void flippedByteInSignatureIsRejected() throws IOException {
        Path file = document("Құпия құжат");
        byte[] sig = service.sign(file, pair.getPrivate());
        sig[0] ^= 1;
        assertFalse(service.verify(file, sig, pair.getPublic()));
    }

    @Test
    void emptyFileCanBeSignedAndVerified() throws IOException {
        Path file = document("");
        byte[] sig = service.sign(file, pair.getPrivate());
        assertTrue(service.verify(file, sig, pair.getPublic()));
    }

    @Test
    void largeFileIsProcessed() throws IOException {
        Path file = dir.resolve("big.bin");
        Files.write(file, new byte[5 * 1024 * 1024]);
        byte[] sig = service.sign(file, pair.getPrivate());
        assertTrue(service.verify(file, sig, pair.getPublic()));
    }

    @Test
    void missingFileThrowsEsignException() {
        Path missing = dir.resolve("жоқ.txt");
        assertThrows(EsignException.class, () -> service.sign(missing, pair.getPrivate()));
    }
}
