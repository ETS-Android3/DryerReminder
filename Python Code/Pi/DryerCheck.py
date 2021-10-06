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
#For the sleep command
import time

#Size of the array
SIZE = 30

#Initialize arrays for each axes
arrayX = []
arrayY = []
arrayZ = []

#Initialize range used for calculating 
global savedRangeX
global savedRangeY
global savedRangeZ

global rangeX
global rangeY
global rangeZ




#Adds values in the x, y, and z arrays at the same time. 
def appendAxes(x, y, z):
    arrayX.append(x)
    arrayY.append(y)
    arrayZ.append(z)
    
    

#Get's range of the array inserted. 
def arrayRange(axisA):
    
    #initalize the varible for the largest and smallest values
    high = axisA[0]
    low = axisA[0]
    
    #Loop through array to find largest and smallest value.
    for e in axisA:
        if high < e: 
            high = e
        elif low > e:
            low = e
            
    range = high - low
    
    print("High: ", high)
    print("Low: ", low)
    print("Range: ", range)

    return range

#Save the ranges of the axis arrays to their ranges and clear them
def arrayCalcRanges():
    #Define the global variables so they can be looked at outside of the function.
    global rangeX
    global rangeY
    global rangeZ
    
    #Find the range of each array
    print("\nX Array:")
    rangeX = arrayRange(arrayX)
    print("\nY Array:")
    rangeY = arrayRange(arrayY)
    print("\nZ Array:")
    rangeZ = arrayRange(arrayZ)
    
    #Clear the array for the next set
    arrayX.clear()
    arrayY.clear()
    arrayZ.clear()

#If one of the arrays are equal to or more then 
def arraySizeCheck():
    if ((len(arrayX) >= SIZE)|(len(arrayY) >= SIZE)
        |(len(arrayZ) >= SIZE)):
        arrayCalcRanges()
#    else:
#        print("\nArray isn't large enough")


#Main Method
if __name__ == '__main__':
    
    print("Start")
    print("")
    
    #Setup IMU 
    sense = SenseHat()
    sense.clear()
    
    #Calibrate the first set when dryer is not moving.
    count = 0
    while (count < SIZE):
        
        axies = sense.get_accelerometer_raw()
        appendAxes(axies['x'], axies['y'], axies['z'])
        arraySizeCheck()    
        time.sleep(.5)
        count = count + 1
        
    savedRangeX = rangeX
    savedRangeY = rangeY + (rangeY * .20) #This gives some free room to mess up. Add a variable later.
    savedRangeZ = rangeZ
    
    #Constantly  
    while True:
        axies = sense.get_accelerometer_raw()
        appendAxes(axies['x'], axies['y'], axies['z'])
        arraySizeCheck()
        time.sleep(.5)
        print("Sleep")
        
        
        #Check if dryer is moving or not. Needs to be added to an if and only runs every SIZE ammount
        if (rangeY <= savedRangeY):
            print("Dryer is not moving")
        else:
            print("Dryer is moving")
    

    

    
    print("")
    print("End")