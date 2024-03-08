import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

public class Cliente extends Thread{
    private String nombre;
    private int port;
    private KeyPair keys;

    @Override
    public void run() {
        String cryptedUsername;
        try{

            port = getAvailablePort();
            nombre = Metodos.getString("Dime tu nombre");
            keys = Metodos.generateKeyPairs();

            cryptedUsername = Metodos.encriptar(nombre, keys.getPublic());

        } catch (SocketException e) {
            System.out.println("Error generando el puerto");
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("Error pidiendo el nombre");
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error creando las claves");
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            System.out.println("Error con la cantidad de bytes para encriptar contraseña");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            System.out.println("Clave publica erronea");
        }

        try(DatagramSocket servidor = new DatagramSocket(port)){
            //reasigno el puerto por si el metodo devuelve 0 y se ha usado el primer puerto disponible
            port = servidor.getLocalPort();

            //envio al servidor el nombre de este cliente
            //byte[] paqueteBytes = nombre.getBytes();
            byte[] paqueteBytes = nombre.getBytes();
            DatagramPacket paquete = new DatagramPacket(paqueteBytes, paqueteBytes.length, InetAddress.getLoopbackAddress(),Servidor.PORT);
            servidor.send(paquete);

            String respuestaServidor;

            while(true){
                escribirAlServidor(servidor,Metodos.getString("Escribe un mensaje: "));
                respuestaServidor = recibirMensajeServidor(servidor);
                System.out.println(respuestaServidor);
            }

        } catch(BindException be){
            System.out.println("Puerto ya en uso.");
            throw new RuntimeException(be);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String recibirMensajeServidor(DatagramSocket servidor) throws IOException {
        byte[] bytesPaquetes = new byte[1024];

        //representa el paquete que le han enviado
        DatagramPacket paqueteRecibido = new DatagramPacket(bytesPaquetes, bytesPaquetes.length);
        servidor.receive(paqueteRecibido);
        //el offset es The index of the first byte to decode
        return new String(paqueteRecibido.getData(),0, paqueteRecibido.getLength());
    }

    private void escribirAlServidor(DatagramSocket servidor, String mensaje) throws IOException {
        byte[] paqueteBytes = mensaje.getBytes();
        DatagramPacket paquete = new DatagramPacket(paqueteBytes, paqueteBytes.length, InetAddress.getLoopbackAddress(),Servidor.PORT);
        servidor.send(paquete);
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
            //System.out.println("desde metodo, el puerto es: " + availablePort);
        }
        return availablePort;
    }

    public static void main(String[] args){
        Cliente cliente = new Cliente();
        cliente.start();
    }
}
