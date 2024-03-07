import models.ClienteModelo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

public class Servidor {
    public static final int PORT = 1234;
    private static final Map<String,ClienteModelo> clientes = new HashMap<>();
    public static void main(String[] args){
        try(DatagramSocket servidor = new DatagramSocket(PORT)){
            byte[] bytesPaquetes;
            while(true) {
                bytesPaquetes = new byte[1024];

                //representa el paquete que le han enviado
                DatagramPacket paquete = new DatagramPacket(bytesPaquetes, bytesPaquetes.length);
                //Espero a recibir paquetes
                servidor.receive(paquete);

                //el offset es The index of the first byte to decode
                //convierto a string el paquete recibido. Este debe ser el nombre del usuario
                String username = new String(paquete.getData(), 0, paquete.getLength()).toUpperCase();
                System.out.println(username);
                if(clientes.putIfAbsent(username, new ClienteModelo(username, paquete.getPort()))==null){
                    System.out.println(username + " ha entrado al chat");
                } else if (username.length()>10) {
                    //supongamos que mayor de 10 no vale como nombre de usuario

                }else {
                    System.out.println("Usuario actualizado");
                }

                clientes.forEach((key, value) -> System.out.println(key + " contiene " + value.getNombre()));
                //cambiar para que no sea este string si no el mensaje que haya recibido de cada usuario
                byte[] paqueteVueltaBytes = username.getBytes();

                //le devuelvo el paquete enviado en mayusculas
                DatagramPacket paqueteVuelta = new DatagramPacket(paqueteVueltaBytes, paqueteVueltaBytes.length, paquete.getAddress(), paquete.getPort());
                servidor.send(paqueteVuelta);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static DatagramPacket getDatagramPacket(DatagramPacket paquete) {
        String paqueteVueltaString = "hola mundo";
        byte[] paqueteVueltaBytes = paqueteVueltaString.getBytes();

        //DatagramPacket paqueteVuelta = new DatagramPacket(paqueteVueltaBytes, paqueteVueltaBytes.length, InetAddress.getLoopbackAddress(),12345);
        DatagramPacket paqueteVuelta = new DatagramPacket(paqueteVueltaBytes, paqueteVueltaBytes.length, paquete.getAddress(), paquete.getPort());
        return paqueteVuelta;
    }
}
