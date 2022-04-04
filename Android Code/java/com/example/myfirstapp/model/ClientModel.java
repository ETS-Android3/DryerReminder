package com.example.myfirstapp.model;

/**
 * Model that holds the data needed to connect to the Raspberry Pi.
 * Convenient for changing things like the IP throughout the entire app.
 *
 */
public class ClientModel
{
    //Variables
    private String ipAddress;
    private String port;
    private int socketPort;
    private String token; //Security Token needed to access API

    //Constructor
    public ClientModel(String ipAddress, String port, int socketPort, String token) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.socketPort = socketPort;
        this.token = token;
    }

    //Default Constructor
    public ClientModel() {
        ipAddress = "192.168.0.23";
        port = "7069";
        socketPort = 9607;
        token = "justUseATimer";
    }

    //Getters and Setters
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getSocketPort() {
        return socketPort;
    }

    public void setSocketPort(int socketPort) {
        this.socketPort = socketPort;
    }
}
