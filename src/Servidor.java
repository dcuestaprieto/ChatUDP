import models.ClienteModelo;
import models.Mensaje;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

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
    private static ReentrantLock lock = new ReentrantLock();
    public static PublicKey serverPublicKey;
    private static PrivateKey serverPrivateKey;

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

                if(clienteExisteEnMap(paquete.getPort())){
                    //si entra aqui es que el cliente sí existe en el map de clientes
                    ClienteModelo currentClient = clientes.get(paquete.getPort());
                    if(!clientes.get(paquete.getPort()).hasPublicKey()){
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
                        if(!mensajeCliente.equals("exit")){
                            //si el mensaje es diferente de exit, lo añado el paquete a su lista de mensajes
                            lock.lock();
                            clientes.get(paquete.getPort()).addMessage(mensajeCliente);
                            lock.unlock();
                            System.out.println("mensaje de "+currentClient.getNombre()+": "+mensajeCliente);
                            //Obtengo el último mensaje del cliente actual para obtener la hora a la que se ha enviado
                            Mensaje lastMessage = clientes.get(paquete.getPort()).getMensajes().get(currentClient.getMensajes().size()-1);
                            mensajeCliente = mensajeCliente.concat(" - "+currentClient.getNombre()+" - "+lastMessage.getCreatedAt().getHour() +":"+lastMessage.getCreatedAt().getMinute());
                        }else{
                            System.out.println("Adios "+currentClient.getNombre());
                            clientes.remove(currentClient.getPort());
                            mensajeCliente = "El usuario "+currentClient.getNombre()+" se ha desconectado";
                        }

                    }
                }else {

                    //cuando el cliente se conecta por primera vez tengo acceso a su nombre
                    lock.lock();
                    clientes.put(paquete.getPort(),new ClienteModelo(mensajeCliente, paquete.getAddress(),paquete.getPort()));
                    lock.unlock();
                    System.out.println("Usuario añadido");
                }

                //si esMensaje es true es que el servidor ha recibido un mensaje, por lo que este se debe reenviar a los usuarios
                if(esMensaje){
                    //creo otra variable ya que en una lambda las variables que se usan no deben cambiar de valor
                    String finalMensajeCliente = mensajeCliente;
                    clientes.forEach((key, client) -> {
                        //String mensajeParaEncriptar = mensajeCliente.concat(" "+client)
                        String mensajeEncriptado;
                        try{
                            //Encripto el mensaje del cliente
                            mensajeEncriptado = Metodos.encriptar(finalMensajeCliente,client.getPublicKey());
                        } catch (IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException |
                                 NoSuchAlgorithmException | InvalidKeyException e) {
                            System.out.println("Error reenviando mensaje encriptado al cliente "+client.getNombre());
                            throw new RuntimeException(e);
                        }
                        //Obtengo los bytes del mensaje encriptado
                        byte[] bytesMensajeParaCliente =  mensajeEncriptado.getBytes();

                        //le devuelvo el paquete enviado en mayusculas
                        DatagramPacket paqueteVuelta = new DatagramPacket(bytesMensajeParaCliente, bytesMensajeParaCliente.length, client.getAddress(), client.getPort());
                        try {
                            servidor.send(paqueteVuelta);
                        } catch (IOException e) {
                            System.out.println("Error mandando mensaje a: "+client.getNombre());
                        }
                    });
                    //Una vez ha entrado al if de es mensaje, pongo a false la variable por si el siguiente mensaje lo manda otro usuario que no está regitrado
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

}
