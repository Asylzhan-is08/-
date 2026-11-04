package kz.esign;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

/** Файлға қол қояды және қолтаңбаны тексереді (SHA256withRSA). */
public class SignService {

    public static final String ALGORITHM = "SHA256withRSA";
    private static final int BUFFER_SIZE = 8192;

    public byte[] sign(Path file, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initSign(privateKey);
            feed(signature, file);
            return signature.sign();
        } catch (GeneralSecurityException | IOException e) {
            throw new EsignException("Қол қою мүмкін болмады: " + file, e);
        }
    }

    public boolean verify(Path file, byte[] signatureBytes, PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initVerify(publicKey);
            feed(signature, file);
            return signature.verify(signatureBytes);
        } catch (SignatureException e) {
            // бүлінген немесе ұзындығы дұрыс емес қолтаңба
            return false;
        } catch (GeneralSecurityException | IOException e) {
            throw new EsignException("Тексеру мүмкін болмады: " + file, e);
        }
    }

    /** Файлды буфермен оқып, Signature-ға береді (үлкен файлдар жадқа толық жүктелмейді). */
    private void feed(Signature signature, Path file) throws IOException, SignatureException {
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                signature.update(buffer, 0, read);
            }
        }
    }
}
