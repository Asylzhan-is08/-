package kz.esign;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.LinkedHashMap;
import java.util.Map;

/** Консольдік интерфейс: keygen, sign, verify. */
public class Main {

    private final KeyService keyService = new KeyService();
    private final PemUtil pemUtil = new PemUtil();
    private final SignService signService = new SignService();

    public static void main(String[] args) {
        System.exit(new Main().run(args));
    }

    /** Шығу коды: 0 - сәтті, 1 - қате, 2 - қолтаңба INVALID. */
    int run(String[] args) {
        if (args.length == 0) {
            printUsage();
            return 1;
        }
        try {
            Map<String, String> options = parseOptions(args);
            switch (args[0]) {
                case "keygen":
                    keygen(options);
                    return 0;
                case "sign":
                    sign(options);
                    return 0;
                case "verify":
                    return verify(options) ? 0 : 2;
                default:
                    printUsage();
                    return 1;
            }
        } catch (EsignException e) {
            System.err.println("Қате: " + e.getMessage());
            return 1;
        }
    }

    private void keygen(Map<String, String> options) {
        Path outDir = Paths.get(options.getOrDefault("out", "keys"));
        try {
            Files.createDirectories(outDir);
        } catch (IOException e) {
            throw new EsignException("Папка жасау мүмкін болмады: " + outDir, e);
        }
        KeyPair pair = keyService.generateKeyPair();
        pemUtil.writePrivateKey(pair.getPrivate(), outDir.resolve("private.pem"));
        pemUtil.writePublicKey(pair.getPublic(), outDir.resolve("public.pem"));
        System.out.println("Кілттер жасалды: " + outDir.resolve("private.pem")
                + ", " + outDir.resolve("public.pem"));
    }

    private void sign(Map<String, String> options) {
        Path file = Paths.get(require(options, "file"));
        Path sigPath = Paths.get(options.getOrDefault("sig", file + ".sig"));
        byte[] signature = signService.sign(file, pemUtil.readPrivateKey(Paths.get(require(options, "key"))));
        try {
            Files.write(sigPath, signature);
        } catch (IOException e) {
            throw new EsignException("Қолтаңбаны жазу мүмкін болмады: " + sigPath, e);
        }
        System.out.println("Қол қойылды: " + sigPath);
    }

    private boolean verify(Map<String, String> options) {
        Path file = Paths.get(require(options, "file"));
        Path sigPath = Paths.get(options.getOrDefault("sig", file + ".sig"));
        byte[] signature;
        try {
            signature = Files.readAllBytes(sigPath);
        } catch (IOException e) {
            throw new EsignException("Қолтаңба файлын оқу мүмкін болмады: " + sigPath, e);
        }
        boolean valid = signService.verify(file, signature,
                pemUtil.readPublicKey(Paths.get(require(options, "key"))));
        System.out.println(valid ? "VALID" : "INVALID");
        return valid;
    }

    /** "--ат мән" жұптарын Map-ке жинайды. */
    static Map<String, String> parseOptions(String[] args) {
        Map<String, String> options = new LinkedHashMap<>();
        for (int i = 1; i < args.length; i += 2) {
            if (!args[i].startsWith("--") || i + 1 >= args.length) {
                throw new EsignException("Параметр дұрыс емес: " + args[i]);
            }
            options.put(args[i].substring(2), args[i + 1]);
        }
        return options;
    }

    private static String require(Map<String, String> options, String name) {
        String value = options.get(name);
        if (value == null) {
            throw new EsignException("Міндетті параметр жоқ: --" + name);
        }
        return value;
    }

    private static void printUsage() {
        System.out.println("Қолдану:");
        System.out.println("  keygen [--out keys]");
        System.out.println("  sign   --key private.pem --file <файл> [--sig <файл.sig>]");
        System.out.println("  verify --key public.pem  --file <файл> [--sig <файл.sig>]");
    }
}
