package com.example.myfirstapp.model;

public class AxesModel {

    //Variables
    private double axisX;
    private double axisY;
    private double axisZ;

    //Constructor
    public AxesModel(double axisX, double axisY, double axisZ)
    {
        this.axisX = axisX;
        this.axisY = axisY;
        this.axisZ = axisZ;
    }

    //Getters and Setters
    public double getAxisX() {
        return axisX;
    }

    public void setAxisX(double axisX) {
        this.axisX = axisX;
    }

    public double getAxisY() {
        return axisY;
    }

    public void setAxisY(double axisY) {
        this.axisY = axisY;
    }

    public double getAxisZ() {
        return axisZ;
    }

    public void setAxisZ(double axisZ) {
        this.axisZ = axisZ;
    }
}
