import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.KeyPair;
import java.util.Random;

public class Cliente extends Thread{
    private int port;
    private KeyPair keys;

    @Override
    public void run() {
        Random random = new Random();
        random.nextInt(11);
    }

    public static void main(String[] args){
        try(DatagramSocket servidor = new DatagramSocket(123)){

            String paqueteString = "hola mundo";
            byte[] paqueteBytes = paqueteString.getBytes();

            DatagramPacket paquete = new DatagramPacket(paqueteBytes, paqueteBytes.length, InetAddress.getLoopbackAddress(),1234);
            System.out.println(paquete.getPort());

            servidor.send(paquete);


            byte[] bytesPaquetes = new byte[1024];

            //representa el paquete que le han enviado
            DatagramPacket paqueteServidor = new DatagramPacket(bytesPaquetes, bytesPaquetes.length);

            servidor.receive(paqueteServidor);

            //el offset es The index of the first byte to decode
            String paqueteServidorString = new String(paqueteServidor.getData(), 0, paqueteServidor.getLength()).toUpperCase();
            System.out.println("El mensaje recibido es: "+paqueteServidorString);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
