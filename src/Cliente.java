import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.*;
import java.security.*;

public class Cliente extends Thread{
    private String nombre;
    private int port;
    public PublicKey publicKey;
    public PrivateKey privateKey;

    public static void main(String[] args) {
        Cliente cliente = new Cliente();
        cliente.start();
    }

    @Override
    public void run() {
        String cryptedFirtsMessage = "";
        try{
            //genero los atributos de clase
            port = getAvailablePort();
            //pido el nombre del cliente
            nombre = Metodos.getString("Dime tu nombre");
            //genero las claves publicas y privadas y las establezco
            KeyPair serverKeys = Metodos.generateKeyPairs();
            publicKey = serverKeys.getPublic();
            privateKey = serverKeys.getPrivate();

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

        try(DatagramSocket servidor = new DatagramSocket(port)){
            //reasigno el puerto por si el metodo devuelve 0 y se ha usado el primer puerto disponible
            port = servidor.getLocalPort();
            //encripto el nombre del clientes
            System.out.println(cryptedFirtsMessage);
            escribirAlServidor(servidor,nombre);
            mandarClavePublica(servidor, publicKey);

            //String respuestaServidor;

            while(true){
                new Thread(()->{
                    while(true) {
                        String respuestaServidor;
                        try {
                            respuestaServidor = recibirMensajeServidor(servidor);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println(respuestaServidor);
                    }
                }).start();
                String mensajeParaServidor = Metodos.getString("Escribe un mensaje: ");
                if(!mensajeParaServidor.equals("exit")){
                    escribirAlServidor(servidor,mensajeParaServidor);
                }else{
                    escribirAlServidor(servidor,"exit");
                    System.out.println("Adios "+nombre);
                    System.exit(0);
                }
            }

        } catch(BindException be){
            System.out.println("Puerto ya en uso.");
            throw new RuntimeException(be);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void mandarClavePublica(DatagramSocket servidor, PublicKey publicKey) throws IOException {
        byte[] bytesPublicKey = publicKey.getEncoded();

        DatagramPacket packetPublicKey = new DatagramPacket(bytesPublicKey, bytesPublicKey.length, Servidor.SERVER_IP,Servidor.PORT);
        servidor.send(packetPublicKey);
    }

    private String recibirMensajeServidor(DatagramSocket servidor) throws IOException {
        byte[] bytesPaquetes = new byte[1024];

        //representa el paquete que le han enviado
        DatagramPacket paqueteRecibido = new DatagramPacket(bytesPaquetes, bytesPaquetes.length);
        servidor.receive(paqueteRecibido);
        String mensajeEncriptado = new String(paqueteRecibido.getData(),0, paqueteRecibido.getLength());
        String mensajeDesencriptado;
        try{
            mensajeDesencriptado = Metodos.desencriptar(mensajeEncriptado,privateKey);
        } catch (IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException |
                 NoSuchAlgorithmException | InvalidKeyException e) {
            System.out.println("Error desencriptando el mensaje del servidor");
            throw new RuntimeException(e);
        }
        return mensajeDesencriptado;
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
        }
        return availablePort;
    }
}
