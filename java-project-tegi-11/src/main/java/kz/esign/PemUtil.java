package kz.esign;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/** Кілттерді PEM форматында файлға жазады және файлдан оқиды. */
public class PemUtil {

    private static final String PRIVATE_TYPE = "PRIVATE KEY"; // PKCS#8
    private static final String PUBLIC_TYPE = "PUBLIC KEY";   // X.509 SubjectPublicKeyInfo

    public void writePrivateKey(PrivateKey key, Path path) {
        writePem(PRIVATE_TYPE, key.getEncoded(), path);
        try {
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException | IOException ignored) {
            // Windows сияқты жүйелерде POSIX рұқсаттары жоқ, елемейміз
        }
    }

    public void writePublicKey(PublicKey key, Path path) {
        writePem(PUBLIC_TYPE, key.getEncoded(), path);
    }

    public PrivateKey readPrivateKey(Path path) {
        byte[] der = readPem(PRIVATE_TYPE, path);
        try {
            return KeyFactory.getInstance(KeyService.ALGORITHM)
                    .generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (GeneralSecurityException e) {
            throw new EsignException("Жеке кілтті оқу мүмкін болмады: " + path, e);
        }
    }

    public PublicKey readPublicKey(Path path) {
        byte[] der = readPem(PUBLIC_TYPE, path);
        try {
            return KeyFactory.getInstance(KeyService.ALGORITHM)
                    .generatePublic(new X509EncodedKeySpec(der));
        } catch (GeneralSecurityException e) {
            throw new EsignException("Ашық кілтті оқу мүмкін болмады: " + path, e);
        }
    }

    private void writePem(String type, byte[] der, Path path) {
        String base64 = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII))
                .encodeToString(der);
        String pem = "-----BEGIN " + type + "-----\n" + base64 + "\n-----END " + type + "-----\n";
        try {
            Files.writeString(path, pem, StandardCharsets.US_ASCII);
        } catch (IOException e) {
            throw new EsignException("Файлға жазу мүмкін болмады: " + path, e);
        }
    }

    private byte[] readPem(String type, Path path) {
        String pem;
        try {
            pem = Files.readString(path, StandardCharsets.US_ASCII);
        } catch (IOException e) {
            throw new EsignException("Файлды оқу мүмкін болмады: " + path, e);
        }
        String begin = "-----BEGIN " + type + "-----";
        String end = "-----END " + type + "-----";
        int from = pem.indexOf(begin);
        int to = pem.indexOf(end);
        if (from < 0 || to < from) {
            throw new EsignException("PEM форматы дұрыс емес, күтілген тип: " + type);
        }
        String body = pem.substring(from + begin.length(), to).replaceAll("\\s", "");
        try {
            return Base64.getDecoder().decode(body);
        } catch (IllegalArgumentException e) {
            throw new EsignException("PEM ішіндегі Base64 бүлінген", e);
        }
    }
}
