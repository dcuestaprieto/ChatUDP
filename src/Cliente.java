import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

public class Cliente extends Thread{
    private String nombre;
    private int port;
    private KeyPair keys;

    @Override
    public void run() {
        try{

            port = getAvailablePort();
            nombre = Metodos.getString("Dime tu nombre");
            keys = Metodos.generateKeyPairs();

        } catch (SocketException e) {
            System.out.println("Error generando el puerto");
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("Error pidiendo el nombre");
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error creando las claves");
            throw new RuntimeException(e);
        }

        System.out.println(port);

        try(DatagramSocket servidor = new DatagramSocket(port)){
            //reasigno el puerto por si el metodo devuelve 0 y se ha usado el primer puerto disponible
            port = servidor.getLocalPort();

            //envio al servidor el nombre de este cliente
            byte[] paqueteBytes = nombre.getBytes();
            DatagramPacket paquete = new DatagramPacket(paqueteBytes, paqueteBytes.length, InetAddress.getLoopbackAddress(),Servidor.PORT);
            servidor.send(paquete);

            byte[] bytesPaquetes = new byte[1024];

            //representa el paquete que le han enviado
            DatagramPacket paqueteRecivido = new DatagramPacket(bytesPaquetes, bytesPaquetes.length);
            servidor.receive(paqueteRecivido);
            //el offset es The index of the first byte to decode
            String paqueteString = new String(paqueteRecivido.getData(),0, paqueteRecivido.getLength()).toUpperCase();
            System.out.println(paqueteString);

            while(true){
                Metodos.getString("Escribe un mensaje: ");
                escribirAlServidor();
            }

        } catch(BindException be){
            System.out.println("Puerto ya en uso.");
            throw new RuntimeException(be);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void escribirAlServidor() {

    }

    /**
     * Metodo para generar un puerto libre en la máquina
     * @return devuelve un puerto disponible o 0 en caso de error
     * @throws SocketException if the socket could not be opened, or the socket could not bind to the specified local port.
     */
    private int getAvailablePort() throws SocketException {
        int availablePort = 0;
        try(DatagramSocket servidor = new DatagramSocket(0)){
            availablePort = servidor.getLocalPort();
            System.out.println("desde metodo, el puerto es: " + availablePort);
        }
        return availablePort;
    }

    public static void main(String[] args){
        Cliente cliente = new Cliente();
        cliente.start();
    }
}
