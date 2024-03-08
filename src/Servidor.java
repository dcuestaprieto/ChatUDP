import models.ClienteModelo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

public class Servidor {
    public static final int PORT = 1234;
    private static final Map<Integer,ClienteModelo> clientes = new HashMap<>();
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
                    //debido a que en el map hay una clave con el valor que he recibido del cliente, considero que es un mensaje
                    esMensaje = true;
                    //añado el paquete a su lista de mensajes ya que no es el primer mensaje que me envia por lo que es un mensaje y no su nombre de usuario
                    clientes.get(paquete.getPort()).addMessage(mensajeCliente);
                }else {
                    clientes.put(paquete.getPort(),new ClienteModelo(mensajeCliente, paquete.getAddress(),paquete.getPort()));
                    System.out.println("Usuario añadido");
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
        }
    }

    private static boolean clienteExisteEnMap(int userPort) {
        return clientes.entrySet().stream().anyMatch(map -> map.getValue().getPort() == userPort);
    }

    private static DatagramPacket getDatagramPacket(DatagramPacket paquete) {
        String paqueteVueltaString = "hola mundo";
        byte[] paqueteVueltaBytes = paqueteVueltaString.getBytes();

        //DatagramPacket paqueteVuelta = new DatagramPacket(paqueteVueltaBytes, paqueteVueltaBytes.length, InetAddress.getLoopbackAddress(),12345);
        return new DatagramPacket(paqueteVueltaBytes, paqueteVueltaBytes.length, paquete.getAddress(), paquete.getPort());
    }
}
