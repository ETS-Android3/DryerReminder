#Model to handel the three Axis for the Accerammeter

import json

class AxesModel:
    
    #Varaibles for each axis
    axisX = 0
    axisY = 0
    axisZ = 0

    
    #Paraterized And Default Constructor
    def __init__(self, x=0,y=0,z=0):
        self.axisX = x
        self.axisY = y
        self.axisZ = z

    #Change the model to json to send over API
    def toJSON(self):
        jsonFormat = {
            "axisX":self.axisX,
            "axisY":self.axisY,
            "axisZ":self.axisZ
            }
        return jsonFormat
    
    #Getters for X, Y, Z varaibles
    def getAxisX(self):
        return self.axisX
    
    def getAxisY(self):
        return self.axisY
    
    def getAxisZ(self):
        return self.axisZ
    
    
    #Setters for X, Y, Z varaibles
    def setAxisX(self, x=0):
        self.axisX = x
        
    def setAxisY(self, y=0):
        self.axisY = y
        
    def setAxisZ(self, z=0):
        self.axisZ = z
