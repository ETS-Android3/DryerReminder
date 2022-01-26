package com.example.myfirstapp.model;

public class ClientModel
{
    //Variables
    private String ipAddress;
    private String port;
    private String token;

    //Constructor
    public ClientModel(String ipAddress, String port, String token) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.token = token;
    }

    //Constructor
    public ClientModel() {
        ipAddress ="192.168.0.23";
        port = "7069";
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
}
