package app.fintrack.infra.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHasher {
    private static final String ALGO = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 16;

    private PasswordHasher() {}

    public static String hash(char[] password) {
        try {
            byte[] salt = new byte[SALT_BYTES];
            new SecureRandom().nextBytes(salt);
            byte[] hash = pbkdf2(password, salt, ITERATIONS, KEY_LENGTH);
            return "pbkdf2"+":"+ITERATIONS+":"+ Base64.getEncoder().encodeToString(salt)+":"+ Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) { throw new IllegalStateException(e); }
        finally { wipe(password); }
    }

    public static boolean verify(char[] password, String stored) {
        try {
            String[] parts = stored.split(":");
            int it = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(password, salt, it, expected.length * 8);
            int diff = 0; // constant-time compare
            for (int i = 0; i < actual.length; i++) diff |= actual[i] ^ expected[i];
            return diff == 0;
        } catch (Exception e) { return false; }
        finally { wipe(password); }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLen) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLen);
        return SecretKeyFactory.getInstance(ALGO).generateSecret(spec).getEncoded();
    }

    private static void wipe(char[] arr) { if (arr != null) java.util.Arrays.fill(arr, '\0'); }
}
