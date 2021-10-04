###########################################
#Author: Michael Mohler
#Date: 8/22/2021
#Project: Dryer Detector
#Device: Use an accelerometer attached to a pi to determine if a dryer is off or on. Then send a notification to the user.
#Explanation: The Pi will achieve this by first saving the range of the x, y, z axis when the machine is
#off. If the current range is consistently below or equal to the 'off' range then send a notification and stop the device.
###########################################

#Initialize arrays for each axes
arrayX = []
arrayY = []
arrayZ = []

#Initialize range used for calculating 
savedRangeX = -1
savedRangeY = -1
savedRangeZ = -1

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
    if ((len(arrayX) >= 10)|(len(arrayY) >= 10)
        |(len(arrayZ) >= 10)):
        arrayCalcRanges()
    else:
        print("\nArray isn't large enough")


#Main Method
if __name__ == '__main__':
    
    print("Start")
    print("")
    
    #How each axis will be added to the array
    appendAxes(1, 16.2, 22.34)
    appendAxes(4, 61.26, 27.49)
    appendAxes(12, 7.2, 4.97)
    arraySizeCheck()
    
    appendAxes(6, 16.2, 22.34)
    appendAxes(4, 61.26, 27.49)
    appendAxes(11, 7.2, 4.97)
    arraySizeCheck()
        
    appendAxes(6, 16.2, 22.34)
    appendAxes(4, 61.26, 27.49)
    appendAxes(61, 7.2, 4.97)
    arraySizeCheck()

    appendAxes(63, 7.2, 4.97)
    arraySizeCheck()
    
    appendAxes(72, 7.2, 4.97)
    arraySizeCheck()
    
    
    if (rangeX <= savedRangeX):
        print("Machine has stoped")
    
    print("")
    print("End")