package models;

import java.security.PublicKey;
import java.util.ArrayList;

public class ClienteModelo {
    private String nombre;
    private int port;
    private ArrayList<Mensaje> mensajes = new ArrayList<>();
    private PublicKey publicKey;

    public ClienteModelo(String nombre, int port) {
        this.nombre = nombre;
        this.port = port;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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
}
