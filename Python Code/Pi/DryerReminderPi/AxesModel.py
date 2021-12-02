#Model to handel the three Axis for the Accerammeter


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
