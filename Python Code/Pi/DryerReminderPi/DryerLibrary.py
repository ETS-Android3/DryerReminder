###########################################
#Author: Michael Mohler
#Date: 8/22/2021
#Project: Dryer Detector
#Device: Use an accelerometer attached to a pi to determine if a dryer is off or on. Then send a notification to the user.
#Explanation: The Pi will achieve this by first saving the range of the x, y, z axis when the machine is
#off. If the current range is consistently below or equal to the 'off' range then send a notification and stop the device.

#This will be the one on the Pi
###########################################

#Let's IMU reed from the excelerameter
from sense_hat import SenseHat
import AxesModel

#For the sleep command
import time

#Size of the array
SIZE = 60

#Initialize arrays for each axes
axes = []

#Initialize range used for calculating
global savedRange
global rangeAxes



#Adds values in the x, y, and z arrays at the same time. 
def appendAxes(AxesModel):
    axes.append(AxesModel)
    
    

#Get's range of the axes array inserted. 
def arrayRange(axisA):
    
    #initalize the varible for the largest and smallest values of
    highX = axisA[0].get_axis_x()
    lowX = axisA[0].get_axis_x()

    highY = axisA[0].get_axis_y()
    lowY = axisA[0].get_axis_y()

    highZ = axisA[0].get_axis_z()
    lowZ = axisA[0].get_axis_z()
    
    #Loop through array to find largest and smallest value of x.
    for e in axisA:
        
        #X axis
        if highX < e.get_axis_x(): 
            highX = e.get_axis_x()
        elif lowX > e.get_axis_x():
            lowX = e.get_axis_x()
        
        #Y axis
        if highY < e.get_axis_y(): 
            highY = e.get_axis_y()
        elif lowY > e.get_axis_y():
            lowY = e.get_axis_y()

        #Z axis
        if highZ < e.get_axis_z(): 
            highZ = e.get_axis_z()
        elif lowZ > e.get_axis_z():
            lowZ = e.get_axis_z()

    #Save and print range 
    rangeX = highX - lowX
    rangeY = highY - lowY
    rangeZ = highZ - lowZ

    #Save range to axes model
    foundRange = AxesModel.AxesModel(rangeX, rangeY, rangeZ)

    #Print Range
    print("Range X: ", rangeX)
    print("Range Y: ", rangeY)
    print("Range Z: ", rangeZ)

    return foundRange


#Save the ranges of the axis arrays to their ranges and clear them
def arrayCalcRanges():
    #Define the global variables so they can be looked at outside of the function.
    global rangeAxes
    
    rangeAxes = arrayRange(axes) 

    #Clear the array for the next set
    axes.clear()


#If one of the arrays are equal to or more then 
def arraySizeCheck():
    if (len(axes) >= SIZE):
        arrayCalcRanges()


#Save offset to a file to read from later.
def setOffset(newOffset):
    file = open('config.txt', 'w')
    file.write(str(newOffset))
    file.close()


#pull the offset from a file and return it as an int
def getOffset():
    file = open('config.txt', 'r')
    data = file.read()
    file.close()
    return int(data)





def calibrate():
    print("Starting: Calibration Check")
    print("")
    
    #Setup IMU 
    sense = SenseHat()
    sense.clear()
    
    #Calibrate the first set when dryer is not moving.
    count = 0
    while (count < SIZE):
        
        axies = sense.get_accelerometer_raw()
        appendAxes(AxesModel.AxesModel(axies['x'], axies['y'], axies['z']))
        arraySizeCheck()    
        time.sleep(.2)
        count = count + 1

    #Offset needs to be moved to main method
    savedRangeX = rangeAxes.get_axis_x()
    savedRangeY = rangeAxes.get_axis_y()
    savedRangeZ = rangeAxes.get_axis_z()

    print("Ending: Calibration Check")

    return AxesModel.AxesModel(savedRangeX, savedRangeY, savedRangeZ)







def justMain(givenRange):
    
    countCheck = 0

    global savedRange 

    setOffset(2)
    offset = getOffset()
    


    #create the offset for each axis
    savedRangeX = givenRange.get_axis_x() + (givenRange.get_axis_x() * offset)
    savedRangeY = givenRange.get_axis_y() + (givenRange.get_axis_y() * offset)
    savedRangeZ = givenRange.get_axis_z() + (givenRange.get_axis_z() * offset)

    savedRange = AxesModel.AxesModel(savedRangeX, savedRangeY, savedRangeZ)

    print("Starting: Shake Detection")

    #Setup IMU 
    sense = SenseHat()
    sense.clear()

    #Constantly  
    while True:
        axies = sense.get_accelerometer_raw()
        appendAxes(AxesModel.AxesModel(axies['x'], axies['y'], axies['z']))
        arraySizeCheck()
        time.sleep(.2)
        
        
        if (len(axes) == 0):
            #Check if dryer is moving or not. Needs to be added to an if and only runs every SIZE ammount
            if (rangeAxes.get_axis_y() <= savedRange.get_axis_y()):
                print()
                print("==Dryer is not moving==")
                countCheck = countCheck + 1
                if(countCheck == 2):
                    print("Dryer has stopped")
                    break
            else:
                print()
                print("==Dryer is moving==")
                countCheck = 0
    
    print("")
    print("End")
    
#Main Method
if __name__ == '__main__':
    justMain()