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
    private static KeyPair serverKeys;
    public static PublicKey serverPublicKey;
    private static PrivateKey serverPrivateKey;

    static {
        try {
            serverKeys = Metodos.generateKeyPairs();
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
                bytesPaquetes = new byte[1024];

                //representa el paquete que le han enviado
                DatagramPacket paquete = new DatagramPacket(bytesPaquetes, bytesPaquetes.length);
                //Espero a recibir paquetes
                servidor.receive(paquete);

                //el offset es The index of the first byte to decode
                //convierto a string el paquete recibido. Este debe ser el nombre del usuario
                String mensajeCliente = new String(paquete.getData(), 0, paquete.getLength());
                //System.out.println(username);

                if(clienteExisteEnMap(paquete.getPort())){
                    if(clientes.get(paquete.getPort()).hasPublicKey()){
                        /*
                         * en caso de que entre aqui es que el cliente existe en el map pero no tiene una clave publica,
                         * por lo que sería el segundo mensaje mandado por el cliente
                         * asi que le añado su clave publica, que es el mensaje que ha mandado.
                         * El cliente envia como primer mensaje su nombre de usuario, y luego su clave publica
                         */
                        clientes.get(paquete.getPort()).setPublicKey(convertirBytesEnClavePublica(servidor));
                    }
                    //debido a que en el map hay una clave con el valor que he recibido del cliente, considero que es un mensaje
                    esMensaje = true;
                    //añado el paquete a su lista de mensajes ya que no es el primer mensaje que me envia por lo que es un mensaje y no su nombre de usuario
                    clientes.get(paquete.getPort()).addMessage(mensajeCliente);
                }else {
                    try{

                        String cadena = "hola mundo";
                        String cadenaCrypt = Metodos.encriptar(cadena,Servidor.serverPublicKey);
                        String cadenaDecrypt = Metodos.desencriptar(cadenaCrypt, serverPrivateKey);
                        System.out.println(cadenaDecrypt);

                        System.out.println("la publica encriptada es: "+mensajeCliente);
                        String stringUserData = Metodos.desencriptar(mensajeCliente,serverPrivateKey);

                        System.out.println(stringUserData);

                        //cuando el cliente se conecta por primera vez tengo acceso a su nombre y a su clave publica
                        clientes.put(paquete.getPort(),new ClienteModelo(stringUserData, paquete.getAddress(),paquete.getPort()));
                        System.out.println("Usuario añadido");
                        System.out.println(clientes.get(paquete.getPort()).getPublicKey());
                    } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
                clientes.forEach((key, value)->{
                    System.out.println(key+" contiene "+value.toString());
                });

                //si esMensaje es true es que el servidor ha recibido un mensaje, por lo que este se debe reenviar a los usuarios
                if(esMensaje){
                    clientes.forEach((key, client) -> {
                        //System.out.println(key + " contiene " + client.getNombre());
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
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            System.out.println("Error convirtiendo la clave publica del cliente");
        }
    }

    private static PublicKey convertirBytesEnClavePublica(DatagramSocket servidor) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Crear un buffer para recibir los datos
        byte[] buffer = new byte[2048];

        // Recibir el paquete del cliente
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        servidor.receive(packet);

        // Convertir los datos recibidos en una instancia de PublicKey
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(packet.getData()));

        // Imprimir la clave pública recibida
        System.out.println("Clave pública recibida: " + publicKey);
        return publicKey;
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
