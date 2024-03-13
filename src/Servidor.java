import models.ClienteModelo;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Servidor {
    public static final int PORT = 1234;
    public static final InetAddress SERVER_IP;

    static {
        try {
            SERVER_IP = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Map<Integer,ClienteModelo> clientes = new HashMap<>();
    public static PublicKey serverPublicKey;
    public static PrivateKey serverPrivateKey;

    static {
        try {
            KeyPair serverKeys = Metodos.generateKeyPairs();
            serverPublicKey = serverKeys.getPublic();
            serverPrivateKey = serverKeys.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error generando las claves del servidor");
        }
    }

    public static void main(String[] args){
        try(DatagramSocket servidor = new DatagramSocket(PORT)){
            byte[] bytesPaquetes;
            boolean esMensaje = false;
            while(true) {
                bytesPaquetes = new byte[2048];

                //representa el paquete que le han enviado
                DatagramPacket paquete = new DatagramPacket(bytesPaquetes, bytesPaquetes.length);
                //Espero a recibir paquetes
                servidor.receive(paquete);

                //convierto a string el paquete recibido.
                String mensajeCliente = new String(paquete.getData(), 0, paquete.getLength());
                //String prueba = Metodos.desencriptar(mensajeCliente, Servidor.serverPrivateKey);
                //System.out.println(prueba);

                if(clienteExisteEnMap(paquete.getPort())){
                    ClienteModelo currentClient = clientes.get(paquete.getPort());
                    System.out.println(currentClient.getNombre()+" ya existe");
                    if(!clientes.get(paquete.getPort()).hasPublicKey()){
                        System.out.println(currentClient.getNombre()+" no tiene clave publica");
                        /*
                         * en caso de que entre aqui es que el cliente existe en el map pero no tiene una clave publica,
                         * por lo que sería el segundo mensaje mandado por el cliente
                         * asi que le añado su clave publica, que es el mensaje que ha mandado.
                         * El cliente envia como primer mensaje su nombre de usuario, y luego su clave publica
                         */
                        try{
                            PublicKey publicKey =
                                    KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(paquete.getData()));
                            clientes.get(paquete.getPort()).setPublicKey(publicKey);
                            System.out.println("clave publica de "+currentClient.getNombre()+" añadida");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }else {
                        /*
                         * si entra aquí es que el usuario que ha mandado mensaje está registrado en el map y tiene public key,
                         * por lo que solo queda que sea mensaje
                         */
                        esMensaje = true;
                        //añado el paquete a su lista de mensajes
                        clientes.get(paquete.getPort()).addMessage(mensajeCliente);
                        System.out.println("mensaje de "+currentClient.getNombre()+": "+mensajeCliente);
                    }
                }else {

                    //cuando el cliente se conecta por primera vez tengo acceso a su nombre
                    clientes.put(paquete.getPort(),new ClienteModelo(mensajeCliente, paquete.getAddress(),paquete.getPort()));
                    System.out.println("Usuario añadido");
                }

                //si esMensaje es true es que el servidor ha recibido un mensaje, por lo que este se debe reenviar a los usuarios
                if(esMensaje){
                    clientes.forEach((key, client) -> {
                        try{
                            String mensajeEncriptado = Metodos.encriptar(mensajeCliente,client.getPublicKey());
                            System.out.println(mensajeEncriptado);
                        } catch (IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException |
                                 NoSuchAlgorithmException | InvalidKeyException e) {
                            System.out.println("Error reenviando mensaje encriptado al cliente "+client.getNombre());
                            throw new RuntimeException(e);
                        }
                        //cambiar para que no sea este string si no el mensaje que haya recibido de cada usuario
                        byte[] bytesMensajeParaCliente =  mensajeCliente.getBytes();

                        //le devuelvo el paquete enviado en mayusculas
                        DatagramPacket paqueteVuelta = new DatagramPacket(bytesMensajeParaCliente, bytesMensajeParaCliente.length, client.getAddress(), client.getPort());
                        try {
                            servidor.send(paqueteVuelta);
                        } catch (IOException e) {
                            System.out.println("Error mandando mensaje a: "+client.getNombre());
                        }
                    });
                    esMensaje = false;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean clienteExisteEnMap(int userPort) {
        return clientes.entrySet().stream().anyMatch(map -> map.getValue().getPort() == userPort);
    }
    public static PublicKey stringToPublicKey(String publicKeyString) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // Cambia "RSA" por el algoritmo de tu clave pública
        return keyFactory.generatePublic(keySpec);
    }

    private static DatagramPacket getDatagramPacket(DatagramPacket paquete) {
        String paqueteVueltaString = "hola mundo";
        byte[] paqueteVueltaBytes = paqueteVueltaString.getBytes();

        //DatagramPacket paqueteVuelta = new DatagramPacket(paqueteVueltaBytes, paqueteVueltaBytes.length, InetAddress.getLoopbackAddress(),12345);
        return new DatagramPacket(paqueteVueltaBytes, paqueteVueltaBytes.length, paquete.getAddress(), paquete.getPort());
    }
}
