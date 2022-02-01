package com.example.myfirstapp.model;

/**
 *  Model that represents the x, y, z axis that will be read or calculated from an accelerometer.
 *
 */
public class AxesModel
{

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

    //Default Constructor
    public AxesModel()
    {
        axisX = 0;
        axisY = 0;
        axisZ = 0;
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
