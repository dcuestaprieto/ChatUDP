import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;

public class Servidor {
    private static Map<String,Object> clientes;
    public static void main(String[] args){
        try(DatagramSocket servidor = new DatagramSocket(1234)){
            byte[] bytesPaquetes = new byte[1024];

            //representa el paquete que le han enviado
            DatagramPacket paquete = new DatagramPacket(bytesPaquetes, bytesPaquetes.length);

            servidor.receive(paquete);

            //el offset es The index of the first byte to decode
            String paqueteString = new String(paquete.getData(),0, paquete.getLength()).toUpperCase();
            System.out.println(paqueteString);


            try(DatagramSocket datagramSocket = new DatagramSocket()){

                String paqueteVueltaString = "hola mundo";
                byte[] paqueteVueltaBytes = paqueteVueltaString.getBytes();

                //DatagramPacket paqueteVuelta = new DatagramPacket(paqueteVueltaBytes, paqueteVueltaBytes.length, InetAddress.getLoopbackAddress(),12345);
                DatagramPacket paqueteVuelta = new DatagramPacket(paqueteVueltaBytes, paqueteVueltaBytes.length, paquete.getAddress(),paquete.getPort());
                System.out.println(paquete.getAddress());
                System.out.println(paquete.getPort());

                datagramSocket.send(paqueteVuelta);

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
