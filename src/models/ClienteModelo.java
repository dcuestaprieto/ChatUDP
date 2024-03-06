package models;

import java.security.PublicKey;
import java.util.ArrayList;

public class ClienteModelo {
    private String nombre;
    private int port;
    private ArrayList<String> mensajes = new ArrayList<>();
    private PublicKey publicKey;
}
