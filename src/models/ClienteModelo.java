package models;

import java.net.InetAddress;
import java.security.PublicKey;
import java.util.ArrayList;

public class ClienteModelo {
    private String nombre;
    private InetAddress address;
    private int port;
    private ArrayList<Mensaje> mensajes = new ArrayList<>();
    private PublicKey publicKey;

    public ClienteModelo(String nombre, InetAddress address, int port) {
        this.nombre = nombre;
        this.address = address;
        this.port = port;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ArrayList<Mensaje> getMensajes() {
        return mensajes;
    }

    public void setMensajes(ArrayList<Mensaje> mensajes) {
        this.mensajes = mensajes;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "ClienteModelo{" +
                "nombre='" + nombre + '\'' +
                ", address=" + address +
                ", port=" + port +
                ", mensajes=" + mensajes +
                ", publicKey=" + publicKey +
                '}';
    }

    public void addMessage(String mensaje){
        mensajes.add(new Mensaje(mensaje));

    }
}
