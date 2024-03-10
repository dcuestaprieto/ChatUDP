import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.*;
import java.util.Base64;

public class Metodos {
    public static BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
    public static String getString(String prompt) throws IOException {
        String name;
        System.out.println(prompt);
        name = lector.readLine();
        return name;
    }

    public static KeyPair generateKeyPairs() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }
    public static String encriptar(String dato, PublicKey publicKey) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE,publicKey);
        byte[] bytesEncriptados = cipher.doFinal(dato.getBytes());
        System.out.println(bytesEncriptados.length);
        return Base64.getEncoder().encodeToString(bytesEncriptados);
    }
    public static String desencriptar(String datoEncriptado, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,privateKey);
        byte[] desencriptado = cipher.doFinal(Base64.getDecoder().decode(datoEncriptado));
        return new String(desencriptado);
    }
}
