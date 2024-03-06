import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class Metodos {
    public static BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
    public static String getString(String prompt) throws IOException {
        String name = "";
        System.out.println(prompt);
        name = lector.readLine();
        return name;
    }

    public static KeyPair generateKeyPairs() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(4096);
        return generator.generateKeyPair();
    }
}
